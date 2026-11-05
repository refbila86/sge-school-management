package mz.co.sge.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import mz.co.sge.dto.MonthlyFeeDTO;
import mz.co.sge.dto.PaymentDTO;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.ClassLevelEntity;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.MonthlyFeeEntity;
import mz.co.sge.entity.PaymentEntity;
import mz.co.sge.entity.PaymentItemEntity;
import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.EnrollmentStatus;
import mz.co.sge.enumes.EnrollmentType;
import mz.co.sge.service.IEnrollmentService;
import mz.co.sge.service.IMonthlyFeeService;
import mz.co.sge.service.IStudentService;

@Component("enrollmentBean")
@Scope("session")
public class EnrollmentBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(EnrollmentBean.class);

	@Autowired
	private IEnrollmentService enrollmentService;

	@Autowired
	IStudentService studentService;

	@Autowired
	IMonthlyFeeService monthlyFeeService;

	@Autowired
	private LoginBean loginBean;

	@Autowired
	private DashboardBean dashboardBean;

	@PersistenceContext
	private EntityManager em;

	// LISTA
	private List<EnrollmentEntity> enrollments;
	private List<EnrollmentEntity> allEnrollments;
	private Long filterYearId;
	private Long loadedYearId;
	private EnrollmentStatus filterStatus;
	private String searchTerm;
	private List<MonthlyFeeEntity> printFees = new ArrayList<>();

	public List<MonthlyFeeEntity> getPrintFees()
	{
		return printFees;
	}

	// COMBOS
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

	// PAGAMENTOS
	private List<PaymentDTO> studentPayments = new ArrayList<>();
	private List<PaymentDTO> selectedPayments = new ArrayList<>();
	private boolean hasValidPayment = false;
	private String selectedPaymentReference = "";

	// MENSALIDADES
	private Integer startMonth;
	private Integer endMonth; // <-- NOVO
	private List<MonthOption> months;
	private List<MonthlyFeeDTO> generatedMonthlyFees = new ArrayList<>();
	private String selectedYear = "2026";
	private BigDecimal defaultAmount = new BigDecimal("2500.00");
	private Integer defaultDueDay = 10;

	// PAGAMENTOS FLAGS
	private boolean hasPaidEnrollment = false;
	private boolean hasPaidRegistration = false;
	private boolean hasPaidRenewal = false;
	private EnrollmentType currentEnrollmentType;

	private ClassLevelEntity selectedClassLevelEntity;
	private ClassroomEntity selectedClassroomEntity;
	private AcademicYearEntity selectedAcademicYearEntity;

	@PostConstruct
	public void init()
	{
		loadAll();
		setDefaultYearFromLogin();
		loadCombos();
	}

	public void setDefaultYearFromLogin()
	{
		try
		{
			Long defaultYearId = null;
			List<AcademicYearEntity> years = new ArrayList<>();
			if (loginBean != null && loginBean.getAcademicYearId() != null)
			{
				defaultYearId = loginBean.getAcademicYearId();
				AcademicYearEntity fromLogin = em.find(AcademicYearEntity.class, defaultYearId);
				if (fromLogin != null)
				{
					years = List.of(fromLogin);
					selectedYear = fromLogin.getYear();
				}
			}
			if (years.isEmpty())
				years = em.createQuery("SELECT a FROM AcademicYearEntity a ORDER BY a.active DESC, a.year DESC", AcademicYearEntity.class).getResultList();
			if (years.isEmpty())
				return;
			academicYearsCache = years;
			if (defaultYearId == null)
			{
				AcademicYearEntity activeYear = years.stream().filter(y -> Boolean.TRUE.equals(y.getActive())).findFirst().orElse(years.get(0));
				defaultYearId = activeYear.getId();
				selectedYear = activeYear.getYear();
			}
			selectedAcademicYearId = defaultYearId;
			loadClassrooms();
		} catch (Exception e)
		{
			log.error("Erro ao definir ano padrão", e);
		}
	}

	// ========= NOVO loadCombos COM REGRA =========
	public void loadCombos()
	{
		try
		{
			availableClassLevels = enrollmentService.findAllClassLevels();
			Long yearId = selectedAcademicYearId;
			if (yearId == null && loginBean != null)
				yearId = loginBean.getAcademicYearId();
			if (yearId == null && academicYearsCache != null && !academicYearsCache.isEmpty())
				yearId = academicYearsCache.get(0).getId();

			if (yearId == null)
			{
				availableStudents = em.createQuery("SELECT s FROM StudentEntity s ORDER BY s.firstName", StudentEntity.class).setMaxResults(200)
						.getResultList();
				loadClassrooms();
				return;
			}

			// QUEM JÁ ESTÁ NESTE ANO
			List<Long> alreadyEnrolledTmp;
			try
			{
				alreadyEnrolledTmp = em.createQuery("SELECT e.student.id FROM EnrollmentEntity e WHERE e.academicYear.id = :yearId", Long.class)
						.setParameter("yearId", yearId).getResultList();
			} catch (Exception ex)
			{
				alreadyEnrolledTmp = List.of();
			}
			final List<Long> alreadyEnrolledThisYear = alreadyEnrolledTmp;

			// ANO ANTERIOR
			Long previousYearId = findPreviousYearId(yearId);

			List<Long> candidates;
			if (previousYearId == null)
			{
				// PRIMEIRO ANO: alunos que NUNCA foram matriculados
				candidates = em.createQuery("SELECT s.id FROM StudentEntity s WHERE s.id NOT IN (SELECT e.student.id FROM EnrollmentEntity e)", Long.class)
						.getResultList();
			} else
			{
				List<Long> newOnes = em
						.createQuery("SELECT s.id FROM StudentEntity s WHERE s.id NOT IN (SELECT e.student.id FROM EnrollmentEntity e)", Long.class)
						.getResultList();
				List<Long> fromPrevious = em.createQuery("SELECT e.student.id FROM EnrollmentEntity e WHERE e.academicYear.id = :prevId", Long.class)
						.setParameter("prevId", previousYearId).getResultList();
				candidates = new ArrayList<>();
				candidates.addAll(newOnes);
				candidates.addAll(fromPrevious);
				candidates = candidates.stream().distinct().toList();
			}

			candidates = candidates.stream().filter(id -> !alreadyEnrolledThisYear.contains(id)).distinct().toList();

			if (candidates.isEmpty())
			{
				availableStudents = List.of();
				loadClassrooms();
				return;
			}

			// SÓ COM PAGAMENTO PAGO NO ANO
			List<Long> withPayment;
			try
			{
				withPayment = em.createQuery(
						"SELECT DISTINCT p.student.id FROM PaymentEntity p WHERE p.student.id IN :ids AND p.academicYear.id = :yearId AND p.status = 'PAGO'",
						Long.class).setParameter("ids", candidates).setParameter("yearId", yearId).getResultList();
			} catch (Exception ex)
			{
				// se não tem status, pega todos candidatos
				withPayment = candidates;
			}

			if (withPayment.isEmpty())
			{
				availableStudents = List.of();
			} else
			{
				availableStudents = em.createQuery("SELECT DISTINCT s FROM StudentEntity s WHERE s.id IN :ids ORDER BY s.firstName", StudentEntity.class)
						.setParameter("ids", withPayment.stream().distinct().toList()).getResultList();
			}

			studentPayments = new ArrayList<>();
			hasValidPayment = false;

		} catch (Exception e)
		{
			log.error("Erro load students", e);
			e.printStackTrace();
			availableStudents = em.createQuery("SELECT s FROM StudentEntity s ORDER BY s.firstName", StudentEntity.class).setMaxResults(200).getResultList();
		}
		loadClassrooms();
	}

	public void loadComboss()
	{
		try
		{
			availableClassLevels = enrollmentService.findAllClassLevels();
		} catch (Exception e)
		{
			availableClassLevels = List.of();
		}

		try
		{
			Long yearId = selectedAcademicYearId;
			if (yearId == null && loginBean != null)
				yearId = loginBean.getAcademicYearId();
			if (yearId == null && academicYearsCache != null && !academicYearsCache.isEmpty())
				yearId = academicYearsCache.get(0).getId();

			log.info("=== LOAD COMBOS yearId={} ===", yearId);

			if (yearId == null)
			{
				availableStudents = em.createQuery("SELECT s FROM StudentEntity s ORDER BY s.firstName", StudentEntity.class).setMaxResults(200)
						.getResultList();
				loadClassrooms();
				return;
			}

			// 1. Quem já está matriculado NESTE ano - para excluir
			List<Long> alreadyEnrolledThisYear = findEnrolledStudentIdsByYear(yearId);
			log.info("Já matriculados neste ano {}: {}", yearId, alreadyEnrolledThisYear.size());

			// 2. Ano anterior
			Long previousYearId = findPreviousYearId(yearId);
			log.info("Ano anterior de {} é {}", yearId, previousYearId);

			List<Long> candidates;
			if (previousYearId == null)
			{
				// PRIMEIRO ANO - só novos
				candidates = em.createNamedQuery("Student.findNewStudentIds", Long.class).getResultList();
			} else
			{
				List<Long> newOnes = em.createNamedQuery("Student.findNewStudentIds", Long.class).getResultList();
				List<Long> fromPrevious = findEnrolledStudentIdsByYear(previousYearId);
				candidates = new ArrayList<>();
				candidates.addAll(newOnes);
				candidates.addAll(fromPrevious);
				candidates = candidates.stream().distinct().toList();
			}

			// 3. Remove quem já está neste ano
			candidates = candidates.stream().filter(id -> !alreadyEnrolledThisYear.contains(id)).toList();

			if (candidates.isEmpty())
			{
				availableStudents = List.of();
				log.info("Nenhum candidato elegível");
				loadClassrooms();
				return;
			}

			// 4. Filtra só quem tem pagamento PAGO de matrícula/inscrição/renovação no ano
			// selecionado
			List<Long> withPayment = List.of();
			try
			{
				withPayment = em.createQuery("SELECT DISTINCT p.student.id FROM PaymentEntity p LEFT JOIN p.items pi LEFT JOIN pi.feeItem f "
						+ "WHERE p.student.id IN :ids AND p.academicYear.id = :yearId AND p.status = 'PAGO' "
						+ "AND (LOWER(COALESCE(pi.descricao,'')) LIKE '%matric%' OR LOWER(COALESCE(pi.descricao,'')) LIKE '%inscri%' OR LOWER(COALESCE(pi.descricao,'')) LIKE '%renov%' OR LOWER(COALESCE(f.descricao,'')) LIKE '%matric%' OR LOWER(COALESCE(f.descricao,'')) LIKE '%inscri%' OR LOWER(COALESCE(f.descricao,'')) LIKE '%renov%')",
						Long.class).setParameter("ids", candidates).setParameter("yearId", yearId).getResultList();
			} catch (Exception ex)
			{
				log.warn("query pagamento falhou, usando todos candidatos", ex);
				withPayment = candidates;
			}

			if (withPayment.isEmpty())
			{
				availableStudents = List.of();
			} else
			{
				availableStudents = em.createQuery("SELECT s FROM StudentEntity s WHERE s.id IN :ids ORDER BY s.firstName", StudentEntity.class)
						.setParameter("ids", withPayment).getResultList();
			}
			log.info("Disponíveis finais para matrícula: {}", availableStudents.size());

		} catch (Exception e)
		{
			log.error("Erro load students", e);
			availableStudents = em.createQuery("SELECT s FROM StudentEntity s ORDER BY s.firstName", StudentEntity.class).setMaxResults(200).getResultList();
		}
		loadClassrooms();
	}

	// ========= HELPERS NOVOS =========
	private List<Long> findEnrolledStudentIdsByYear(Long yearId)
	{
		try
		{
			return em.createNamedQuery("Enrollment.findEnrolledStudentIdsByYear", Long.class).setParameter("yearId", yearId).getResultList();
		} catch (Exception e)
		{
			// fallback se não tem NamedQuery
			return em.createQuery("SELECT e.student.id FROM EnrollmentEntity e WHERE e.academicYear.id = :yearId", Long.class).setParameter("yearId", yearId)
					.getResultList();
		}
	}

	private Long findPreviousYearId(Long currentYearId)
	{
		try
		{
			// Pega valor do ano ex: "2025" ou "2025/2026" -> tenta extrair 2025
			AcademicYearEntity current = em.find(AcademicYearEntity.class, currentYearId);
			if (current == null)
				return null;
			String yStr = current.getYear().replaceAll("[^0-9]", "").substring(0, 4);
			int yVal = Integer.parseInt(yStr);
			// Procura ano com valor = yVal -1
			List<AcademicYearEntity> prev = em.createQuery("SELECT a FROM AcademicYearEntity a WHERE a.year LIKE :like", AcademicYearEntity.class)
					.setParameter("like", "%" + (yVal - 1) + "%").setMaxResults(1).getResultList();
			return prev.isEmpty() ? null : prev.get(0).getId();
		} catch (Exception e)
		{
			return null;
		}
	}

	public int getAvailableStudentsCount()
	{
		return availableStudents != null ? availableStudents.size() : 0;
	}

	public List<ClassroomEntity> getAllClassrooms()
	{
		if (availableClassrooms == null)
			loadClassrooms();
		return availableClassrooms;
	}

	public void loadClassrooms()
	{
		try
		{
			String base = "SELECT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.responsibleTeacher LEFT JOIN FETCH c.academicYear ";
			if (selectedClassLevelId != null)
			{
				availableClassrooms = em.createQuery(base + "WHERE c.classLevel.id = :clId AND c.active = true ORDER BY c.name", ClassroomEntity.class)
						.setParameter("clId", selectedClassLevelId).getResultList();
			} else if (selectedAcademicYearId != null)
			{
				availableClassrooms = em.createQuery(base + "WHERE c.academicYear.id = :yearId AND c.active = true ORDER BY c.name", ClassroomEntity.class)
						.setParameter("yearId", selectedAcademicYearId).getResultList();
			} else
			{
				availableClassrooms = em.createQuery(base + "WHERE c.active = true ORDER BY c.name", ClassroomEntity.class).getResultList();
			}
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
		selectedStudentId = null;
		selectedStudentEntity = null;
		studentPayments = new ArrayList<>();
		selectedPayments = new ArrayList<>();
		hasValidPayment = false;
		hasPaidEnrollment = false;
		hasPaidRegistration = false;
		hasPaidRenewal = false;
		loadCombos();
	}

	@jakarta.transaction.Transactional
	public void loadStudentPaymsents()
	{
		studentPayments = new ArrayList<>();
		if (selectedStudentId == null)
			return;
		try
		{
			Long yearId = selectedAcademicYearId;
			if (yearId == null && loginBean != null)
				yearId = loginBean.getAcademicYearId();

			List<PaymentEntity> payments;
			String base = "SELECT DISTINCT p FROM PaymentEntity p LEFT JOIN FETCH p.items pi LEFT JOIN FETCH pi.feeItem f WHERE p.student.id = :sid ";
			String order = " ORDER BY p.id DESC";
			if (yearId != null)
			{
				try
				{
					payments = em.createQuery(base + "AND p.academicYear.id = :yearId" + order, PaymentEntity.class).setParameter("sid", selectedStudentId)
							.setParameter("yearId", yearId).getResultList();
				} catch (Exception ex)
				{
					payments = em.createQuery(base + order, PaymentEntity.class).setParameter("sid", selectedStudentId).getResultList();
				}
			} else
			{
				payments = em.createQuery(base + order, PaymentEntity.class).setParameter("sid", selectedStudentId).getResultList();
			}

			hasPaidEnrollment = false;
			hasPaidRegistration = false;
			hasPaidRenewal = false;

			java.util.Set<String> seenKeys = new java.util.HashSet<>();

			for (PaymentEntity p : payments)
			{
				if (p.getItems() == null)
					continue;
				for (PaymentItemEntity pi : p.getItems())
				{
					if (pi == null)
						continue;

					String desc = pi.getFeeItem() != null && pi.getFeeItem().getDescricao() != null ? pi.getFeeItem().getDescricao() : pi.getDescricao();

					// mesma descrição = mesmo item (fica o mais recente, pois ordena por id DESC)
					String key = desc != null ? desc.trim().toUpperCase() : "ID-" + pi.getId();
					if (!seenKeys.add(key))
						continue;

					PaymentDTO dto = new PaymentDTO();
					dto.setReference(p.getReference() != null ? p.getReference() : "PAY-" + p.getId());
					java.math.BigDecimal preco = pi.getFeeItem() != null && pi.getFeeItem().getValor() != null ? pi.getFeeItem().getValor()
							: (pi.getValor() != null ? pi.getValor() : java.math.BigDecimal.ZERO);
					dto.setAmount(preco.toPlainString());
					dto.setStatus(p.getMethod() != null ? p.getMethod().name() : "PAGO");
					dto.setDescription(desc);
					dto.setMethod(p.getMethod() != null ? p.getMethod().name() : "M-Pesa");
					dto.setMonthLabel(desc);
					dto.setSelected(false);
					studentPayments.add(dto);

					String upper = desc != null ? desc.toUpperCase() : "";
					if (upper.contains("INSCRI"))
						hasPaidEnrollment = true;
					if (upper.contains("MATRIC"))
						hasPaidRegistration = true;
					if (upper.contains("RENOV"))
						hasPaidRenewal = true;
				}
			}

			if (currentEnrollmentType == null)
				currentEnrollmentType = enrollmentService.getEnrollmentTypeForStudent(selectedStudentId);
			if (currentEnrollmentType == null)
				currentEnrollmentType = EnrollmentType.FIRST_ENROLLMENT;

			if (currentEnrollmentType == EnrollmentType.FIRST_ENROLLMENT)
				hasValidPayment = hasPaidEnrollment && hasPaidRegistration;
			else
				hasValidPayment = hasPaidRenewal;

		} catch (Exception e)
		{
			log.error("loadStudentPayments", e);
			hasValidPayment = false;
		}
	}

	public String confirmEnrollment()
	{
		try
		{
			if (selectedStudentId == null || selectedAcademicYearId == null)
			{
				addError("Dados incompletos", "Selecione aluno e ano");
				return null;
			}
			Boolean exists = em.createNamedQuery("EnrollmentEntity.existsByStudentAndYear", Boolean.class).setParameter("studentId", selectedStudentId)
					.setParameter("yearId", selectedAcademicYearId).getSingleResult();
			if (Boolean.TRUE.equals(exists))
			{
				addError("Já matriculado", "Aluno já tem matrícula neste ano");
				refreshList();
				dashboardBean.navigate("enrollment/enrollment-list", "matriculas");
				return null;
			}
			// VALIDA LOTAÇÃO
			if (selectedClassroomId != null)
			{
				ClassroomEntity turmaCheck = em.find(ClassroomEntity.class, selectedClassroomId);
				if (turmaCheck != null)
				{
					em.refresh(turmaCheck);
					if (turmaCheck.getCurrentStudents() >= turmaCheck.getCapacity())
					{
						addError("Turma lotada", turmaCheck.getName() + " lotada!");
						return null;
					}
				}
			}
			AcademicYearEntity year = em.find(AcademicYearEntity.class, selectedAcademicYearId);
			StudentEntity stu = em.find(StudentEntity.class, selectedStudentId);
			SchoolEntity school = em.createQuery("SELECT s FROM SchoolEntity s", SchoolEntity.class).setMaxResults(1).getSingleResult();
			EnrollmentEntity e = new EnrollmentEntity();
			e.setSchool(school);
			e.setAcademicYear(year);
			e.setStudent(stu);
			if (selectedClassLevelId != null)
				e.setClassLevel(em.find(ClassLevelEntity.class, selectedClassLevelId));
			if (selectedClassroomId != null)
				e.setClassroom(em.find(ClassroomEntity.class, selectedClassroomId));
			e.setType(currentEnrollmentType != null ? currentEnrollmentType : EnrollmentType.FIRST_ENROLLMENT);
			e.setStatus(EnrollmentStatus.ENROLLED);
			e.setCode("ENR-" + System.currentTimeMillis());
			em.persist(e);
			em.flush();
			if (selectedClassroomId != null)
			{
				em.createQuery("UPDATE ClassroomEntity c SET c.currentStudents = c.currentStudents + 1 WHERE c.id = :id")
						.setParameter("id", selectedClassroomId).executeUpdate();
			}
			addInfo("Matrícula confirmada", "Aluno " + stu.getFullName() + " - " + e.getCode());
			refreshList();
			dashboardBean.navigate("enrollment/enrollment-list", "matriculas");
			return null;
		} catch (Exception ex)
		{
			log.error("Erro ao confirmar matricula", ex);
			addError("Erro", ex.getMessage());
			return null;
		}
	}

	// ... mantém o resto do teu bean igual (loadAll, filter, prepareNew,
	// viewEnrollment, prepareEdit, delete, saveAndReturn, goToConfirm, etc) ...
	// Só copia daqui pra baixo do teu arquivo antigo

	private void refreshList()
	{
		filterYearId = null;
		filterStatus = null;
		searchTerm = null;
		em.flush();
		loadAll();
	}

	public void loadAll()
	{
		try
		{
			Long yearId = loginBean.getAcademicYearId();
			loadedYearId = yearId;
			if (yearId != null)
			{
				allEnrollments = em.createQuery(
						"SELECT e FROM EnrollmentEntity e LEFT JOIN FETCH e.student LEFT JOIN FETCH e.academicYear LEFT JOIN FETCH e.classroom WHERE e.academicYear.id = :yearId ORDER BY e.id DESC",
						EnrollmentEntity.class).setParameter("yearId", yearId).getResultList();
			} else
			{
				allEnrollments = em.createQuery(
						"SELECT e FROM EnrollmentEntity e LEFT JOIN FETCH e.student LEFT JOIN FETCH e.academicYear LEFT JOIN FETCH e.classroom ORDER BY e.id DESC",
						EnrollmentEntity.class).getResultList();
			}
			enrollments = allEnrollments;
			if (allEnrollments == null)
				allEnrollments = List.of();
			if (enrollments == null)
				enrollments = List.of();
		} catch (Exception e)
		{
			e.printStackTrace();
			allEnrollments = enrollmentService.findAll();
			enrollments = allEnrollments;
		}
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
			enrollments = enrollments.stream().filter(e -> (e.getStudent() != null && e.getStudent().getFullName().toLowerCase().contains(term))
					|| (e.getCode() != null && e.getCode().toLowerCase().contains(term))).collect(Collectors.toList());
		}
	}

	public void prepareNew()
	{
		selected = new EnrollmentEntity();
		selectedClassLevelId = null;
		selectedClassroomId = null;
		selectedStudentId = null;
		selectedStudentEntity = null;
		selectedAcademicYearId = null;
		editing = false;

		// LIMPA PAGAMENTOS ANTIGOS
		studentPayments = new ArrayList<>();
		selectedPayments = new ArrayList<>();
		hasValidPayment = false;
		hasPaidEnrollment = false;
		hasPaidRegistration = false;
		hasPaidRenewal = false;
		selectedPaymentReference = "";
		currentEnrollmentType = null;

		// recarrega combos do zero
		availableStudents = new ArrayList<>();
		availableClassrooms = new ArrayList<>();

		setDefaultYearFromLogin();
		loadCombos();
	}

	public void preparePrint(EnrollmentEntity e)
	{
		this.selected = e;
		this.selectedYear = e.getAcademicYear() != null ? String.valueOf(e.getAcademicYear().getYear()) : "2026";
		try
		{
			printFees = monthlyFeeService.findByEnrollmentId(e.getId());
		} catch (Exception ex)
		{
			printFees = new ArrayList<>();
		}
	}
