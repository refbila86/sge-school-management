package mz.co.sge.controller;

import java.io.Serializable;
import java.util.ArrayList;
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
import mz.co.sge.entity.SubjectEntity;
import mz.co.sge.entity.UserEntity;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.service.ITeacherService;
import mz.co.sge.service.ISubjectService;
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
	@Autowired
	private ISubjectService subjectService;

	@Inject
	private LoginBean loginBean;
	@Inject
	private DashboardBean dashboardBean;

	private List<TeacherEntity> teachers;
	private List<TeacherEntity> filteredTeachers;
	private List<AcademicYearEntity> academicYears;
	private List<UserEntity> availableUsers;
	private List<SubjectEntity> allSubjects;

	private TeacherEntity teacher = new TeacherEntity();
	private TeacherEntity selected = new TeacherEntity();
	private boolean editing = false;
	private String searchTerm;
	private Long selectedUserId;
	private Long selectedAcademicYearId;
	private Long filterYearId;
	private Long selectedYearId;
	private List<Long> selectedSubjectIds = new ArrayList<>();
	
 

	@PostConstruct
	public void init()
	{
		loadAcademicYears();
		loadAvailableUsers();
		loadAllSubjects();
		loadTeachers();
	}

	public void loadAllSubjects()
	{
		try
		{
			allSubjects = subjectService.findAll();
		} catch (Exception e)
		{
			allSubjects = List.of();
		}
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
		if (teachers == null)
			teachers = new ArrayList<>();
		filteredTeachers = new ArrayList<>(teachers);
	}

	public void loadAll()
	{
		loadTeachers();
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
				users = userService.findBySchoolIdAndAcademicYearId(schoolId, yearId);
			else
				users = userService.findAll();
			availableUsers = users.stream().filter(u -> u.getRole() != null && u.getRole().toString().equalsIgnoreCase("TEACHER")).collect(Collectors.toList());
		} catch (Exception e)
		{
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
		filteredTeachers = new ArrayList<>(teachers);
	}

	public void applyFilter()
	{
		if (searchTerm == null || searchTerm.isBlank())
		{
			filteredTeachers = new ArrayList<>(teachers);
		} else
		{
			String q = searchTerm.toLowerCase();
			filteredTeachers = teachers.stream()
					.filter(t -> (t.getFirstName() != null && t.getFirstName().toLowerCase().contains(q))
							|| (t.getLastName() != null && t.getLastName().toLowerCase().contains(q))
							|| (t.getCode() != null && t.getCode().toLowerCase().contains(q)))
					.collect(Collectors.toList());
		}
	}

	public void clearFilters()
	{
		searchTerm = null;
		filterYearId = null;
		loadTeachers();
	}

	public void prepareNew()
	{
		teacher = new TeacherEntity();
		selected = new TeacherEntity();
		editing = false;
		selectedUserId = null;
		selectedAcademicYearId = loginBean.getAcademicYearId();
		selectedYearId = selectedAcademicYearId;
		selectedSubjectIds = new ArrayList<>();
	}

	public void prepareEdit(TeacherEntity t)
	{
		TeacherEntity full = teacherService.findById(t.getId());
		if (full != null)
			t = full;
		selected = t;
		teacher = t;
		editing = true;
		selectedUserId = t.getUser() != null ? t.getUser().getId() : null;
		selectedAcademicYearId = t.getAcademicYear() != null ? t.getAcademicYear().getId() : null;
		selectedYearId = selectedAcademicYearId;
		selectedSubjectIds = t.getSubjects() != null ? t.getSubjects().stream().map(SubjectEntity::getId).collect(Collectors.toList()) : new ArrayList<>();
	}

	public void cleanForm()
	{
		teacher = new TeacherEntity();
		selected = new TeacherEntity();
		editing = false;
		selectedUserId = null;
		selectedAcademicYearId = loginBean.getAcademicYearId();
		selectedYearId = selectedAcademicYearId;
		selectedSubjectIds = new ArrayList<>();
	}

	public void save()
	{
		try
		{
			TeacherEntity toSave = editing ? teacher : selected;
			if (toSave == null)
				toSave = new TeacherEntity();

			if (selectedUserId != null)
			{
				UserEntity u = userService.findById(selectedUserId).orElse(null);
				toSave.setUser(u);
			}
			Long yId = selectedYearId != null ? selectedYearId : (selectedAcademicYearId != null ? selectedAcademicYearId : loginBean.getAcademicYearId());
			if (yId != null)
			{
				AcademicYearEntity ay = academicYearService.getAcademicYearById(yId).orElse(null);
				if (ay == null)
					ay = academicYearService.findByYearId(yId);
				toSave.setAcademicYear(ay);
			}
			if (toSave.getCode() == null || toSave.getCode().isBlank())
			{
				toSave.setCode("PROF-" + (System.currentTimeMillis() % 10000));
			}
			List<SubjectEntity> subs = new ArrayList<>();
			if (selectedSubjectIds != null)
			{
				for (Long sid : selectedSubjectIds)
				{
					SubjectEntity s = subjectService.findById(sid);
					if (s != null)
						subs.add(s);
				}
			}
			toSave.setSubjects(subs);

			teacherService.save(toSave);
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
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Removido", t.getFirstName()));
		} catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}
	
	public void prepareView(TeacherEntity t) {
	    try {
	        selected = teacherService.findById(t.getId()); // traz com JOIN FETCH subjects
	        if (selected == null) selected = t;
	    } catch (Exception e) {
	        selected = t;
	    }
	}

	// getters/setters + novos
	public List<TeacherEntity> getTeachers()
	{
		return teachers;
	}

	public void setTeachers(List<TeacherEntity> teachers)
	{
		this.teachers = teachers;
	}

	public List<TeacherEntity> getFilteredTeachers()
	{
		return filteredTeachers;
	}

	public void setFilteredTeachers(List<TeacherEntity> filteredTeachers)
	{
		this.filteredTeachers = filteredTeachers;
	}

	public TeacherEntity getTeacher()
	{
		return teacher;
	}

	public void setTeacher(TeacherEntity teacher)
	{
		this.teacher = teacher;
	}

	public TeacherEntity getSelected()
	{
		return selected;
	}

	public void setSelected(TeacherEntity selected)
	{
		this.selected = selected;
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

	public Long getFilterYearId()
	{
		return filterYearId;
	}

	public void setFilterYearId(Long filterYearId)
	{
		this.filterYearId = filterYearId;
	}

	public Long getSelectedYearId()
	{
		return selectedYearId;
	}

	public void setSelectedYearId(Long selectedYearId)
	{
		this.selectedYearId = selectedYearId;
	}

	public List<AcademicYearEntity> getAcademicYears()
	{
		return academicYears;
	}

	public List<UserEntity> getAvailableUsers()
	{
		return availableUsers;
	}

	public List<SubjectEntity> getAllSubjects()
	{
		return allSubjects;
	}

	public List<Long> getSelectedSubjectIds()
	{
		return selectedSubjectIds;
	}

	public void setSelectedSubjectIds(List<Long> selectedSubjectIds)
	{
		this.selectedSubjectIds = selectedSubjectIds;
	}
}