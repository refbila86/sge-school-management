package mz.co.sge.controller;

import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.ClassLevelEntity;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.entity.TeacherEntity;
import mz.co.sge.service.IAcademicYearService;
import mz.co.sge.service.IClassLevelService;
import mz.co.sge.service.IClassroomService;
import mz.co.sge.service.ITeacherService;
import mz.co.sge.entity.ClassroomEntity.Shift;

@Named
@ViewScoped
public class ClassroomBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Autowired
	private IClassroomService classroomService;
	@Autowired
	private IClassLevelService classLevelService;
	@Autowired
	private ITeacherService teacherService;
	@Autowired
	private IAcademicYearService academicYearService;

	@Inject
	private LoginBean loginBean;
	@Inject
	private DashboardBean dashboardBean;

	private List<ClassroomEntity> classrooms;
	private List<ClassLevelEntity> classLevels;
	private List<TeacherEntity> teachers;
	private List<AcademicYearEntity> academicYears;

	private ClassroomEntity classroom = new ClassroomEntity();
	private boolean editing = false;
	private String searchTerm;
	private Long selectedTeacherId;
	private Long selectedClassLevelId;
	private Long selectedAcademicYearId;

	@PostConstruct
	public void init()
	{
		loadAcademicYears();
		loadClassLevels();
		loadTeachers();
		loadClassrooms();
	}

	public void loadClassrooms()
	{
		try
		{
			Long yearId = selectedAcademicYearId != null ? selectedAcademicYearId : loginBean.getAcademicYearId();
			System.out.println(">>> loadClassrooms yearId: " + yearId);
			if (yearId != null)
			{
				classrooms = classroomService.findByAcademicYear(yearId);
			} else
			{
				// FALLBACK: lista tudo se não tem ano logado
				classrooms = classroomService.findAll();
			}
			System.out.println(">>> classrooms found: " + (classrooms != null ? classrooms.size() : 0));
			if (classrooms == null)
				classrooms = List.of();
		} catch (Exception e)
		{
			e.printStackTrace();
			classrooms = classroomService.findAll();
		}
	}

	public void loadAcademicYears()
	{
		try
		{
			academicYears = academicYearService.getAllAcademicYears();
		} catch (Exception e)
		{
			academicYears = List.of();
		}
	}

	public void loadClassLevels()
	{
		try
		{
			classLevels = classLevelService.findAll();
		} catch (Exception e)
		{
			classLevels = List.of();
		}
	}

	public void loadTeachers()
	{
		try
		{
			teachers = teacherService.findAll();
		} catch (Exception e)
		{
			teachers = List.of();
		}
	}

	public void search()
	{
		if (searchTerm == null || searchTerm.isBlank())
		{
			loadClassrooms();
			return;
		}
		Long yearId = loginBean.getAcademicYearId();
		classrooms = classroomService.search(searchTerm);
	}

	public void cleanForm()
	{
		classroom = new ClassroomEntity();
		classroom.setActive(true);
		classroom.setCurrentStudents(0);
		editing = false;
		selectedTeacherId = null;
		selectedClassLevelId = null;
		selectedAcademicYearId = loginBean.getAcademicYearId();
	}

	public void save()
	{
		try
		{
			if (classroom.getCapacity() <= 0)
			{
				addMsg(FacesMessage.SEVERITY_ERROR, "Erro", "Capacity must be > 0");
				return;
			}
			if (selectedTeacherId != null)
				classroom.setResponsibleTeacher(teacherService.findById(selectedTeacherId));
			if (selectedClassLevelId != null)
				classroom.setClassLevel(classLevelService.findById(selectedClassLevelId));

			Long yId = selectedAcademicYearId != null ? selectedAcademicYearId : loginBean.getAcademicYearId();
			if (yId == null)
			{
				addMsg(FacesMessage.SEVERITY_ERROR, "Erro", "Academic year not selected");
				return;
			}
			AcademicYearEntity ay = academicYearService.findByYearId(yId);
			if (ay == null)
				ay = academicYearService.getAcademicYearById(yId).orElse(null);
			classroom.setAcademicYear(ay);

			classroomService.save(classroom);
			addMsg(FacesMessage.SEVERITY_INFO, "Sucesso", "Classroom saved");
			loadClassrooms();
			dashboardBean.navigate("classroom/classroom-list", "turmas");
		} catch (Exception e)
		{
			e.printStackTrace();
			addMsg(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
		}
	}

	public Shift[] getTurnos()
	{
		return Shift.values();
	}

	private void addMsg(FacesMessage.Severity s, String sum, String det)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(s, sum, det));
	}

	// getters setters
	public List<ClassroomEntity> getClassrooms()
	{
		return classrooms;
	}

	public void setClassrooms(List<ClassroomEntity> classrooms)
	{
		this.classrooms = classrooms;
	}

	public ClassroomEntity getClassroom()
	{
		return classroom;
	}

	public void setClassroom(ClassroomEntity classroom)
	{
		this.classroom = classroom;
	}

	public boolean isEditing()
	{
		return editing;
	}

	public void setEditing(boolean editing)
	{
		this.editing = editing;
	}

	public String getSearchTerm()
	{
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm)
	{
		this.searchTerm = searchTerm;
	}

	public Long getSelectedTeacherId()
	{
		return selectedTeacherId;
	}

	public void setSelectedTeacherId(Long id)
	{
		this.selectedTeacherId = id;
	}

	public Long getSelectedClassLevelId()
	{
		return selectedClassLevelId;
	}

	public void setSelectedClassLevelId(Long id)
	{
		this.selectedClassLevelId = id;
	}

	public Long getSelectedAcademicYearId()
	{
		return selectedAcademicYearId;
	}

	public void setSelectedAcademicYearId(Long id)
	{
		this.selectedAcademicYearId = id;
	}

	public List<ClassLevelEntity> getClassLevels()
	{
		return classLevels;
	}

	public List<TeacherEntity> getTeachers()
	{
		return teachers;
	}

	public List<AcademicYearEntity> getAcademicYears()
	{
		return academicYears;
	}
}