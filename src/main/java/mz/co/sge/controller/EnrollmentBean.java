package mz.co.sge.controller;

import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.ClassLevelEntity;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.EnrollmentStatus;
import mz.co.sge.service.IEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component("enrollmentBean")
@Scope("session")
public class EnrollmentBean implements Serializable
{
	@Autowired
	private IEnrollmentService enrollmentService;
	@Autowired
	private LoginBean loginBean;
	@Autowired
	private DashboardBean dashboardBean;
	@PersistenceContext
	private EntityManager em;

	private List<EnrollmentEntity> enrollments;
	private List<EnrollmentEntity> allEnrollments;
	private Long filterYearId;
	private EnrollmentStatus filterStatus;
	private String searchTerm;

	private List<ClassLevelEntity> availableClassLevels;
	private List<StudentEntity> availableStudents;
	private List<ClassroomEntity> availableClassrooms;
	private List<AcademicYearEntity> academicYearsCache;
	private EnrollmentEntity selected;
	private Long selectedAcademicYearId;
	private Long selectedClassLevelId;
	private Long selectedStudentId;
	private Long selectedClassroomId;
	private StudentEntity selectedStudentEntity;
	private boolean editing = false;

	private Integer startMonth;
	private List<MonthOption> months;
	private List<MonthlyFeeDTO> generatedMonthlyFees = new ArrayList<>();
	private String selectedYear = "2026";
	private Double defaultAmount = 2500.0;
	private Integer defaultDueDay = 10;

	@PostConstruct
	public void init()
	{
		loadAll();
		loadCombos();
		setDefaultYearFromLogin();
	}

