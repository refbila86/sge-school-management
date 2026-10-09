package mz.co.sge.controller;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import mz.co.sge.entity.TeacherEntity;
import mz.co.sge.entity.UserEntity;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.service.ITeacherService;
import mz.co.sge.service.IUserService;
import mz.co.sge.service.IAcademicYearService;

@Named
@ViewScoped
public class TeacherBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Autowired
	private ITeacherService teacherService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IAcademicYearService academicYearService;

	@Inject
	private LoginBean loginBean;
	@Inject
	private DashboardBean dashboardBean;

	private List<TeacherEntity> teachers;
	private List<AcademicYearEntity> academicYears;
	private List<UserEntity> availableUsers;

	private TeacherEntity teacher = new TeacherEntity();
	private boolean editing = false;
	private String searchTerm;
	private Long selectedUserId;
	private Long selectedAcademicYearId;

	@PostConstruct
	public void init()
	{
		loadAcademicYears();
		loadAvailableUsers();
		loadTeachers();
	}

	public void loadTeachers()
	{
		try
		{
			Long yearId = loginBean.getAcademicYearId();
			if (yearId != null)
				teachers = teacherService.findByAcademicYear(yearId);
			else
				teachers = teacherService.findAll();
		} catch (Exception e)
		{
			teachers = teacherService.findAll();
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

	public void loadAvailableUsers()
	{
		try
		{
			Long schoolId = loginBean.getSchoolId();
			Long yearId = loginBean.getAcademicYearId();

			List<UserEntity> users;
			if (schoolId != null)
			{
				users = userService.findBySchoolIdAndAcademicYearId(schoolId, yearId);
			} else
			{
				users = userService.findAll();
			}

			// filtra só TEACHER e que ainda não tem professor associado
			availableUsers = users.stream().filter(u -> u.getRole() != null && u.getRole().toString().equalsIgnoreCase("TEACHER")).collect(Collectors.toList());

		} catch (Exception e)
		{
			e.printStackTrace();
			availableUsers = List.of();
		}
	}

	public void search()
	{
		if (searchTerm == null || searchTerm.isBlank())
		{
			loadTeachers();
			return;
		}
		teachers = teacherService.search(searchTerm, loginBean.getAcademicYearId());
	}

	public void cleanForm()
	{
		teacher = new TeacherEntity();
		editing = false;
		selectedUserId = null;
		selectedAcademicYearId = loginBean.getAcademicYearId();
	}

	public void save()
	{
		try
		{
			if (selectedUserId != null)
			{
				UserEntity u = userService.findById(selectedUserId).orElse(null);
				teacher.setUser(u);
			}
			Long yId = selectedAcademicYearId != null ? selectedAcademicYearId : loginBean.getAcademicYearId();
			if (yId != null)
			{
				AcademicYearEntity ay = academicYearService.getAcademicYearById(yId).orElse(null);
				if (ay == null)
					ay = academicYearService.findByYearId(yId);
				teacher.setAcademicYear(ay);
			}
			if (teacher.getCode() == null || teacher.getCode().isBlank())
			{
				teacher.setCode("PROF-" + (System.currentTimeMillis() % 10000));
			}
			teacherService.save(teacher);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Professor salvo"));
			loadTeachers();
			dashboardBean.navigate("teacher/teacher-list", "professores");
		} catch (Exception e)
		{
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public void delete(TeacherEntity t)
	{
		try
		{
			teacherService.delete(t.getId());
			loadTeachers();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Removido", t.getName()));
		} catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	// getters/setters
	public List<TeacherEntity> getTeachers()
	{
		return teachers;
	}

	public void setTeachers(List<TeacherEntity> teachers)
	{
		this.teachers = teachers;
	}

	public TeacherEntity getTeacher()
	{
		return teacher;
	}

	public void setTeacher(TeacherEntity teacher)
	{
		this.teacher = teacher;
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

	public Long getSelectedUserId()
	{
		return selectedUserId;
	}

	public void setSelectedUserId(Long id)
	{
		this.selectedUserId = id;
	}

	public Long getSelectedAcademicYearId()
	{
		return selectedAcademicYearId;
	}

	public void setSelectedAcademicYearId(Long id)
	{
		this.selectedAcademicYearId = id;
	}

	public List<AcademicYearEntity> getAcademicYears()
	{
		return academicYears;
	}

	public List<UserEntity> getAvailableUsers()
	{
		return availableUsers;
	}
}