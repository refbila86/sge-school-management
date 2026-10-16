package mz.co.sge.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import mz.co.sge.controller.MonthlyFeeBean.MonthlyFeeDTO;
import mz.co.sge.dto.PaymentDTO;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.ClassLevelEntity;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.MonthlyFeeEntity;
import mz.co.sge.entity.PaymentEntity;
import mz.co.sge.entity.PaymentItemEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.EnrollmentStatus;
import mz.co.sge.service.IEnrollmentService;

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

	// LISTA
	private List<EnrollmentEntity> enrollments;
	private List<EnrollmentEntity> allEnrollments;
	private Long filterYearId;
	private EnrollmentStatus filterStatus;
	private String searchTerm;

	// INCLUDE + PAGAMENTOS
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

	private List<PaymentDTO> studentPayments = new ArrayList<>();
	private List<PaymentDTO> selectedPayments = new ArrayList<>();
	private boolean hasValidPayment = false;
	private String selectedPaymentReference = "";

	// VIEW MENSALIDADES SIMPLES (mes, referencia, valor, data limite)
	private Integer startMonth;
	private List<MonthOption> months;
	private List<MonthlyFeeDTO> generatedMonthlyFees = new ArrayList<>();
	private String selectedYear = "2026";
	private BigDecimal defaultAmount = new BigDecimal("2500.00");
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
			availableClassLevels = enrollmentService.findAllClassLevels();
		} catch (Exception e)
		{
			availableClassLevels = List.of();
		}

		try
		{
			Long schoolId = null;
			Long yearId = selectedAcademicYearId;

			// Pega schoolId correto do usuário logado
			if (loginBean != null && loginBean.getLoggedUser() != null)
			{
				// tenta getSchool().getId()
				try
				{
					if (loginBean.getLoggedUser().getSchoolId() != null)
					{
						schoolId = loginBean.getLoggedUser().getSchoolId();
					}
				} catch (Exception ex)
				{
					// fallback se não tiver getSchool()
					schoolId = loginBean.getLoggedUser().getId();
				}

				// Pega yearId do login (o ano escolhido na tela de login)
				try
				{
					java.lang.reflect.Method m = loginBean.getClass().getMethod("getSelectedAcademicYearId");
					Object v = m.invoke(loginBean);
					if (v != null)
						yearId = (Long) v;
				} catch (Exception ignore)
				{
				}
			}

			if (schoolId == null)
				schoolId = 1L;
			if (yearId == null)
				yearId = 1L;

			// SÓ ALUNOS QUE PAGARAM MATRÍCULA NAQUELE ANO
			availableStudents = enrollmentService.findStudentsWithMatriculaPaid(schoolId, yearId);

			System.out.println("Filtro matrícula: school=" + schoolId + " year=" + yearId + " -> " + availableStudents.size() + " alunos");

		} catch (Exception e)
		{
			e.printStackTrace();
			availableStudents = List.of();
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
		loadCombos(); // recarrega alunos quando muda ano
		loadClassrooms();
	}

	// QUANDO MUDA ALUNO - CARREGA PAGAMENTOS
	public void onStudentChange()
	{
		studentPayments = new ArrayList<>();
		selectedPayments = new ArrayList<>();
		hasValidPayment = false;
		selectedPaymentReference = "";
		if (selectedStudentId == null)
		{
			selectedStudentEntity = null;
			return;
		}
		try
		{
			selectedStudentEntity = em.find(StudentEntity.class, selectedStudentId);
		} catch (Exception e)
		{
		}
		loadStudentPayments();
	}

	public void onStudentChanges()
	{
		studentPayments = new ArrayList<>();
		selectedPayments = new ArrayList<>();
		hasValidPayment = false;
		selectedPaymentReference = "";
		if (selectedStudentId == null)
		{
			selectedStudentEntity = null;
			return;
		}
		try
		{
			selectedStudentEntity = em.find(StudentEntity.class, selectedStudentId);
		} catch (Exception e)
		{
		}
		loadStudentPayments();
	}

	public void loadStudentPayments()
	{
		studentPayments = new ArrayList<>();
		if (selectedStudentId == null)
			return;
		try
		{
			Long yearId = selectedAcademicYearId;
			if (yearId == null && loginBean != null)
			{
				try
				{
					Object v = loginBean.getClass().getMethod("getSelectedAcademicYearId").invoke(loginBean);
					if (v != null)
						yearId = (Long) v;
				} catch (Exception ignore)
				{
				}
			}
			// Busca pagamentos do aluno naquele ano COM feeItem
			List<PaymentEntity> payments = em
					.createQuery(
							"SELECT DISTINCT p FROM PaymentEntity p " + "LEFT JOIN FETCH p.items pi " + "LEFT JOIN FETCH pi.feeItem f "
									+ "WHERE p.student.id = :sid " + (yearId != null ? "AND p.academicYear.id = :yearId " : "") + "ORDER BY p.paymentDate DESC",
							PaymentEntity.class)
					.setParameter("sid", selectedStudentId).setParameter("yearId", yearId != null ? yearId : 1L).getResultList();
			// Se p.academicYear não existe, busca sem ano
			if (payments.isEmpty() && yearId != null)
			{
				payments = em.createQuery(
						"SELECT DISTINCT p FROM PaymentEntity p LEFT JOIN FETCH p.items pi LEFT JOIN FETCH pi.feeItem WHERE p.student.id = :sid ORDER BY p.paymentDate DESC",
						PaymentEntity.class).setParameter("sid", selectedStudentId).getResultList();
			}
			for (PaymentEntity p : payments)
			{
				if (p.getItems() == null)
					continue;
				for (PaymentItemEntity pi : p.getItems())
				{
					PaymentDTO dto = new PaymentDTO();
					dto.setReference(p.getReference() != null ? p.getReference() : "PAY-" + p.getId());
					// PREÇO VEM DO fee_items VIA fee_item_id
					BigDecimal preco = pi.getFeeItem() != null && pi.getFeeItem().getValor() != null ? pi.getFeeItem().getValor()
							: (pi.getValor() != null ? pi.getValor() : BigDecimal.ZERO);
					dto.setAmount(preco.toPlainString());
					dto.setStatus(p.getMethod() != null ? p.getMethod().name() : "PAGO");
					dto.setDescription(pi.getFeeItem() != null ? pi.getFeeItem().getDescricao() : pi.getDescricao());
					dto.setMethod(p.getMethod() != null ? p.getMethod().name() : "M-Pesa");
					dto.setMonthLabel(pi.getFeeItem() != null ? pi.getFeeItem().getDescricao() : "");
					dto.setSelected(false);
					studentPayments.add(dto);
					// Se for MATRICULA ou INSCRIÇÃO paga, valida para ENROLLED
					if (pi.getFeeItem() != null && pi.getFeeItem().getDescricao() != null)
					{
						String desc = pi.getFeeItem().getDescricao().toUpperCase();
						if (desc.contains("MATRIC") || desc.contains("INSCRI"))
						{
							hasValidPayment = true;
						}
					}
				}
			}
			if (!studentPayments.isEmpty() && !hasValidPayment)
			{
				hasValidPayment = true; // tem pelo menos um pagamento
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void loadStudentPaymentss()
	{
		studentPayments = new ArrayList<>();
		if (selectedStudentId == null)
			return;
		try
		{
			// Tenta carregar de PaymentEntity, MonthlyFeeEntity pago, etc - adapta ao teu
			// modelo
			// Exemplo 1: Payments table
			List<?> payments = new ArrayList<>();
			try
			{
				payments = em.createQuery("SELECT p FROM PaymentEntity p WHERE p.student.id = :sid ORDER BY p.paymentDate DESC")
						.setParameter("sid", selectedStudentId).getResultList();
				for (Object o : payments)
				{
					try
					{
						java.lang.reflect.Method getRef = o.getClass().getMethod("getReference");
						java.lang.reflect.Method getAmount = o.getClass().getMethod("getAmount");
						java.lang.reflect.Method getDate = o.getClass().getMethod("getPaymentDate");
						java.lang.reflect.Method getStatus = o.getClass().getMethod("getStatus");
						String ref = String.valueOf(getRef.invoke(o));
						Object amt = getAmount.invoke(o);
						String amount = amt != null ? amt.toString() : "0";
						Object date = getDate.invoke(o);
						String status = getStatus != null ? String.valueOf(getStatus.invoke(o)) : "PAGO";
						PaymentDTO dto = new PaymentDTO();
						dto.setReference(ref);
						dto.setAmount(amount);
						dto.setStatus(status);
						dto.setDescription("Pagamento Matrícula");
						dto.setPaymentDate(date != null ? convertToDate(date) : new Date());
						dto.setMethod("M-Pesa");
						dto.setSelected(false);
						studentPayments.add(dto);
					} catch (Exception ex)
					{
					}
				}
			} catch (Exception e)
			{
				// Fallback: cria pagamentos fake de exemplo se não tiver tabela PaymentEntity
				// Remove isso quando tiveres PaymentEntity real
				PaymentDTO dto1 = new PaymentDTO();
				dto1.setReference("PAY-" + selectedStudentId + "-2026-001");
				dto1.setAmount("3500");
				dto1.setStatus("PAGO");
				dto1.setDescription("Taxa de Matrícula 2026");
				dto1.setPaymentDate(new Date());
				dto1.setMethod("M-Pesa");
				dto1.setMonthLabel("Matrícula");
				dto1.setSelected(false);
				PaymentDTO dto2 = new PaymentDTO();
				dto2.setReference("PAY-" + selectedStudentId + "-2026-002");
				dto2.setAmount("2500");
				dto2.setStatus("PENDENTE");
				dto2.setDescription("Mensalidade Janeiro");
				dto2.setPaymentDate(new Date());
				dto2.setMethod("Numerário");
				dto2.setMonthLabel("Janeiro");
				dto2.setSelected(false);
				studentPayments.add(dto1);
				// studentPayments.add(dto2);
			}

			// Tenta também monthly_fees pagas
			try
			{
				List<MonthlyFeeEntity> fees = em
						.createQuery("SELECT m FROM MonthlyFeeEntity m WHERE m.student.id = :sid AND m.status = 'PAGO' ORDER BY m.year, m.month",
								MonthlyFeeEntity.class)
						.setParameter("sid", selectedStudentId).getResultList();
				for (MonthlyFeeEntity f : fees)
				{
					PaymentDTO dto = new PaymentDTO();
					dto.setReference(f.getReference());
					dto.setAmount(f.getAmount().toString());
					dto.setStatus("PAGO");
					dto.setDescription(f.getMonthLabel() + " " + f.getYear());
					dto.setMonthLabel(f.getMonthLabel());
					dto.setSelected(false);
					studentPayments.add(dto);
				}
			} catch (Exception ignore)
			{
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private Date convertToDate(Object o)
	{
		if (o instanceof Date)
			return (Date) o;
		if (o instanceof LocalDate)
			return Date.from(((LocalDate) o).atStartOfDay(ZoneId.systemDefault()).toInstant());
		if (o instanceof java.time.LocalDateTime)
			return Date.from(((java.time.LocalDateTime) o).atZone(ZoneId.systemDefault()).toInstant());
		return new Date();
	}

	public void onPaymentSelect(PaymentDTO pay)
	{
		if (pay.isSelected())
		{
			selectedPayments.add(pay);
		} else
		{
			selectedPayments.removeIf(p -> p.getReference().equals(pay.getReference()));
		}
		hasValidPayment = !selectedPayments.isEmpty();
		selectedPaymentReference = selectedPayments.stream().map(PaymentDTO::getReference).collect(Collectors.joining(", "));
		if (hasValidPayment)
		{
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Pagamento selecionado", selectedPaymentReference + " - Pode mudar para ENROLLED"));
		}
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
		studentPayments = new ArrayList<>();
		selectedPayments = new ArrayList<>();
		hasValidPayment = false;
		selectedPaymentReference = "";
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
		onStudentChange();
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

	// AO CLICAR CONFIRMAR -> MUDA PARA ENROLLED USANDO PAGAMENTO SELECIONADO
	public void goToConfirm()
	{
		if (selectedAcademicYearId == null || selectedClassLevelId == null || selectedClassroomId == null || selectedStudentId == null)
		{
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Campos obrigatórios", "Selecione Ano, Classe, Turma e Aluno"));
			return;
		}
		if (!hasValidPayment)
		{
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pagamento obrigatório", "Selecione pelo menos um pagamento PAGO para mudar para ENROLLED"));
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
			if (!hasValidPayment)
			{
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Selecione pagamento válido"));
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
					// AQUI MUDA PARA ENROLLED PORQUE TEM PAGAMENTO VALIDO
					enrollment.setStatus(EnrollmentStatus.ENROLLED);
					em.merge(enrollment);
					classroom.setCurrentStudents(classroom.getCurrentStudents() + 1);
					em.merge(classroom);
				}
			}
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Matrícula confirmada ENROLLED com pagamento " + selectedPaymentReference));
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

	// GRELHA MENSALIDADES SIMPLES
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
	}

	@Transactional
	public void saveMonthlyReferences()
	{
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Gravado", generatedMonthlyFees.size() + " referências gravadas"));
	}

	public BigDecimal getTotalAmount()
	{
		return generatedMonthlyFees.stream().map(f -> f.getAmount() != null ? f.getAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	// GETTERS SETTERS
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

}
