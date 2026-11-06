package mz.co.sge.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.GuardianEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.MunicipalDistrict;
import mz.co.sge.enumes.Province;
import mz.co.sge.enumes.StudentStatus;
import mz.co.sge.service.IAcademicYearService;
import mz.co.sge.service.IGuardianService;
import mz.co.sge.service.IStudentService;

@Component("studentBean")
@Scope("view")
public class StudentBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Autowired
	private transient IStudentService studentService;
	@Autowired
	private transient IGuardianService guardianService;
	@Autowired
	private transient IAcademicYearService academicYearService;
	@Autowired
	private transient LoginBean loginBean;
	@Autowired
	private transient DashboardBean dashboardBean;

	private List<StudentEntity> students;
	private List<StudentEntity> filteredStudents;
	private List<GuardianEntity> guardians;
	private List<AcademicYearEntity> academicYears;

	private StudentEntity student = newStudent();;
	private StudentEntity selectedStudent;
	private Long selectedGuardianId;
	private GuardianEntity selectedGuardianEntity;
	private List<StudentEntity> studentsOfSelectedGuardian = new ArrayList<>();
	private boolean editing = false;
	private String filterTerm;
	private String searchTerm;
	private GuardianEntity quickGuardian = new GuardianEntity();
	private AcademicYearEntity currentAcademicYear;
	private Long selectedAcademicYearId;

	@PostConstruct
	public void init()
	{
		loadCurrentAcademicYear();
		loadAcademicYears();
		loadGuardians();
		loadStudents();
	}

	public void loadCurrentAcademicYear()
	{
		try
		{
			Long yearId = getCurrentAcademicYearId();
			if (yearId == null)
			{
				Object objId = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("selectedAcademicYearId");
				if (objId != null)
					yearId = objId instanceof Long ? (Long) objId : Long.valueOf(objId.toString());
			}
			if (yearId != null)
			{
				currentAcademicYear = academicYearService.findByYearId(yearId);
				this.selectedAcademicYearId = yearId;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public AcademicYearEntity getCurrentAcademicYear()
	{
		return currentAcademicYear;
	}

	public int getCountStudentsByGuardian()
	{
		if (selectedStudent != null && selectedStudent.getGuardian() != null)
		{
			return (int) studentService.countByGuardianId(selectedStudent.getGuardian().getId());
		}
		return 0;
	}

	public void loadStudents()
	{
		try
		{
			// AGORA ALUNO É GLOBAL - não filtra mais por escola/ano na tabela students
			List<StudentEntity> baseList = studentService.getAllStudents();

			if (baseList == null)
				baseList = new ArrayList<>();

			String term = (searchTerm != null && !searchTerm.isBlank()) ? searchTerm : filterTerm;
			if (term != null && !term.isBlank())
			{
				String t = term.toLowerCase().trim();
				this.students = baseList.stream().filter(s ->
				{
					boolean matchCode = s.getCode() != null && s.getCode().toLowerCase().contains(t);
					boolean matchName = s.getFullName() != null && s.getFullName().toLowerCase().contains(t);
					boolean matchGuardian = s.getGuardian() != null && s.getGuardian().getFullName() != null
							&& s.getGuardian().getFullName().toLowerCase().contains(t);
					boolean matchEmail = s.getEmail() != null && s.getEmail().toLowerCase().contains(t);
					return matchCode || matchName || matchGuardian || matchEmail;
				}).collect(Collectors.toList());
			} else
			{
				this.students = baseList;
			}
			System.out.println("Alunos carregados: " + this.students.size());
		} catch (Exception e)
		{
			e.printStackTrace();
			this.students = new ArrayList<>();
		}
	}

	private Long getCurrentSchoolId()
	{
		if (loginBean == null)
			return null;
		if (loginBean.getLoggedSchool() != null && loginBean.getLoggedSchool().getId() != null)
			return loginBean.getLoggedSchool().getId();
		return loginBean.getSchoolId();
	}

	public GuardianEntity getQuickGuardian()
	{
		if (quickGuardian == null)
			quickGuardian = new GuardianEntity();
		return quickGuardian;
	}

	public void setQuickGuardian(GuardianEntity quickGuardian)
	{
		this.quickGuardian = quickGuardian;
	}

	public void quickSaveForStudent()
	{
		try
		{
			Long yearId = getCurrentAcademicYearId();
			if (yearId == null)
				throw new IllegalArgumentException("Selecione o ano letivo no login");

			if (quickGuardian.getAcademicYear() == null)
			{
				AcademicYearEntity year = currentAcademicYear;
				if (year == null || !year.getId().equals(yearId))
					year = academicYearService.findByYearId(yearId);
				quickGuardian.setAcademicYear(year);
			}

			guardianService.createGuardian(quickGuardian);
			this.guardians = guardianService.getAllGuardians();
			this.selectedGuardianId = quickGuardian.getId();
			this.selectedGuardianEntity = quickGuardian;
			onGuardianChange();
			this.quickGuardian = new GuardianEntity();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Encarregado criado com sucesso!"));
		} catch (Exception e)
		{
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro: " + e.getMessage(), null));
		}
	}

	private Long getCurrentAcademicYearId()
	{
		if (loginBean != null && loginBean.getAcademicYearId() != null)
			return loginBean.getAcademicYearId();
		try
		{
			Object obj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("selectedAcademicYearId");
			if (obj != null)
				return obj instanceof Long ? (Long) obj : Long.valueOf(obj.toString());
		} catch (Exception ignored)
		{
		}
		return selectedAcademicYearId;
	}

	public void search()
	{
		loadStudents();
	}

	public void filter()
	{
		loadStudents();
	}

	private StudentEntity newStudent()
	{
		StudentEntity s = new StudentEntity();
		s.setStatus(StudentStatus.PENDING_ENROLLMENT);
		return s;
	}

	public void loadGuardians()
	{
		String term = (searchTerm != null && !searchTerm.isBlank()) ? searchTerm : filterTerm;
		List<GuardianEntity> all = guardianService.getAllGuardians();
		if (all == null)
			all = new ArrayList<>();
		if (term != null && !term.isBlank())
		{
			String t = term.toLowerCase().trim();
			this.guardians = all.stream().filter(g -> (g.getCode() != null && g.getCode().toLowerCase().contains(t))
					|| (g.getFullName() != null && g.getFullName().toLowerCase().contains(t))).collect(Collectors.toList());
		} else
		{
			this.guardians = all;
		}
	}

	public void loadAcademicYears()
	{
		this.academicYears = academicYearService.getAllAcademicYears();
	}

	public void refreshGuardians()
	{
		loadGuardians();
	}

	public void onGuardianChange()
	{
		if (selectedGuardianId != null)
		{
			selectedGuardianEntity = guardianService.getGuardianById(selectedGuardianId).orElse(null);
			studentsOfSelectedGuardian = studentService.getStudentsByGuardianId(selectedGuardianId).stream()
					.filter(s -> editing ? (student.getId() == null || !s.getId().equals(student.getId())) : true).collect(Collectors.toList());
		} else
		{
			selectedGuardianEntity = null;
			studentsOfSelectedGuardian = new ArrayList<>();
		}
	}

	public void cleanForm()
	{
		this.student = newStudent();
		this.selectedGuardianId = null;
		this.selectedGuardianEntity = null;
		this.studentsOfSelectedGuardian = new ArrayList<>();
		this.editing = false;
	}

	public String getYearLabel(Long id)
	{
		if (id == null)
			return "Geral";
		if (academicYears == null)
			return id.toString();
		return academicYears.stream().filter(ay -> ay.getId().equals(id)).map(ay -> String.valueOf(ay.getYear())).findFirst().orElse(id.toString());
	}

	public String getGuardianLabel(Long guardianId)
	{
		if (guardianId == null)
			return "Sem Encarregado";
		if (guardians == null)
			return guardianId.toString();
		return guardians.stream().filter(g -> g.getId().equals(guardianId)).map(GuardianEntity::getFullName).findFirst().orElse(guardianId.toString());
	}

	public void prepareNew()
	{
		cleanForm();
	}

	public void prepareEdit(StudentEntity s)
	{
		this.student = s;
		this.selectedGuardianId = s.getGuardian() != null ? s.getGuardian().getId() : null;
		this.editing = true;
		onGuardianChange();
	}

	public void prepareView(StudentEntity s)
	{
		this.selectedStudent = s;
	}

	public void prepareDelete(StudentEntity s)
	{
		this.selectedStudent = s;
	}

	public void delete()
	{
		try
		{
			if (selectedStudent != null && selectedStudent.getId() != null)
			{
				studentService.deleteStudent(selectedStudent.getId());
				loadStudents();
				addMessage("Removido", "Estudante removido com sucesso");
			}
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
		}
	}

	public void save()
	{
		try
		{
			saveInternal();
			loadStudents();
			addMessage("Sucesso", "Estudante " + student.getFullName() + " (" + student.getCode() + ") gravado");
			dashboardBean.navigate("student/student-list", "alunos");
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
		}
	}

	public String saves()
	{
		try
		{
			saveInternal();
			loadStudents();
			addMessage(editing ? "Atualizado" : "Criado", "Estudante " + student.getFullName() + " - " + student.getCode());
			return null;
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
			return null;
		}
	}

	private void saveInternal()
	{
		if (selectedGuardianId != null)
		{
			GuardianEntity guardian = guardianService.getGuardianById(selectedGuardianId).orElse(null);
			student.setGuardian(guardian);
		} else
		{
			student.setGuardian(null);
		}
		if (editing)
			studentService.updateStudent(student);
		else
			studentService.registerStudent(student, selectedGuardianId);
	}

	public List<StudentEntity> getStudentsOfSelectedGuardian()
	{
		if (selectedGuardianId == null)
			return List.of();
		return studentService.getStudentsByGuardianId(selectedGuardianId).stream()
				.filter(s -> editing ? (student.getId() == null || !s.getId().equals(student.getId())) : true).collect(Collectors.toList());
	}

	public StudentStatus[] getStatusValues()
	{
		if (editing)
			return StudentStatus.values();
		return new StudentStatus[]
		{ StudentStatus.PENDING_ENROLLMENT };
	}

	private void addMessage(String summary, String detail)
	{
		addMessage(FacesMessage.SEVERITY_INFO, summary, detail);
	}

	private void addMessage(FacesMessage.Severity severity, String summary, String detail)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
	}

	public void handlePhotoUpload(FileUploadEvent event)
	{
		UploadedFile file = event.getFile();
		student.setPhoto(file.getContent());
		student.setPhotoContentType(file.getContentType());
	}

	public Province[] getProvinces()
	{
		return Province.values();
	}

	public MunicipalDistrict[] getDistricts()
	{
		return MunicipalDistrict.values();
	}

	public String getSelectedPhotoSrc()
	{
		if (selectedStudent == null || selectedStudent.getPhoto() == null || selectedStudent.getPhoto().length == 0)
			return null;
		String type = selectedStudent.getPhotoContentType() != null ? selectedStudent.getPhotoContentType() : "image/jpeg";
		return "data:" + type + ";base64," + Base64.getEncoder().encodeToString(selectedStudent.getPhoto());
	}

	public void removePhoto()
	{
		student.setPhoto(null);
		student.setPhotoContentType(null);
	}

	public String getPhotoSrc()
	{
		if (student == null || student.getPhoto() == null || student.getPhoto().length == 0)
			return null;
		String type = student.getPhotoContentType() != null ? student.getPhotoContentType() : "image/jpeg";
		return "data:" + type + ";base64," + Base64.getEncoder().encodeToString(student.getPhoto());
	}

	// Getters / Setters
	public List<StudentEntity> getStudents()
	{
		return students;
	}

	public void setStudents(List<StudentEntity> students)
	{
		this.students = students;
	}

	public List<StudentEntity> getFilteredStudents()
	{
		return filteredStudents;
	}

	public void setFilteredStudents(List<StudentEntity> filteredStudents)
	{
		this.filteredStudents = filteredStudents;
	}

	public List<GuardianEntity> getGuardians()
	{
		return guardians;
	}

	public void setGuardians(List<GuardianEntity> guardians)
	{
		this.guardians = guardians;
	}

	public List<AcademicYearEntity> getAcademicYears()
	{
		return academicYears;
	}

	public void setAcademicYears(List<AcademicYearEntity> academicYears)
	{
		this.academicYears = academicYears;
	}

	public StudentEntity getStudent()
	{
		return student;
	}

	public void setStudent(StudentEntity student)
	{
		this.student = student;
	}

	public StudentEntity getSelectedStudent()
	{
		return selectedStudent;
	}

	public void setSelectedStudent(StudentEntity selectedStudent)
	{
		this.selectedStudent = selectedStudent;
	}

	public Long getSelectedGuardianId()
	{
		return selectedGuardianId;
	}

	public void setSelectedGuardianId(Long selectedGuardianId)
	{
		this.selectedGuardianId = selectedGuardianId;
	}

	public Long getSelectedAcademicYearId()
	{
		return selectedAcademicYearId;
	}

	public void setSelectedAcademicYearId(Long selectedAcademicYearId)
	{
		this.selectedAcademicYearId = selectedAcademicYearId;
	}

	public GuardianEntity getSelectedGuardianEntity()
	{
		return selectedGuardianEntity;
	}

	public void setSelectedGuardianEntity(GuardianEntity selectedGuardianEntity)
	{
		this.selectedGuardianEntity = selectedGuardianEntity;
	}

	public boolean isEditing()
	{
		return editing;
	}

	public void setEditing(boolean editing)
	{
		this.editing = editing;
	}

	public String getFilterTerm()
	{
		return filterTerm;
	}

	public void setFilterTerm(String filterTerm)
	{
		this.filterTerm = filterTerm;
	}

	public String getSearchTerm()
	{
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm)
	{
		this.searchTerm = searchTerm;
	}

	public int getTotalStudentsCount()
	{
		return students != null ? students.size() : 0;
	}

	public String getTotalStudentsFormatted()
	{
		int total = getTotalStudentsCount();
		if (total >= 1000)
			return String.format("%.1fk", total / 1000.0).replace(",", ".");
		return String.valueOf(total);
	}
}