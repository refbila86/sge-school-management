package mz.co.sge.controller;

import java.io.Serializable;
import java.util.ArrayList;
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
	@Autowired
	private transient DashboardBean dashboardBean; // <--- ADICIONA ISSO PRA NÃO PERDER ESTRUTURA

	private List<StudentEntity> students;
	private List<StudentEntity> filteredStudents;
	private List<GuardianEntity> guardians;
	private List<AcademicYearEntity> academicYears;

	private StudentEntity student = new StudentEntity();
	private StudentEntity selectedStudent;
	private Long selectedGuardianId;
	private Long selectedAcademicYearId;
	private GuardianEntity selectedGuardianEntity; // <--- FALTAVA - CAUSA DO TEU ERRO
	private List<StudentEntity> studentsOfSelectedGuardian = new ArrayList<>();
	private boolean editing = false;
	private String filterTerm;
	private String searchTerm;
	private GuardianEntity quickGuardian = new GuardianEntity();
	Long schoolId = getCurrentSchoolId();
	private AcademicYearEntity currentAcademicYear;

	public StudentBean()
	{
	}

	@PostConstruct
	public void init()
	{
		loadCurrentAcademicYear(); // tem que ser primeiro
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
				{
					yearId = objId instanceof Long ? (Long) objId : Long.valueOf(objId.toString());
				}
			}

			if (yearId != null)
			{
				currentAcademicYear = academicYearService.findByYearId(yearId);
				this.selectedAcademicYearId = yearId;
			} else
			{
				currentAcademicYear = null;
				this.selectedAcademicYearId = null;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			currentAcademicYear = null;
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
			Long currentYearId = getCurrentAcademicYearId();
			Long currentSchoolId = getCurrentSchoolId();

			if (currentYearId == null)
			{
				this.students = new ArrayList<>();
				return;
			}

			List<StudentEntity> baseList;

			if (currentSchoolId != null)
			{
				baseList = studentService.findBySchoolIdAndAcademicYearId(currentSchoolId, currentYearId);
			} else
			{
				baseList = studentService.findByAcademicYearId(currentYearId);
			}

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
			{
				throw new IllegalArgumentException("Selecione o ano letivo no login");
			}

			// GARANTE ANO LETIVO - era isso que faltava
			if (quickGuardian.getAcademicYear() == null)
			{
				AcademicYearEntity year = currentAcademicYear;
				if (year == null || !year.getId().equals(yearId))
				{
					year = academicYearService.findByYearId(yearId);
				}
				quickGuardian.setAcademicYear(year);
			}

			// Se tua entidade Guardian tem school também obrigatório
//			if (quickGuardian.getSchool() == null)
//			{
//				Long schoolId = getCurrentSchoolId();
//				if (schoolId != null)
//				{
//					SchoolEntity sch = new SchoolEntity();
//					sch.setId(schoolId);
//					quickGuardian.setSchool(sch);
//				}
//			}

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

	public void loadGuardians()
	{
		String term = (searchTerm != null && !searchTerm.isBlank()) ? searchTerm : filterTerm;
		List<GuardianEntity> all = guardianService.getAllGuardians();
		if (all == null)
			all = new java.util.ArrayList<>();

		if (term != null && !term.isBlank())
		{
			String t = term.toLowerCase().trim();
			this.guardians = all.stream()
					.filter(g -> (g.getCode() != null && g.getCode().toLowerCase().contains(t))
							|| (g.getFirstName() != null && g.getFirstName().toLowerCase().contains(t))
							|| (g.getLastName() != null && g.getLastName().toLowerCase().contains(t))
							|| (g.getFullName() != null && g.getFullName().toLowerCase().contains(t)) || (g.getPhone() != null && g.getPhone().contains(t))
							|| (g.getRelationship() != null && g.getRelationship().toLowerCase().contains(t)))
					.collect(Collectors.toList());
		} else
		{
			this.guardians = all;
		}
	}

	public void loadAcademicYears()
	{
		this.academicYears = academicYearService.getAllAcademicYears();
	}

	// CHAMADO PELO POPUP QUANDO CRIA ENCARREGADO
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
		this.student = new StudentEntity();
		this.selectedGuardianId = null;
		this.selectedGuardianEntity = null;
		this.selectedAcademicYearId = loginBean != null ? loginBean.getAcademicYearId() : null;
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
		this.selectedAcademicYearId = s.getAcademicYear() != null ? s.getAcademicYear().getId() : getCurrentAcademicYearId();
		this.editing = true;
		onGuardianChange(); // carrega entidade e outros alunos
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

	// CORRIGIDO - SEM REDIRECT PRA NÃO QUEBRAR O LAYOUT
	public void save()
	{
		try
		{
			saveInternal();
			loadStudents();
			addMessage("Sucesso", "Estudante " + student.getFullName() + " gravado com sucesso");
			// VOLTA PRA LISTA MANTENDO ESTRUTURA
			dashboardBean.navigate("student/student-list", "alunos");
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
		}
	}

	// Mantive o saves() mas sem redirect, pra compatibilidade
	public String saves()
	{
		try
		{
			saveInternal();
			loadStudents();
			addMessage(editing ? "Atualizado" : "Criado", "Estudante " + student.getFullName());
			return null; // NÃO FAZ REDIRECT
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
			return null;
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

		Long currentSchoolId = getCurrentSchoolId();
		if (currentSchoolId == null)
		{
			throw new IllegalArgumentException("Sessão expirada: School não selecionado no login. Faça login novamente.");
		}

		GuardianEntity guardian = guardianService.getGuardianById(selectedGuardianId)
				.orElseThrow(() -> new IllegalArgumentException("Encarregado não encontrado: " + selectedGuardianId));

		AcademicYearEntity academicYear = academicYears.stream().filter(ay -> ay.getId().equals(selectedAcademicYearId)).findFirst()
				.orElseGet(() -> academicYearService.getAcademicYearById(selectedAcademicYearId)
						.orElseThrow(() -> new IllegalArgumentException("Ano Letivo não encontrado")));

		student.setGuardian(guardian);
		student.setAcademicYear(academicYear);

		if (student.getSchool() == null || !currentSchoolId.equals(student.getSchool().getId()))
		{
			SchoolEntity sch = new SchoolEntity();
			sch.setId(currentSchoolId);
			student.setSchool(sch);
		}

		if (editing)
			studentService.updateStudent(student);
		else
			studentService.registerStudent(student, selectedGuardianId);
	}

	private void saveInternalGuardian()
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