//
//	public void viewEnrollment(EnrollmentEntity e)
//	{
//		this.selected = em.find(EnrollmentEntity.class, e.getId());
//		if (this.selected == null)
//			this.selected = e;
//		this.startMonth = null;
//		this.generatedMonthlyFees = new ArrayList<>();
//		try
//		{
//			selectedYear = selected.getAcademicYear().getYear().substring(0, 4);
//		} catch (Exception ex)
//		{
//			selectedYear = "2026";
//		}
//		dashboardBean.navigate("enrollment/enrollment-view", "ver-matricula");
//	}

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
		onStudentChange();
	}

	@jakarta.transaction.Transactional
	public void onStudentChange()
	{
		studentPayments = new ArrayList<>();
		selectedPayments = new ArrayList<>();
		hasValidPayment = false;
		hasPaidEnrollment = false;
		hasPaidRegistration = false;
		hasPaidRenewal = false;
		selectedPaymentReference = "";
		currentEnrollmentType = null;
		selectedStudentEntity = null;

		if (selectedStudentId == null)
			return;

		selectedStudentEntity = em.find(StudentEntity.class, selectedStudentId);
		currentEnrollmentType = enrollmentService.getEnrollmentTypeForStudent(selectedStudentId);
		loadStudentPayments(); // agora traz só do aluno selecionado
	}

	@jakarta.transaction.Transactional
	public void loadStudentPayments()
	{
		studentPayments = new ArrayList<>();
		selectedPayments = new ArrayList<>();
		selectedPaymentReference = "";
		if (selectedStudentId == null)
			return;
		try
		{
			Long yearId = selectedAcademicYearId;
			if (yearId == null && loginBean != null)
				yearId = loginBean.getAcademicYearId();

			List<PaymentEntity> payments;
			String base = "SELECT DISTINCT p FROM PaymentEntity p LEFT JOIN FETCH p.items pi LEFT JOIN FETCH pi.feeItem f WHERE p.student.id = :sid ";
			String order = " ORDER BY p.id DESC";
			if (yearId != null)
			{
				try
				{
					payments = em.createQuery(base + "AND p.academicYear.id = :yearId" + order, PaymentEntity.class).setParameter("sid", selectedStudentId)
							.setParameter("yearId", yearId).getResultList();
				} catch (Exception ex)
				{
					payments = em.createQuery(base + order, PaymentEntity.class).setParameter("sid", selectedStudentId).getResultList();
				}
			} else
			{
				payments = em.createQuery(base + order, PaymentEntity.class).setParameter("sid", selectedStudentId).getResultList();
			}

			hasPaidEnrollment = false;
			hasPaidRegistration = false;
			hasPaidRenewal = false;

			// Deduplica por descrição do item (fica o pagamento mais recente, pois ordena
			// por id DESC)
			java.util.Set<String> seenKeys = new java.util.HashSet<>();

			for (PaymentEntity p : payments)
			{
				if (p.getItems() == null)
					continue;
				for (PaymentItemEntity pi : p.getItems())
				{
					if (pi == null)
						continue;

					String desc = pi.getFeeItem() != null && pi.getFeeItem().getDescricao() != null ? pi.getFeeItem().getDescricao() : pi.getDescricao();

					String key = desc != null ? desc.trim().toUpperCase() : "ID-" + pi.getId();
					if (!seenKeys.add(key))
						continue;

					PaymentDTO dto = new PaymentDTO();
					dto.setReference(p.getReference() != null ? p.getReference() : "PAY-" + p.getId());
					java.math.BigDecimal preco = pi.getFeeItem() != null && pi.getFeeItem().getValor() != null ? pi.getFeeItem().getValor()
							: (pi.getValor() != null ? pi.getValor() : java.math.BigDecimal.ZERO);
					dto.setAmount(preco.toPlainString());
					dto.setStatus(p.getMethod() != null ? p.getMethod().name() : "PAGO");
					dto.setDescription(desc);
					dto.setMethod(p.getMethod() != null ? p.getMethod().name() : "M-Pesa");
					dto.setMonthLabel(desc);
					dto.setSelected(false);
					studentPayments.add(dto);

					String upper = desc != null ? desc.toUpperCase() : "";
					if (upper.contains("INSCRI"))
						hasPaidEnrollment = true;
					if (upper.contains("MATRIC"))
						hasPaidRegistration = true;
					if (upper.contains("RENOV"))
						hasPaidRenewal = true;
				}
			}

			if (currentEnrollmentType == null)
				currentEnrollmentType = enrollmentService.getEnrollmentTypeForStudent(selectedStudentId);
			if (currentEnrollmentType == null)
				currentEnrollmentType = EnrollmentType.FIRST_ENROLLMENT;

			if (currentEnrollmentType == EnrollmentType.FIRST_ENROLLMENT)
				hasValidPayment = hasPaidEnrollment && hasPaidRegistration;
			else
				hasValidPayment = hasPaidRenewal;

		} catch (Exception e)
		{
			log.error("loadStudentPayments", e);
			hasValidPayment = false;
		}
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
				em.createQuery("DELETE FROM MonthlyFeeEntity m WHERE m.enrollment.id = :enrollId").setParameter("enrollId", toDelete.getId()).executeUpdate();
				em.remove(toDelete);
				addInfo("Apagado", "Matrícula " + e.getCode() + " apagada");
				loadAll();
			}
		} catch (Exception ex)
		{
			log.error("delete enrollment", ex);
			addError("Erro", ex.getMessage());
		}
	}

	public String backToForm()
	{
		dashboardBean.navigate("enrollment/enrollment-include", "matriculas");
		return null;
	}

	@Transactional
	public String saveAndReturn()
	{
		try
		{
			if (selectedStudentId == null || selectedAcademicYearId == null || selectedClassroomId == null)
			{
				addError("Campos obrigatórios", "Selecione Ano, Classe, Turma e Aluno");
				return null;
			}
			Boolean exists = em.createNamedQuery("EnrollmentEntity.existsByStudentAndYear", Boolean.class).setParameter("studentId", selectedStudentId)
					.setParameter("yearId", selectedAcademicYearId).getSingleResult();
			if (Boolean.TRUE.equals(exists))
			{
				addError("Já matriculado", "Aluno já tem matrícula neste ano");
				return null;
			}
			// VALIDA LOTAÇÃO ANTES
			ClassroomEntity turmaCheck = em.find(ClassroomEntity.class, selectedClassroomId);
			if (turmaCheck != null)
			{
				em.refresh(turmaCheck);
				if (turmaCheck.getCurrentStudents() >= turmaCheck.getCapacity())
				{
					addError("Turma lotada", turmaCheck.getName() + " está com " + turmaCheck.getCurrentStudents() + "/" + turmaCheck.getCapacity());
					return null;
				}
			}
			AcademicYearEntity year = em.find(AcademicYearEntity.class, selectedAcademicYearId);
			StudentEntity stu = em.find(StudentEntity.class, selectedStudentId);
			SchoolEntity school = em.createQuery("SELECT s FROM SchoolEntity s", SchoolEntity.class).setMaxResults(1).getSingleResult();
			EnrollmentEntity en = new EnrollmentEntity();
			en.setSchool(school);
			en.setAcademicYear(year);
			en.setStudent(stu);
			if (selectedClassLevelId != null)
				en.setClassLevel(em.find(ClassLevelEntity.class, selectedClassLevelId));
			if (selectedClassroomId != null)
			{
				ClassroomEntity classroom = em.find(ClassroomEntity.class, selectedClassroomId);
				en.setClassroom(classroom);
			}
			en.setType(currentEnrollmentType != null ? currentEnrollmentType : EnrollmentType.FIRST_ENROLLMENT);
			en.setStatus(EnrollmentStatus.ENROLLED);
			en.setCode("ENR-" + System.currentTimeMillis());
			em.persist(en);
			em.flush();
			// INCREMENTA CONTADOR
			if (selectedClassroomId != null)
			{
				em.createQuery("UPDATE ClassroomEntity c SET c.currentStudents = c.currentStudents + 1 WHERE c.id = :id")
						.setParameter("id", selectedClassroomId).executeUpdate();
			}
			addInfo("Matrícula confirmada", "Aluno " + stu.getFullName() + " matriculado em " + (turmaCheck != null ? turmaCheck.getName() : ""));
			refreshList();
			dashboardBean.navigate("enrollment/enrollment-list", "matriculas");
			return null;
		} catch (Exception ex)
		{
			log.error("Erro saveAndReturn", ex);
			addError("Erro ao confirmar", ex.getMessage());
			return null;
		}
	}

	public void goToConfirm()
	{
		if (selectedAcademicYearId == null || selectedClassLevelId == null || selectedClassroomId == null || selectedStudentId == null)
		{
			addError("Campos obrigatórios", "Selecione Ano, Classe, Turma e Aluno");
			return;
		}
		if (!hasValidPayment)
		{
			addError("Pagamento obrigatório", "Selecione um pagamento válido");
			return;
		}
		if (selectedStudentEntity == null)
			selectedStudentEntity = em.find(StudentEntity.class, selectedStudentId);
		dashboardBean.navigate("enrollment/enrollment-confirm", "confirmar-matricula");
	}

	public List<PaymentDTO> getSelectedPayments()
	{
		if (selectedPayments == null)
			selectedPayments = new ArrayList<>();
		return selectedPayments;
	}

	public void setSelectedPayments(List<PaymentDTO> v)
	{
		this.selectedPayments = v != null ? v : new ArrayList<>();
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

	public void generateMonthlyFromDate()
	{
		if (selected == null || startMonth == null)
		{
			addError("Erro", "Selecione mês inicial");
			return;
		}
		if (endMonth != null && endMonth < startMonth)
		{
			addError("Erro", "Mês final não pode ser menor que o inicial");
			return;
		}
		String sc = selected.getStudent() != null ? selected.getStudent().getCode() : "SEM-COD";
		String eid = selected.getId() != null ? selected.getId().toString() : "0";
		int year = 2026;
		try
		{
			year = Integer.parseInt(selectedYear);
		} catch (Exception ignored)
		{
		}
		int finalMonth = (endMonth == null) ? 12 : endMonth; // se não escolher, vai até Dezembro

		generatedMonthlyFees = new ArrayList<>();
		for (int m = startMonth; m <= finalMonth; m++)
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
	}

	@Transactional
	public void saveMonthlyReferences()
	{
		if (selected == null || startMonth == null)
		{
			addError("Erro", "Selecione matrícula e mês inicial");
			return;
		}
		int finalMonth = (endMonth == null) ? 12 : endMonth;
		try
		{
			Long userId = loginBean.getLoggedUser().getId();
			int year = Integer.parseInt(selectedYear);
			if (defaultAmount == null)
				defaultAmount = new BigDecimal("2500.00");
			if (defaultDueDay == null)
				defaultDueDay = 10;

			int gravadas = 0;
			for (int m = startMonth; m <= finalMonth; m++)
			{
				gravadas += monthlyFeeService.generateFromMonth(selected.getId(), m, year, defaultAmount, defaultDueDay, userId).size();
			}
			addInfo("Sucesso", gravadas + " referências gravadas");
			generatedMonthlyFees = new ArrayList<>();
		} catch (Exception e)
		{
			e.printStackTrace();
			addError("Erro", e.getMessage());
		}
	}

	public void viewEnrollment(EnrollmentEntity e)
	{
		this.selected = e;
		this.selectedYear = e.getAcademicYear() != null ? String.valueOf(e.getAcademicYear().getYear()) : "2026";
		this.generatedMonthlyFees = new ArrayList<>();
		this.startMonth = null;
		this.endMonth = null;

		// CARREGA AS JÁ GRAVADAS DO BANCO
		try
		{
			List<MonthlyFeeEntity> saved = monthlyFeeService.findByEnrollmentId(e.getId());
			if (saved != null && !saved.isEmpty())
			{
				// Converte Entity -> DTO pra mostrar na mesma tabela
				for (MonthlyFeeEntity entity : saved)
				{
					MonthlyFeeDTO dto = new MonthlyFeeDTO();
					dto.setMonth(entity.getMonth());
					dto.setMonthLabel(entity.getMonthLabel());
					dto.setYear(entity.getYear());
					dto.setReference(entity.getReference());
					dto.setAmount(entity.getAmount());
					dto.setDueDate(Date.from(entity.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
					generatedMonthlyFees.add(dto);
				}
				// ordena por mês
				generatedMonthlyFees.sort(Comparator.comparing(MonthlyFeeDTO::getMonth));
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}

		dashboardBean.navigate("enrollment/enrollment-view", "ver");
	}

	public BigDecimal getTotalAmount()
	{
		return generatedMonthlyFees.stream().map(f -> f.getAmount() != null ? f.getAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private void addInfo(String s, String d)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, s, d));
	}

	private void addError(String s, String d)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, s, d));
	}

	public boolean isFirstEnrollment()
	{
		return currentEnrollmentType != null && currentEnrollmentType.name().equals("FIRST_ENROLLMENT");
	}

	public boolean isRenewalEnrollment()
	{
		return currentEnrollmentType != null && !currentEnrollmentType.name().equals("FIRST_ENROLLMENT");
	}

	public String getSelectedStudentName()
	{
		if (selectedStudentEntity != null)
			return selectedStudentEntity.getFullName();
		if (selected != null && selected.getStudent() != null)
			return selected.getStudent().getFullName();
		return "Aluno";
	}

	public String getSelectedYearLabel()
	{
		if (selected != null && selected.getAcademicYear() != null)
			return selected.getAcademicYear().getYear();
		return selectedYear != null ? selectedYear : "2026";
	}

	// GETTERS SETTERS (mantém os teus)
	public List<EnrollmentEntity> getEnrollments()
	{
		Long currentYearId = loginBean != null ? loginBean.getAcademicYearId() : null;
		if (enrollments == null || !java.util.Objects.equals(currentYearId, loadedYearId))
		{
			filterYearId = null;
			loadAll();
			setDefaultYearFromLogin();
		}
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
		{
			loadClassrooms();
		}
		if (availableClassrooms == null)
			return List.of();

		return availableClassrooms; // TODAS para gestão

	}

	public List<ClassroomEntity> getAvailableClassroomsWithVacancy()
	{
		if (availableClassrooms == null)
		{
			loadClassrooms();
		}
		if (availableClassrooms == null)
			return List.of();
		return availableClassrooms.stream().filter(c -> c.getCurrentStudents() < c.getCapacity()).toList();
	}

	public List<AcademicYearEntity> getAcademicYearsCache()
	{
		return academicYearsCache;
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

	public List<PaymentDTO> getStudentPayments()
	{
		return studentPayments;
	}

	public int getValidPaymentsCount()
	{
		return (int) studentPayments.stream().filter(p -> "PAGO".equals(p.getStatus())).count();
	}

	public boolean isHasValidPayment()
	{
		return hasValidPayment;
	}

	public void setHasValidPayment(boolean v)
	{
		hasValidPayment = v;
	}

	public String getSelectedPaymentReference()
	{
		return selectedPaymentReference;
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

	public BigDecimal getDefaultAmount()
	{
		return defaultAmount;
	}

	public void setDefaultAmount(BigDecimal v)
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

	public boolean isHasPaidEnrollment()
	{
		return hasPaidEnrollment;
	}

	public boolean isHasPaidRegistration()
	{
		return hasPaidRegistration;
	}

	public boolean isHasPaidRenewal()
	{
		return hasPaidRenewal;
	}

	public EnrollmentType getCurrentEnrollmentType()
	{
		return currentEnrollmentType;
	}

	public Integer getEndMonth()
	{
		return endMonth;
	}

	public void setEndMonth(Integer endMonth)
	{
		this.endMonth = endMonth;
	}

	public void onPaymentSelect(PaymentDTO pay)
	{
		if (pay.isSelected())
			selectedPayments.add(pay);
		else
			selectedPayments.removeIf(p -> p.getReference().equals(pay.getReference()));
		hasValidPayment = !selectedPayments.isEmpty();
		selectedPaymentReference = selectedPayments.stream().map(PaymentDTO::getReference).collect(Collectors.joining(", "));
	}
}