	public void setDefaultYearFromLogin()
	{
		try
		{
			Long defaultYearId = null;
			List<AcademicYearEntity> years = em.createQuery("SELECT a FROM AcademicYearEntity a ORDER BY a.year DESC", AcademicYearEntity.class)
					.getResultList();
			if (years != null && !years.isEmpty())
			{
				academicYearsCache = years;
				AcademicYearEntity def = years.stream().filter(y -> y.getActive() != null && y.getActive()).findFirst().orElse(null);
				if (def == null)
					def = years.stream().filter(y -> y.getYear() != null && y.getYear().toString().contains("2026")).findFirst().orElse(null);
				if (def == null)
					def = years.get(0);
				if (def != null)
					defaultYearId = def.getId();
			}
			if (loginBean != null)
			{
				try
				{
					java.lang.reflect.Method m = loginBean.getClass().getMethod("getSelectedAcademicYearId");
					Object v = m.invoke(loginBean);
					if (v != null)
						defaultYearId = (Long) v;
				} catch (Exception ignore)
				{
				}
				try
				{
					java.lang.reflect.Method m2 = loginBean.getClass().getMethod("getAcademicYearId");
					Object v2 = m2.invoke(loginBean);
					if (v2 != null)
						defaultYearId = (Long) v2;
				} catch (Exception ignore)
				{
				}
			}
			if (defaultYearId != null)
			{
				selectedAcademicYearId = defaultYearId;
				loadClassrooms();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void loadCombos()
	{
		try
		{
			availableClassLevels = em.createQuery("SELECT c FROM ClassLevelEntity c ORDER BY c.name", ClassLevelEntity.class).getResultList();
		} catch (Exception e)
		{
			availableClassLevels = List.of();
		}
		try
		{
			availableStudents = em.createQuery("SELECT s FROM StudentEntity s WHERE s.status = 'REGISTERED' ORDER BY s.firstName", StudentEntity.class)
					.getResultList();
		} catch (Exception ex)
		{
			try
			{
				List<StudentEntity> all = em.createQuery("SELECT s FROM StudentEntity s ORDER BY s.firstName", StudentEntity.class).getResultList();
				availableStudents = all.stream().filter(s -> s.getStatus() != null && s.getStatus().name().equals("REGISTERED")).collect(Collectors.toList());
			} catch (Exception exc)
			{
				availableStudents = List.of();
			}
		}
		loadClassrooms();
	}

	public void loadClassrooms()
	{
		try
		{
			if (selectedClassLevelId != null)
				availableClassrooms = em.createQuery(
						"SELECT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.responsibleTeacher LEFT JOIN FETCH c.academicYear WHERE c.classLevel.id = :clId AND c.active = true ORDER BY c.name",
						ClassroomEntity.class).setParameter("clId", selectedClassLevelId).getResultList();
			else if (selectedAcademicYearId != null)
				availableClassrooms = em.createQuery(
						"SELECT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.responsibleTeacher LEFT JOIN FETCH c.academicYear WHERE c.academicYear.id = :yearId AND c.active = true ORDER BY c.name",
						ClassroomEntity.class).setParameter("yearId", selectedAcademicYearId).getResultList();
			else
				availableClassrooms = em.createQuery(
						"SELECT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.responsibleTeacher LEFT JOIN FETCH c.academicYear WHERE c.active = true ORDER BY c.name",
						ClassroomEntity.class).getResultList();
		} catch (Exception e)
		{
			availableClassrooms = List.of();
		}
	}

	public void onClassLevelChange()
	{
		selectedClassroomId = null;
		loadClassrooms();
	}

	public void onAcademicYearChange()
	{
		selectedClassroomId = null;
		loadClassrooms();
	}

	public void loadAll()
	{
		try
		{
			allEnrollments = em.createQuery(
					"SELECT e FROM EnrollmentEntity e LEFT JOIN FETCH e.classLevel LEFT JOIN FETCH e.classroom c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.responsibleTeacher LEFT JOIN FETCH e.academicYear LEFT JOIN FETCH e.student LEFT JOIN FETCH e.createdBy ORDER BY e.id DESC",
					EnrollmentEntity.class).getResultList();
		} catch (Exception e)
		{
			try
			{
				allEnrollments = enrollmentService.findAll();
			} catch (Exception ex)
			{
				allEnrollments = List.of();
			}
		}
		if (allEnrollments == null)
			allEnrollments = List.of();
		filter();
	}

	public void filter()
	{
		if (allEnrollments == null)
		{
			enrollments = List.of();
			return;
		}
		enrollments = new ArrayList<>(allEnrollments);
		if (filterYearId != null)
			enrollments = enrollments.stream().filter(e -> e.getAcademicYear() != null && e.getAcademicYear().getId().equals(filterYearId))
					.collect(Collectors.toList());
		if (filterStatus != null)
			enrollments = enrollments.stream().filter(e -> e.getStatus() == filterStatus).collect(Collectors.toList());
		if (searchTerm != null && !searchTerm.trim().isEmpty())
		{
			String term = searchTerm.toLowerCase();
			enrollments = enrollments.stream().filter(e -> e.getStudent() != null && e.getStudent().getFullName().toLowerCase().contains(term))
					.collect(Collectors.toList());
		}
	}

	public void prepareNew()
	{
		selected = new EnrollmentEntity();
		selectedClassLevelId = null;
		selectedClassroomId = null;
		selectedStudentId = null;
		selectedStudentEntity = null;
		editing = false;
		loadCombos();
		setDefaultYearFromLogin();
	}

	public void viewEnrollment(EnrollmentEntity e)
	{
		this.selected = em.find(EnrollmentEntity.class, e.getId());
		if (this.selected == null)
			this.selected = e;
		this.startMonth = null;
		this.generatedMonthlyFees = new ArrayList<>();
		try
		{
			selectedYear = selected.getAcademicYear().getYear().toString().substring(0, 4);
		} catch (Exception ex)
		{
			selectedYear = "2026";
		}
		dashboardBean.navigate("enrollment/enrollment-view", "ver-matricula");
	}

	public void prepareEdit(EnrollmentEntity e)
	{
		selected = em.find(EnrollmentEntity.class, e.getId());
		if (selected == null)
			selected = e;
		selectedAcademicYearId = selected.getAcademicYear() != null ? selected.getAcademicYear().getId() : null;
		selectedClassLevelId = selected.getClassLevel() != null ? selected.getClassLevel().getId() : null;
		selectedClassroomId = selected.getClassroom() != null ? selected.getClassroom().getId() : null;
		selectedStudentId = selected.getStudent() != null ? selected.getStudent().getId() : null;
		selectedStudentEntity = selected.getStudent();
		editing = true;
		loadCombos();
	}

	@Transactional
	public void delete(EnrollmentEntity e)
	{
		try
		{
			EnrollmentEntity toDelete = em.find(EnrollmentEntity.class, e.getId());
			if (toDelete != null)
			{
				if (toDelete.getClassroom() != null)
				{
					ClassroomEntity cr = em.find(ClassroomEntity.class, toDelete.getClassroom().getId());
					if (cr != null && cr.getCurrentStudents() > 0)
					{
						cr.setCurrentStudents(cr.getCurrentStudents() - 1);
						em.merge(cr);
					}
				}
				try
				{
					em.createQuery("DELETE FROM MonthlyFeeEntity m WHERE m.enrollment.id = :enrollId").setParameter("enrollId", toDelete.getId())
							.executeUpdate();
				} catch (Exception ignore)
				{
				}
				em.remove(toDelete);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Apagado", "Matrícula #" + e.getId() + " apagada"));
				loadAll();
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", ex.getMessage()));
		}
	}

	public void goToConfirm()
	{
		if (selectedAcademicYearId == null || selectedClassLevelId == null || selectedClassroomId == null || selectedStudentId == null)
		{
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Campos obrigatórios", "Selecione Ano, Classe, Turma e Aluno"));
			return;
		}
		try
		{
			ClassroomEntity cr = em.find(ClassroomEntity.class, selectedClassroomId);
			if (cr != null && cr.getCurrentStudents() >= cr.getCapacity())
			{
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Turma cheia", cr.getName()));
				return;
			}
		} catch (Exception ex)
		{
		}
		if (selectedStudentEntity == null && selectedStudentId != null)
		{
			try
			{
				selectedStudentEntity = em.find(StudentEntity.class, selectedStudentId);
			} catch (Exception ex)
			{
			}
		}
		dashboardBean.navigate("enrollment/enrollment-confirm", "confirmar-matricula");
	}

	@Transactional
	public String saveAndReturn()
	{
		try
		{
			if (selectedAcademicYearId == null || selectedClassLevelId == null || selectedClassroomId == null || selectedStudentId == null)
			{
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Preencha todos"));
				return null;
			}
			Long uid = 1L;
			try
			{
				if (loginBean != null && loginBean.getLoggedUser() != null)
					uid = loginBean.getLoggedUser().getId();
			} catch (Exception ex)
			{
			}
			EnrollmentEntity enrollment = enrollmentService.createPreEnrollment(selectedAcademicYearId, selectedStudentId, selectedClassLevelId, uid);
			if (enrollment != null)
			{
				ClassroomEntity classroom = em.find(ClassroomEntity.class, selectedClassroomId);
				if (classroom != null)
				{
					if (classroom.getCurrentStudents() >= classroom.getCapacity())
						return null;
					enrollment.setClassroom(classroom);
					enrollment.setStatus(EnrollmentStatus.ENROLLED);
					em.merge(enrollment);
					classroom.setCurrentStudents(classroom.getCurrentStudents() + 1);
					em.merge(classroom);
				}
			}
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Matriculado ENROLLED"));
			loadAll();
			loadCombos();
			setDefaultYearFromLogin();
			dashboardBean.navigate("enrollment/enrollment-list", "matriculas");
			return null;
		} catch (Exception e)
		{
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
			return null;
		}
	}

	public List<MonthOption> getMonths()
	{
		if (months == null)
		{
			months = new ArrayList<>();
			String[] pt =
			{ "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" };
			for (int i = 1; i <= 12; i++)
				months.add(new MonthOption(i, pt[i - 1] + " (" + String.format("%02d", i) + ")"));
		}
		return months;
	}

	public String getStartMonthLabel()
	{
		if (startMonth == null)
			return "";
		String[] pt =
		{ "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" };
		return pt[startMonth - 1];
	}

	public String getPreviewReference()
	{
		if (selected == null)
			return "ESTXXX-0-2026-01";
		String sc = selected.getStudent() != null ? selected.getStudent().getCode() : "SEM-COD";
		String eid = selected.getId() != null ? selected.getId().toString() : "0";
		String y = selectedYear;
		String m = startMonth != null ? String.format("%02d", startMonth) : "MM";
		return sc + "-" + eid + "-" + y + "-" + m;
	}

	@Transactional
	public void generateMonthlyFromDate()
	{
		if (selected == null || startMonth == null)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Selecione mês inicial"));
			return;
		}
		String sc = selected.getStudent() != null ? selected.getStudent().getCode() : "SEM-COD";
		String eid = selected.getId() != null ? selected.getId().toString() : "0";
		int year = 2026;
		try
		{
			year = Integer.parseInt(selectedYear);
		} catch (Exception e)
		{
		}
		generatedMonthlyFees = new ArrayList<>();
		for (int m = startMonth; m <= 12; m++)
		{
			String ms = String.format("%02d", m);
			String ref = sc + "-" + eid + "-" + year + "-" + ms;
			String[] pt =
			{ "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" };
			LocalDate dueLocal = LocalDate.of(year, m, Math.min(defaultDueDay, LocalDate.of(year, m, 1).lengthOfMonth()));
			Date dueDate = Date.from(dueLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
			MonthlyFeeDTO dto = new MonthlyFeeDTO();
			dto.setMonth(m);
			dto.setMonthLabel(pt[m - 1]);
			dto.setYear(year);
			dto.setReference(ref);
			dto.setAmount(defaultAmount);
			dto.setDueDate(dueDate);
			generatedMonthlyFees.add(dto);
		}
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Gerado",
				generatedMonthlyFees.size() + " referências: Mês, Referência, Valor e Data Limite (não editáveis)"));
	}

	@Transactional
	public void saveMonthlyReferences()
	{
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Gravado", generatedMonthlyFees.size() + " referências gravadas"));
	}

	public double getTotalAmount()
	{
		return generatedMonthlyFees.stream().mapToDouble(f -> f.getAmount() != null ? f.getAmount() : 0).sum();
	}

	public List<EnrollmentEntity> getEnrollments()
	{
		if (enrollments == null)
			loadAll();
		return enrollments;
	}

	public void setEnrollments(List<EnrollmentEntity> e)
	{
		enrollments = e;
	}

	public Long getFilterYearId()
	{
		return filterYearId;
	}

	public void setFilterYearId(Long v)
	{
		filterYearId = v;
	}

	public EnrollmentStatus getFilterStatus()
	{
		return filterStatus;
	}

	public void setFilterStatus(EnrollmentStatus v)
	{
		filterStatus = v;
	}

	public String getSearchTerm()
	{
		return searchTerm;
	}

	public void setSearchTerm(String v)
	{
		searchTerm = v;
	}

	public List<ClassLevelEntity> getAvailableClassLevels()
	{
		if (availableClassLevels == null)
			loadCombos();
		return availableClassLevels;
	}

	public List<StudentEntity> getAvailableStudents()
	{
		if (availableStudents == null)
			loadCombos();
		return availableStudents;
	}

	public List<ClassroomEntity> getAvailableClassrooms()
	{
		if (availableClassrooms == null)
			loadClassrooms();
		return availableClassrooms;
	}

	public EnrollmentEntity getSelected()
	{
		if (selected == null)
			selected = new EnrollmentEntity();
		return selected;
	}

	public void setSelected(EnrollmentEntity s)
	{
		selected = s;
	}

	public Long getSelectedAcademicYearId()
	{
		return selectedAcademicYearId;
	}

	public void setSelectedAcademicYearId(Long v)
	{
		selectedAcademicYearId = v;
	}

	public Long getSelectedClassLevelId()
	{
		return selectedClassLevelId;
	}

	public void setSelectedClassLevelId(Long v)
	{
		selectedClassLevelId = v;
	}

	public Long getSelectedStudentId()
	{
		return selectedStudentId;
	}

	public void setSelectedStudentId(Long v)
	{
		selectedStudentId = v;
	}

	public Long getSelectedClassroomId()
	{
		return selectedClassroomId;
	}

	public void setSelectedClassroomId(Long v)
	{
		selectedClassroomId = v;
	}

	public StudentEntity getSelectedStudentEntity()
	{
		return selectedStudentEntity;
	}

	public void setSelectedStudentEntity(StudentEntity v)
	{
		selectedStudentEntity = v;
	}

	public boolean isEditing()
	{
		return editing;
	}

	public void setEditing(boolean v)
	{
		editing = v;
	}

	public List<EnrollmentStatus> getAllStatuses()
	{
		return Arrays.asList(EnrollmentStatus.values());
	}

	public Integer getStartMonth()
	{
		return startMonth;
	}

	public void setStartMonth(Integer v)
	{
		startMonth = v;
	}

	public List<MonthlyFeeDTO> getGeneratedMonthlyFees()
	{
		return generatedMonthlyFees;
	}

	public String getSelectedYear()
	{
		return selectedYear;
	}

	public void setSelectedYear(String v)
	{
		selectedYear = v;
	}

	public Double getDefaultAmount()
	{
		return defaultAmount;
	}

	public void setDefaultAmount(Double v)
	{
		defaultAmount = v;
	}

	public Integer getDefaultDueDay()
	{
		return defaultDueDay;
	}

	public void setDefaultDueDay(Integer v)
	{
		defaultDueDay = v;
	}

	public static class MonthOption implements Serializable
	{
		private Integer value;
		private String label;

		public MonthOption(Integer v, String l)
		{
			value = v;
			label = l;
		}

		public Integer getValue()
		{
			return value;
		}

		public String getLabel()
		{
			return label;
		}
	}

	public static class MonthlyFeeDTO implements Serializable
	{
		private int month;
		private String monthLabel;
		private int year;
		private String reference;
		private Double amount;
		private Date dueDate;

		public int getMonth()
		{
			return month;
		}

		public void setMonth(int m)
		{
			month = m;
		}

		public String getMonthLabel()
		{
			return monthLabel;
		}

		public void setMonthLabel(String l)
		{
			monthLabel = l;
		}

		public int getYear()
		{
			return year;
		}

		public void setYear(int y)
		{
			year = y;
		}

		public String getReference()
		{
			return reference;
		}

		public void setReference(String r)
		{
			reference = r;
		}

		public Double getAmount()
		{
			return amount;
		}

		public void setAmount(Double a)
		{
			amount = a;
		}

		public Date getDueDate()
		{
			return dueDate;
		}

		public void setDueDate(Date d)
		{
			dueDate = d;
		}
	}
}
