package mz.co.sge.controller;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.GuardianEntity;
import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.entity.StudentEntity;
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

	private List<StudentEntity> students;
	private List<StudentEntity> filteredStudents;
	private List<GuardianEntity> guardians;
	private List<AcademicYearEntity> academicYears;

	private StudentEntity student = new StudentEntity();
	private StudentEntity selectedStudent;
	private Long selectedGuardianId;
	private Long selectedAcademicYearId;
	private boolean editing = false;
	private String filterTerm;
	private String searchTerm;

	public StudentBean()
	{
	}

	public StudentBean(IStudentService studentService, IGuardianService guardianService, IAcademicYearService academicYearService)
	{
		this.studentService = studentService;
		this.guardianService = guardianService;
		this.academicYearService = academicYearService;
	}

	@PostConstruct
	public void init()
	{
		loadStudents();
		loadGuardians();
		loadAcademicYears();
	}

	public void loadStudents()
	{
		Long currentSchoolId = getCurrentSchoolId();
		// loginBean.getLoggedSchool().getId() ou loginBean.getSchoolId()
		Long currentYearId = getCurrentAcademicYearId(); // loginBean.getAcademicYearId()

		List<StudentEntity> baseList;

		if (currentSchoolId != null && currentYearId != null)
		{
			baseList = studentService.findBySchoolIdAndAcademicYearId(currentSchoolId, currentYearId);
		} else if (currentSchoolId != null)
		{
			baseList = studentService.findBySchoolId(currentSchoolId);
		} else if (currentYearId != null)
		{
			baseList = studentService.findByAcademicYearId(currentYearId);
		} else
		{
			baseList = studentService.getAllStudents();
		}

		// aplica filtro de pesquisa se existir
		String term = (searchTerm != null && !searchTerm.isBlank()) ? searchTerm : filterTerm;
		if (term != null && !term.isBlank())
		{
			String t = term.toLowerCase().trim();
			this.students = baseList.stream()
					.filter(s -> (s.getCode() != null && s.getCode().toLowerCase().contains(t)) || s.getFullName().toLowerCase().contains(t)
							|| (s.getGuardian() != null && s.getGuardian().getFullName().toLowerCase().contains(t))
							|| (s.getEmail() != null && s.getEmail().toLowerCase().contains(t)))
					.collect(Collectors.toList());
		} else
		{
			this.students = baseList;
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

	private Long getCurrentAcademicYearId()
	{
		if (loginBean == null)
			return null;
		return loginBean.getAcademicYearId();
	}

	public void search()
	{
		loadStudents();
	}

	public void filter()
	{
		loadStudents();
	}

	public void loadGuardians()
	{
		Long schoolId = getCurrentSchoolId();
		if (schoolId != null)
			this.guardians = guardianService.getGuardiansBySchool(schoolId);
		else
			this.guardians = guardianService.getAllGuardians();
	}

	public void loadAcademicYears()
	{
		this.academicYears = academicYearService.getAllAcademicYears();
	}

	public void cleanForm()
	{
		this.student = new StudentEntity();
		this.selectedGuardianId = null;
		this.selectedAcademicYearId = loginBean != null ? loginBean.getAcademicYearId() : null;
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
		this.selectedAcademicYearId = s.getAcademicYear() != null ? s.getAcademicYear().getId() : getCurrentAcademicYearId();
		this.editing = true;
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
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Removido", "Estudante removido com sucesso"));
			}
		} catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public String saves()
	{
		try
		{
			saveInternal();
			if (editing)
				addMessage("Atualizado com sucesso", "Estudante " + student.getFullName() + " atualizado");
			else
				addMessage("Criado com sucesso", "Estudante " + student.getFullName() + " cadastrado");
			loadStudents();
			return "/pages/alunos/student-list?faces-redirect=true";
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
			return null;
		}
	}

	public void save()
	{
		try
		{
			saveInternal();
			loadStudents();
			this.student = new StudentEntity();
			this.selectedGuardianId = null;
			this.selectedAcademicYearId = getCurrentAcademicYearId();
			this.editing = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Estudante gravado com sucesso"));
		} catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	private void saveInternal()
	{
		if (selectedGuardianId == null)
			throw new IllegalArgumentException("Deve indicar o encarregado - campo obrigatório.");
		if (selectedAcademicYearId == null)
			selectedAcademicYearId = getCurrentAcademicYearId();
		if (selectedAcademicYearId == null)
			throw new IllegalArgumentException("Deve indicar o Ano Letivo.");

		GuardianEntity guardian = guardianService.getGuardianById(selectedGuardianId)
				.orElseThrow(() -> new IllegalArgumentException("Encarregado não encontrado: " + selectedGuardianId));

		AcademicYearEntity academicYear = academicYears.stream().filter(ay -> ay.getId().equals(selectedAcademicYearId)).findFirst()
				.orElseGet(() -> academicYearService.getAcademicYearById(selectedAcademicYearId)
						.orElseThrow(() -> new IllegalArgumentException("Ano Letivo não encontrado")));

		student.setGuardian(guardian);
		student.setAcademicYear(academicYear);

		// Escola da sessão - isolamento multi-escola
		Long schoolId = getCurrentSchoolId();
		if (schoolId != null)
		{
			if (student.getSchool() == null || !schoolId.equals(student.getSchool().getId()))
			{
				SchoolEntity sch = new SchoolEntity();
				sch.setId(schoolId);
				student.setSchool(sch);
			}
		}

		if (editing)
			studentService.updateStudent(student);
		else
			studentService.registerStudent(student, selectedGuardianId);
	}

	public void deleteEntity(StudentEntity entity)
	{
		try
		{
			studentService.deleteStudent(entity.getId());
			loadStudents();
			addMessage("Removido", "Estudante removido com sucesso");
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
		}
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
		return StudentStatus.values();
	}

	private void addMessage(String summary, String detail)
	{
		addMessage(FacesMessage.SEVERITY_INFO, summary, detail);
	}

	private void addMessage(FacesMessage.Severity severity, String summary, String detail)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
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
