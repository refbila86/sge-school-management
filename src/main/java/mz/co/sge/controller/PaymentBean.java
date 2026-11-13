package mz.co.sge.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.FeeItemEntity;
import mz.co.sge.entity.PaymentEntity;
import mz.co.sge.entity.PaymentItemEntity;
import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.EnrollmentType;
import mz.co.sge.enumes.PaymentMethod;
import mz.co.sge.service.IEnrollmentService;
import mz.co.sge.service.IFeeItemsService;
import mz.co.sge.service.IPaymentService;
import mz.co.sge.service.IStudentService;

@Component
@ViewScoped
public class PaymentBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	// LISTAGEM
	private List<PaymentEntity> payments = new ArrayList<>();
	private List<PaymentEntity> filteredPayments = new ArrayList<>();
	private PaymentEntity selected = new PaymentEntity();
	private PaymentEntity viewItem;

	// FILTROS
	private String filterAluno = "";
	private String filterCodigoAluno = "";
	private LocalDate filterDataInicio;
	private LocalDate filterDataFim;

	// FORM PAGAMENTO
	private Long selectedStudentId;
	private StudentEntity selectedStudentEntity;
	private PaymentMethod paymentMethod = PaymentMethod.NUMERARIO;
	private String reference = "";
	private List<FeeItemEntity> feeItems = new ArrayList<>();
	private Long selectedFeeItemId;
	private String customDescricao = "";
	private BigDecimal customValor = BigDecimal.ZERO;
	private boolean outroMode = false;
	private List<PaymentItemEntity> cart = new ArrayList<>();
	private BigDecimal cartTotal = BigDecimal.ZERO;

	// STATUS PAGAMENTO - REGRA DE NEGOCIO
	private boolean hasPaidEnrollment = false;
	private boolean hasPaidRegistration = false;
	private boolean hasPaidRenewal = false;
	private boolean readyForEnrollment = false;
	private EnrollmentType currentEnrollmentType = EnrollmentType.FIRST_ENROLLMENT;

	// SERVICES
	@Autowired
	private IPaymentService paymentService;
	@Autowired
	private IStudentService studentService;
	@Autowired
	private IFeeItemsService feeItemsService;
	@Autowired
	private IEnrollmentService enrollmentService;
	@Autowired
	private LoginBean loginBean;
	@Autowired
	private DashboardBean dashboardBean;
	@PersistenceContext
	private EntityManager em;

	// ================= INIT =================
	@PostConstruct
	public void init()
	{
		try
		{
			Long schoolId = loginBean.getSchoolId();
			Long yearId = loginBean.getAcademicYearId();
			if (schoolId != null && yearId != null)
			{
				payments = paymentService.getBySchoolAndYearId(schoolId, yearId);
				filteredPayments = new ArrayList<>(payments);
				feeItems = feeItemsService.getActiveBySchoolAndYearId(schoolId, yearId);
			}
		} catch (Exception e)
		{
			payments = new ArrayList<>();
			filteredPayments = new ArrayList<>();
		}
		if (cart == null)
			cart = new ArrayList<>();
		if (customValor == null)
			customValor = BigDecimal.ZERO;
	}

	public boolean isFirstEnrollment()
	{
		return currentEnrollmentType != null && currentEnrollmentType.name().equals("FIRST_ENROLLMENT");
	}

	public boolean isRenewalEnrollment()
	{
		return currentEnrollmentType != null && !currentEnrollmentType.name().equals("FIRST_ENROLLMENT");
	}

	public void prepareNew()
	{
		selectedStudentId = null;
		selectedStudentEntity = null;
		paymentMethod = PaymentMethod.TRANSFERENCIA_BANCARIA;
		reference = "";
		cart = new ArrayList<>();
		cartTotal = BigDecimal.ZERO;
		selectedFeeItemId = null;
		customDescricao = "";
		customValor = BigDecimal.ZERO;
		outroMode = false;
		hasPaidEnrollment = false;
		hasPaidRegistration = false;
		hasPaidRenewal = false;
		readyForEnrollment = false;
		currentEnrollmentType = EnrollmentType.FIRST_ENROLLMENT;
		Long schoolId = loginBean.getSchoolId();
		Long yearId = loginBean.getAcademicYearId();
		if (schoolId != null && yearId != null)
			feeItems = feeItemsService.getActiveBySchoolAndYearId(schoolId, yearId);
		else
			feeItems = new ArrayList<>();
	}

	// ================= NAVEGACAO =================
	public String prepareView(PaymentEntity p)
	{
		this.viewItem = p;
		this.selected = p;
		dashboardBean.navigate("payments/payment-view", "pagamentos");
		return null;
	}

	public String prepareEdit(PaymentEntity p)
	{
		this.selected = p;
		this.selectedStudentId = p.getStudent().getId();
		this.selectedStudentEntity = p.getStudent();
		this.paymentMethod = p.getMethod();
		this.reference = p.getReference();
		this.cart = new ArrayList<>(p.getItems());
		recalcTotal();
		dashboardBean.navigate("payments/payment-include", "pagamentos");
		return null;
	}

	public void prepareDelete(PaymentEntity p)
	{
		this.selected = p;
	}

	public void delete()
	{
		try
		{
			paymentService.delete(selected.getId());
			init();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Removido"));
		} catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	// ================= AUTOCOMPLETE ALUNO =================
	public List<StudentEntity> completeStudent(String q)
	{
		try
		{
			Long schoolId = loginBean.getSchoolId();
			Long yearId = loginBean.getAcademicYearId();
			if (schoolId == null || yearId == null)
				return List.of();

			String like = "%" + (q == null ? "" : q.toLowerCase().trim()) + "%";
			Long prevYearId = getPreviousYearId(schoolId, yearId);

			StringBuilder jpql = new StringBuilder();
			jpql.append("SELECT s FROM StudentEntity s WHERE 1=1 ");

			// nunca pode já estar matriculado no ano actual
			jpql.append("AND NOT EXISTS (SELECT 1 FROM EnrollmentEntity ec WHERE ec.student = s AND ec.academicYear.id = :yearId) ");

			if (prevYearId != null)
			{
				// matriculados no ano anterior (renovação) OU novos (sem nenhuma matrícula)
				jpql.append("AND (EXISTS (SELECT 1 FROM EnrollmentEntity ep WHERE ep.student = s ");
				jpql.append("             AND ep.school.id = :schoolId AND ep.academicYear.id = :prevYearId) ");
				jpql.append("     OR NOT EXISTS (SELECT 1 FROM EnrollmentEntity ea WHERE ea.student = s)) ");
			} else
			{
				// sem ano anterior: só novos
				jpql.append("AND NOT EXISTS (SELECT 1 FROM EnrollmentEntity ea WHERE ea.student = s) ");
			}

			jpql.append("AND (LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE :like OR LOWER(s.code) LIKE :like) ");
			jpql.append("ORDER BY s.firstName, s.lastName");

			var query = em.createQuery(jpql.toString(), StudentEntity.class).setParameter("yearId", yearId).setParameter("like", like);
			if (prevYearId != null)
			{
				query.setParameter("schoolId", schoolId);
				query.setParameter("prevYearId", prevYearId);
			}
			return query.setMaxResults(20).getResultList();
		} catch (Exception e)
		{
			e.printStackTrace();
			return List.of();
		}
	}

	private Long getPreviousYearId(Long schoolId, Long yearId)
	{
		return em.createQuery("SELECT MAX(e.academicYear.id) FROM EnrollmentEntity e " + "WHERE e.school.id = :schoolId AND e.academicYear.id < :yearId",
				Long.class).setParameter("schoolId", schoolId).setParameter("yearId", yearId).getSingleResult();
	}

	public List<StudentEntity> completeStudents(String q)
	{
		return completeStudent(q);
	}

	public void setSelectedStudentId(Long id)
	{
		this.selectedStudentId = id;
		if (id != null)
		{
			try
			{
				this.selectedStudentEntity = studentService.getByIdAndSchoolAndYear(id, loginBean.getSchoolId(), loginBean.getAcademicYearId());
				if (this.selectedStudentEntity == null)
					this.selectedStudentEntity = studentService.getStudentById(id).orElse(null);
			} catch (Exception e)
			{
				this.selectedStudentEntity = null;
			}
		} else
			this.selectedStudentEntity = null;
	}

	public void onStudentSelect(SelectEvent<StudentEntity> event)
	{
		try
		{
			StudentEntity s = null;
			if (event != null && event.getObject() != null)
				s = event.getObject();
			else if (selectedStudentEntity != null)
				s = selectedStudentEntity;
			if (s == null || s.getId() == null)
				return;
			this.selectedStudentId = s.getId();
			this.selectedStudentEntity = s;
			checkPaymentStatus();
		} catch (Exception e)
		{
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível selecionar aluno"));
		}
	}

	// ================= REGRA PRINCIPAL: PRIMEIRO ANO vs RENOVACAO
	// =================
	public void checkPaymentStatus()
	{
		try
		{
			if (selectedStudentEntity == null || selectedStudentEntity.getId() == null)
				return;
			Long yearId = loginBean.getAcademicYearId();
			Long schoolId = loginBean.getSchoolId();
			if (yearId == null || schoolId == null)
				return;
			if (selectedStudentId == null)
				selectedStudentId = selectedStudentEntity.getId();

			// 1. Tipo: primeiro ano ou renovação?
			try
			{
				currentEnrollmentType = enrollmentService.getEnrollmentTypeForStudent(selectedStudentId);
			} catch (Exception e)
			{
				Long count = em.createQuery("SELECT COUNT(e) FROM EnrollmentEntity e WHERE e.student.id = :sid AND e.academicYear.id != :yearId", Long.class)
						.setParameter("sid", selectedStudentId).setParameter("yearId", yearId).getSingleResult();
				currentEnrollmentType = (count != null && count > 0) ? EnrollmentType.RENEWAL : EnrollmentType.FIRST_ENROLLMENT;
			}
			if (currentEnrollmentType == null)
				currentEnrollmentType = EnrollmentType.FIRST_ENROLLMENT;

			// 2. O que já pagou no ano
			hasPaidEnrollment = paymentService.hasPaid(selectedStudentId, yearId, schoolId, "inscri");
			hasPaidRegistration = paymentService.hasPaid(selectedStudentId, yearId, schoolId, "matric");
			hasPaidRenewal = paymentService.hasPaid(selectedStudentId, yearId, schoolId, "renov");

			// 3. Regra que pediste
			if (currentEnrollmentType == EnrollmentType.FIRST_ENROLLMENT)
			{
				readyForEnrollment = hasPaidEnrollment && hasPaidRegistration;
			} else
			{
				readyForEnrollment = hasPaidRenewal;
			}

			// 4. Filtra taxas
			feeItems = feeItemsService.getActiveBySchoolAndYearId(schoolId, yearId);
			if (currentEnrollmentType == EnrollmentType.FIRST_ENROLLMENT)
			{
				if (hasPaidEnrollment)
					feeItems.removeIf(f -> f.getDescricao().toLowerCase().contains("inscri"));
				if (hasPaidRegistration)
					feeItems.removeIf(f -> f.getDescricao().toLowerCase().contains("matric"));
			} else
			{
				if (hasPaidRenewal)
					feeItems.removeIf(f -> f.getDescricao().toLowerCase().contains("renov"));
				feeItems.removeIf(f ->
				{
					String d = f.getDescricao().toLowerCase();
					return d.contains("inscri") && !d.contains("renov");
				});
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void checkPaymentStatusWithCart()
	{
		boolean cartHasInsc = cart.stream().anyMatch(i -> i.getDescricao().toLowerCase().contains("inscri"));
		boolean cartHasMatric = cart.stream().anyMatch(i -> i.getDescricao().toLowerCase().contains("matric"));
		boolean cartHasRenov = cart.stream().anyMatch(i -> i.getDescricao().toLowerCase().contains("renov"));

		boolean finalInsc = hasPaidEnrollment || cartHasInsc;
		boolean finalMatric = hasPaidRegistration || cartHasMatric;
		boolean finalRenov = hasPaidRenewal || cartHasRenov;

		if (currentEnrollmentType == EnrollmentType.FIRST_ENROLLMENT)
		{
			readyForEnrollment = finalInsc && finalMatric;
		} else
		{
			readyForEnrollment = finalRenov;
		}
	}

	// ================= CARRINHO =================
	public void onFeeItemSelect()
	{
		if (selectedFeeItemId != null)
		{
			FeeItemEntity fi = feeItems.stream().filter(f -> f.getId().equals(selectedFeeItemId)).findFirst().orElse(null);
			if (fi != null)
			{
				customDescricao = fi.getDescricao();
				customValor = fi.getValor();
				outroMode = false;
			}
		}
	}

	public void enableOutro()
	{
		outroMode = true;
		selectedFeeItemId = null;
		customDescricao = "";
		customValor = BigDecimal.ZERO;
	}

	public void addItem()
	{
		if (customDescricao == null || customDescricao.isBlank() || customValor == null || customValor.compareTo(BigDecimal.ZERO) <= 0)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Informe descrição e valor"));
			return;
		}
		String lower = customDescricao.toLowerCase();
		if (lower.contains("inscri") && hasPaidEnrollment)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bloqueado", "Inscrição já paga este ano!"));
			return;
		}
		if (lower.contains("matric") && hasPaidRegistration)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bloqueado", "Matrícula já paga este ano!"));
			return;
		}
		if (lower.contains("renov") && hasPaidRenewal)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bloqueado", "Renovação já paga este ano!"));
			return;
		}
		String descCheck = customDescricao.trim().toLowerCase();
		boolean exists = cart.stream().anyMatch(i -> i.getDescricao().trim().toLowerCase().equals(descCheck));
		if (exists)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Duplicado", "Item já no carrinho"));
			return;
		}
		PaymentItemEntity item = new PaymentItemEntity();
		item.setDescricao(customDescricao);
		item.setValor(customValor);
		item.setQuantidade(BigDecimal.ONE);
		item.calc();
		cart.add(item);
		recalcTotal();
		customDescricao = "";
		customValor = BigDecimal.ZERO;
		selectedFeeItemId = null;
		outroMode = false;
		checkPaymentStatusWithCart();
	}

	public void removeItem(PaymentItemEntity item)
	{
		cart.remove(item);
		recalcTotal();
		checkPaymentStatusWithCart();
	}

	private void recalcTotal()
	{
		cartTotal = cart.stream().map(PaymentItemEntity::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	// ================= CONFIRMACAO =================
	public String goToConfirm()
	{
		if (selectedStudentId == null)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Selecione aluno"));
			return null;
		}
		if (cart.isEmpty())
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Carrinho vazio"));
			return null;
		}
		dashboardBean.navigate("payments/payment-confirm", "pagamentos");
		return null;
	}

	public String backToInclude()
	{
		dashboardBean.navigate("payments/payment-include", "pagamentos");
		return null;
	}

	public String confirmPaymentAndGoToList()
	{
		try
		{
			Long schoolId = loginBean.getSchoolId();
			Long yearId = loginBean.getAcademicYearId();
			if (cart.isEmpty())
			{
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Carrinho vazio"));
				return null;
			}
			PaymentEntity saved;
			if (selected == null || selected.getId() == null)
			{
				StudentEntity aluno = studentService.getByIdAndSchoolAndYear(selectedStudentId, schoolId, yearId);
				if (aluno == null)
					aluno = studentService.getStudentById(selectedStudentId).orElse(null);
				if (aluno == null)
				{
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Aluno não encontrado"));
					return null;
				}
				PaymentEntity p = new PaymentEntity();
				p.setStudent(aluno);
				SchoolEntity sch = new SchoolEntity();
				sch.setId(schoolId);
				p.setSchool(sch);
				p.setAcademicYear(loginBean.getLoggedAcademicYear());
				p.setTotalAmount(cartTotal);
				p.setMethod(paymentMethod);
				p.setReference(reference);
				p.setCreatedBy(loginBean.getLoggedUser());
				saved = paymentService.createWithItems(p, cart);
			} else
				saved = paymentService.updateWithItems(selected.getId(), cart, paymentMethod, reference);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pagamento " + saved.getReceiptNumber()));
			init();
			prepareNew();
			dashboardBean.navigate("payments/payment-list", "pagamentos");
		} catch (Exception e)
		{
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
		return null;
	}

	public String confirmPaymentAndGoToLists()
	{
		return confirmPaymentAndGoToList();
	}

	// ================= FILTROS & UTILS =================
	public void filter()
	{
		try
		{
			filteredPayments = payments.stream()
					.filter(p -> filterAluno == null || filterAluno.isBlank() || p.getStudent().getFullName().toLowerCase().contains(filterAluno.toLowerCase()))
					.filter(p -> filterCodigoAluno == null || filterCodigoAluno.isBlank()
							|| p.getStudent().getCode().toLowerCase().contains(filterCodigoAluno.toLowerCase()))
					.filter(p -> filterDataInicio == null || !p.getCreatedAt().toLocalDate().isBefore(filterDataInicio))
					.filter(p -> filterDataFim == null || !p.getCreatedAt().toLocalDate().isAfter(filterDataFim)).collect(Collectors.toList());
		} catch (Exception e)
		{
			filteredPayments = new ArrayList<>(payments);
		}
	}

	public void clearFilters()
	{
		filterAluno = "";
		filterCodigoAluno = "";
		filterDataInicio = null;
		filterDataFim = null;
		filteredPayments = new ArrayList<>(payments);
	}

	public String formatDateTime(java.time.LocalDateTime dt)
	{
		return dt == null ? "—" : dt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
	}

	public String formatDate(LocalDate d)
	{
		return d == null ? "—" : d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}

	public String getCreatedByName()
	{
		if (selected == null || selected.getCreatedBy() == null)
			return "Sistema";
		try
		{
			return selected.getCreatedBy().getUsername();
		} catch (Exception e)
		{
			return "Sistema";
		}
	}

	// ================= GETTERS / SETTERS =================
	public List<PaymentEntity> getPayments()
	{
		return payments;
	}

	public void setPayments(List<PaymentEntity> payments)
	{
		this.payments = payments;
	}

	public List<PaymentEntity> getFilteredPayments()
	{
		return filteredPayments;
	}

	public void setFilteredPayments(List<PaymentEntity> l)
	{
		this.filteredPayments = l;
	}

	public PaymentEntity getSelected()
	{
		return selected;
	}

	public void setSelected(PaymentEntity s)
	{
		this.selected = s;
	}

	public String getFilterAluno()
	{
		return filterAluno;
	}

	public void setFilterAluno(String s)
	{
		this.filterAluno = s;
	}

	public String getFilterCodigoAluno()
	{
		return filterCodigoAluno;
	}

	public void setFilterCodigoAluno(String s)
	{
		this.filterCodigoAluno = s;
	}

	public LocalDate getFilterDataInicio()
	{
		return filterDataInicio;
	}

	public void setFilterDataInicio(LocalDate d)
	{
		this.filterDataInicio = d;
	}

	public LocalDate getFilterDataFim()
	{
		return filterDataFim;
	}

	public void setFilterDataFim(LocalDate d)
	{
		this.filterDataFim = d;
	}

	public Long getSelectedStudentId()
	{
		return selectedStudentId;
	}

	public StudentEntity getSelectedStudentEntity()
	{
		return selectedStudentEntity;
	}

	public void setSelectedStudentEntity(StudentEntity s)
	{
		this.selectedStudentEntity = s;
		this.selectedStudentId = s != null ? s.getId() : null;
	}

	public PaymentMethod getPaymentMethod()
	{
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod m)
	{
		this.paymentMethod = m;
	}

	public String getReference()
	{
		return reference;
	}

	public void setReference(String r)
	{
		this.reference = r;
	}

	public List<FeeItemEntity> getFeeItems()
	{
		return feeItems;
	}

	public void setFeeItems(List<FeeItemEntity> l)
	{
		this.feeItems = l;
	}

	public Long getSelectedFeeItemId()
	{
		return selectedFeeItemId;
	}

	public void setSelectedFeeItemId(Long id)
	{
		this.selectedFeeItemId = id;
	}

	public String getCustomDescricao()
	{
		return customDescricao;
	}

	public void setCustomDescricao(String s)
	{
		this.customDescricao = s;
	}

	public BigDecimal getCustomValor()
	{
		return customValor;
	}

	public void setCustomValor(BigDecimal v)
	{
		this.customValor = v;
	}

	public boolean isOutroMode()
	{
		return outroMode;
	}

	public void setOutroMode(boolean b)
	{
		this.outroMode = b;
	}

	public List<PaymentItemEntity> getCart()
	{
		return cart;
	}

	public void setCart(List<PaymentItemEntity> c)
	{
		this.cart = c;
	}

	public BigDecimal getCartTotal()
	{
		return cartTotal;
	}

	public void setCartTotal(BigDecimal v)
	{
		this.cartTotal = v;
	}

	public PaymentEntity getViewItem()
	{
		return viewItem;
	}

	public void setViewItem(PaymentEntity v)
	{
		this.viewItem = v;
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

	public boolean isReadyForEnrollment()
	{
		return readyForEnrollment;
	}

	public EnrollmentType getCurrentEnrollmentType()
	{
		return currentEnrollmentType;
	}

	public PaymentMethod[] getMethods()
	{
		return PaymentMethod.values();
	}
}