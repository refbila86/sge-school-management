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
import lombok.Getter;
import lombok.Setter;
import mz.co.sge.entity.FeeItemEntity;
import mz.co.sge.entity.PaymentEntity;
import mz.co.sge.entity.PaymentItemEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.PaymentMethod;
import mz.co.sge.enumes.StudentStatus;
import mz.co.sge.service.IFeeItemsService;
import mz.co.sge.service.IPaymentService;
import mz.co.sge.service.IStudentService;

@Component
@ViewScoped
@Getter
@Setter
public class PaymentBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	// LISTAGEM
	private List<PaymentEntity> payments = new ArrayList<>();
	private List<PaymentEntity> filteredPayments = new ArrayList<>();
	private PaymentEntity selected = new PaymentEntity();

	// FILTROS
	private String filterAluno = "";
	private String filterCodigoAluno = "";
	private LocalDate filterDataInicio;
	private LocalDate filterDataFim;

	// INCLUSÃO
	private Long selectedStudentId;
	private StudentEntity selectedStudentEntity;
	private PaymentMethod method = PaymentMethod.NUMERARIO;
	private String reference = "";
	private List<FeeItemEntity> feeItems = new ArrayList<>();
	private Long selectedFeeItemId;
	private String customDescricao = "";
	private BigDecimal customValor = BigDecimal.ZERO;
	private boolean outroMode = false;
	private List<PaymentItemEntity> cart = new ArrayList<>();
	private BigDecimal cartTotal = BigDecimal.ZERO;

	private PaymentEntity viewItem;

	@Autowired
	private IPaymentService paymentService;

	@Autowired
	private IStudentService studentService;

	@Autowired
	private IFeeItemsService feeItemsService;

	@Autowired
	private LoginBean loginBean;

	@Autowired
	private DashboardBean dashboardBean;
	
	@Autowired
	private WelcomeBean welcomeBean;

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

	public void prepareNew()
	{
		selectedStudentId = null;
		selectedStudentEntity = null;
		setMethod(PaymentMethod.NUMERARIO);
		reference = "";
		cart = new ArrayList<>();
		cartTotal = BigDecimal.ZERO;
		selectedFeeItemId = null;
		customDescricao = "";
		customValor = BigDecimal.ZERO;
		outroMode = false;
		Long schoolId = loginBean.getSchoolId();
		Long yearId = loginBean.getAcademicYearId();
		if (schoolId != null && yearId != null)
		{
			feeItems = feeItemsService.getActiveBySchoolAndYearId(schoolId, yearId);
		} else
		{
			feeItems = new ArrayList<>();
		}
	}

	public String prepareView(PaymentEntity p)
	{
		this.viewItem = p;
		this.selected = p;
		dashboardBean.navigate("payments/payment-view", "pagamentos");
		return null;
	}

	public String getCreatedByName()
	{
		if (selected == null || selected.getCreatedBy() == null)
			return "Sistema";
		try
		{
			return selected.getCreatedBy().getUsername();
		} catch (org.hibernate.LazyInitializationException e)
		{
			return "Sistema";
		}
	}

	public String prepareEdit(PaymentEntity p)
	{
		this.selected = p;
		this.selectedStudentId = p.getStudent().getId();
		this.selectedStudentEntity = p.getStudent();
		this.setMethod(p.getMethod());
		this.reference = p.getReference();
		this.cart = new ArrayList<>(p.getItems());
		recalcTotal();
		dashboardBean.navigate("payments/payment-include", "pagamentos");
		return null;
	}

	// AUTOCOMPLETE - permite digitar nome
	public List<StudentEntity> completeStudent(String q)
	{
		try
		{
			Long schoolId = loginBean.getSchoolId();
			Long yearId = loginBean.getAcademicYearId();
			if (schoolId == null || yearId == null)
				return List.of();

			// Só alunos com matrícula pendente
			List<StudentEntity> pending = studentService.getStudentsBySchoolIdAndAcademicYearId(schoolId, yearId).stream()
					.filter(s -> s.getStatus() == StudentStatus.PENDING_ENROLLMENT).collect(Collectors.toList());

			if (q == null || q.isBlank())
			{
				return pending.stream().limit(20).collect(Collectors.toList());
			}

			String term = q.toLowerCase().trim();
			return pending.stream()
					.filter(s -> (s.getFullName() != null && s.getFullName().toLowerCase().contains(term))
							|| (s.getCode() != null && s.getCode().toLowerCase().contains(term))
							|| (s.getFirstName() != null && s.getFirstName().toLowerCase().contains(term)))
					.limit(15).collect(Collectors.toList());
		} catch (Exception e)
		{
			e.printStackTrace();
			return List.of();
		}
	}

	public List<StudentEntity> completeStudents(String q)
	{
		try
		{
			Long schoolId = loginBean.getSchoolId();
			Long yearId = loginBean.getAcademicYearId();
			if (schoolId == null || yearId == null)
				return List.of();

			List<StudentEntity> all = studentService.getStudentsBySchoolIdAndAcademicYearId(schoolId, yearId);

			List<StudentEntity> pending = all.stream().filter(s -> s.getStatus() == StudentStatus.PENDING_ENROLLMENT).collect(Collectors.toList());

			if (q == null || q.isBlank())
			{
				return all.stream().limit(20).collect(Collectors.toList());
			}
			String term = q.toLowerCase().trim();
			return all.stream()
					.filter(s -> (s.getFullName() != null && s.getFullName().toLowerCase().contains(term))
							|| (s.getCode() != null && s.getCode().toLowerCase().contains(term))
							|| (s.getFirstName() != null && s.getFirstName().toLowerCase().contains(term)))
					.limit(15).collect(Collectors.toList());
		} catch (Exception e)
		{
			e.printStackTrace();
			return List.of();
		}
	}

	public void setSelectedStudentId(Long id)
	{
		this.selectedStudentId = id;
		if (id != null)
		{
			try
			{
				this.selectedStudentEntity = studentService.getByIdAndSchoolAndYear(id, loginBean.getSchoolId(), loginBean.getAcademicYearId());
			} catch (Exception e)
			{
				this.selectedStudentEntity = null;
			}
		} else
		{
			this.selectedStudentEntity = null;
		}
	}

	public void onStudentSelect(SelectEvent<StudentEntity> event)
	{
		StudentEntity s = event.getObject();
		this.selectedStudentId = s.getId();
		this.selectedStudentEntity = s;
	}

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
		String descCheck = customDescricao.trim().toLowerCase();
		boolean exists = cart.stream().anyMatch(i -> i.getDescricao().trim().toLowerCase().equals(descCheck));
		if (exists)
		{
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Item duplicado", "O item '" + customDescricao + "' já foi adicionado"));
			return;
		}
		PaymentItemEntity item = new PaymentItemEntity();
		item.setDescricao(customDescricao);
		item.setValor(customValor);
		cart.add(item);
		recalcTotal();
		customDescricao = "";
		customValor = BigDecimal.ZERO;
		selectedFeeItemId = null;
		outroMode = false;
	}

	public void removeItem(PaymentItemEntity item)
	{
		cart.remove(item);
		recalcTotal();
	}

	private void recalcTotal()
	{
		cartTotal = cart.stream().map(PaymentItemEntity::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	// NAVEGACAO - payment-include -> payment-confirm
	public String goToConfirm()
	{
		if (selectedStudentId == null)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Selecione o aluno digitando o nome"));
			return null;
		}
		if (cart.isEmpty())
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Carrinho vazio"));
			return null;
		}
		if (selectedStudentEntity == null)
		{
			try
			{
				selectedStudentEntity = studentService.getByIdAndSchoolAndYear(selectedStudentId, loginBean.getSchoolId(), loginBean.getAcademicYearId());
			} catch (Exception e)
			{
			}
		}
		dashboardBean.navigate("payments/payment-confirm", "pagamentos");
		return null;
	}

	public String backToInclude()
	{
		dashboardBean.navigate("payments/payment-include", "pagamentos");
		return null;
	}

	public String formatDateTime(java.time.LocalDateTime dt)
	{
		if (dt == null)
			return "—";
		return dt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
	}

	public String formatDate(java.time.LocalDate d)
	{
		if (d == null)
			return "—";
		return d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}

	public String confirmPaymentAndGoToList()
	{
		try
		{
			
			Long schoolId = loginBean.getSchoolId();
			Long yearId = loginBean.getAcademicYearId();

			if (cart == null || cart.isEmpty())
			{
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Carrinho vazio"));
				return null;
			}

			PaymentEntity saved;

			if (selected == null || selected.getId() == null)
			{
				// ===== NOVO PAGAMENTO =====
				if (selectedStudentId == null)
				{
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Selecione um aluno"));
					return null;
				}

				StudentEntity aluno = studentService.getByIdAndSchoolAndYear(selectedStudentId, schoolId, yearId);
				if (aluno == null)
				{
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Aluno não encontrado"));
					return null;
				}

				if (aluno.getStatus() != StudentStatus.PENDING_ENROLLMENT)
				{
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso",
							"Só alunos PENDING_ENROLLMENT podem pagar aqui. Este está " + aluno.getStatus()));
					return null;
				}

				PaymentEntity p = new PaymentEntity();
				p.setStudent(aluno);
				p.setSchool(aluno.getSchool());
				p.setAcademicYear(loginBean.getLoggedAcademicYear());
				p.setTotalAmount(cartTotal);
				p.setMethod(getMethod());
				p.setReference(reference);
				p.setCreatedBy(loginBean.getLoggedUser());

				saved = paymentService.createWithItems(p, cart);

			} else
			{
				// ===== ATUALIZAÇÃO - é aqui que dava detached =====
				saved = paymentService.updateWithItems(selected.getId(), cart, getMethod(), reference);
				
			}

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pagamento " + saved.getReceiptNumber() + " registado"));

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
		try
		{
			Long schoolId = loginBean.getSchoolId();
			Long yearId = loginBean.getAcademicYearId();
			StudentEntity aluno = studentService.getByIdAndSchoolAndYear(selectedStudentId, schoolId, yearId);
			if (aluno == null)
			{
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Aluno não encontrado"));
				return null;
			}
			PaymentEntity p = new PaymentEntity();
			p.setStudent(aluno);
			p.setSchool(aluno.getSchool());
			p.setAcademicYear(loginBean.getLoggedAcademicYear());
			p.setTotalAmount(cartTotal);
			p.setMethod(getMethod());
			p.setReference(reference);
			p.setCreatedBy(loginBean.getLoggedUser());
			PaymentEntity saved = paymentService.createWithItems(p, cart);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pagamento " + saved.getReceiptNumber() + " registado"));
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

	public PaymentEntity getViewItem()
	{
		return viewItem;
	}

	public PaymentMethod[] getMethods()
	{
		return PaymentMethod.values();
	}

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

	public void setFilteredPayments(List<PaymentEntity> filteredPayments)
	{
		this.filteredPayments = filteredPayments;
	}

	public PaymentEntity getSelected()
	{
		return selected;
	}

	public void setSelected(PaymentEntity selected)
	{
		this.selected = selected;
	}

	// COMPATIBILIDADE
	@Deprecated
	public StudentEntity getSelectedStudent()
	{
		return selectedStudentEntity;
	}

	@Deprecated
	public void setSelectedStudent(StudentEntity s)
	{
		this.selectedStudentEntity = s;
		this.selectedStudentId = s != null ? s.getId() : null;
	}

	public PaymentMethod getMethod()
	{
		return method;
	}

	public void setMethod(PaymentMethod method)
	{
		this.method = method;
	}
}