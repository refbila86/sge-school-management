package mz.co.sge.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.MonthlyFeeEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.PaymentMethod;
import mz.co.sge.service.IAcademicYearService;
import mz.co.sge.service.IMonthlyFeeService;

@Named
@SessionScoped
public class MonthlyFeeBean implements Serializable
{
	@Autowired
	private IMonthlyFeeService monthlyFeeService;
	@Autowired
	private IAcademicYearService academicYearService;

	@Autowired
	private DashboardBean dashboardBean;

	private int filterYear = LocalDate.now().getYear();
	private Integer filterMonth = LocalDate.now().getMonthValue();
	private String searchStudent;
	private String searchCode;

	private List<MonthlyFeeEntity> allFees;
	private List<MonthlyFeeEntity> unpaidFees;
	private List<MonthlyFeeEntity> selectedFees;
	private MonthlyFeeEntity printFee;
	private StudentEntity foundStudent;
	private List<AcademicYearEntity> academicYears;

	private PaymentMethod paymentMethod = PaymentMethod.NUMERARIO;
	private String bankName;
	private String transactionNumber;
	private LocalDate paymentDate = LocalDate.now();
	private List<String> banks = List.of("BCI", "BIM - Millennium", "Standard Bank", "Moza Banco", "Absa", "FNB", "Nedbank", "Banco Unico");
	private List<MonthItem> months;

	// NOVO FLUXO CONFIRM
	private List<FeePayItem> payItems;

	@PostConstruct
	public void init()
	{
		months = new ArrayList<>();
		for (int i = 1; i <= 12; i++)
		{
			months.add(new MonthItem(i, Month.of(i).getDisplayName(TextStyle.FULL, new Locale("pt", "MZ"))));
		}
		try
		{
			academicYears = academicYearService.getAllAcademicYears();
		} catch (Exception e)
		{
			academicYears = List.of();
		}
		loadAll();
	}

	public static class MonthItem
	{
		private int value;
		private String label;

		public MonthItem(int v, String l)
		{
			value = v;
			label = l;
		}

		public int getValue()
		{
			return value;
		}

		public String getLabel()
		{
			return label;
		}
	}

	public static class FeePayItem implements Serializable
	{
		private MonthlyFeeEntity fee;
		private PaymentMethod paymentMethod = PaymentMethod.NUMERARIO;
		private String bankName;
		private String transactionNumber;
		private LocalDate paymentDate = LocalDate.now();
		private BigDecimal multa;
		private BigDecimal total;

		public FeePayItem(MonthlyFeeEntity f)
		{
			this.fee = f;
		}

		public MonthlyFeeEntity getFee()
		{
			return fee;
		}

		public PaymentMethod getPaymentMethod()
		{
			return paymentMethod;
		}

		public void setPaymentMethod(PaymentMethod p)
		{
			paymentMethod = p;
		}

		public String getBankName()
		{
			return bankName;
		}

		public void setBankName(String b)
		{
			bankName = b;
		}

		public String getTransactionNumber()
		{
			return transactionNumber;
		}

		public void setTransactionNumber(String t)
		{
			transactionNumber = t;
		}

		public LocalDate getPaymentDate()
		{
			return paymentDate;
		}

		public void setPaymentDate(LocalDate d)
		{
			paymentDate = d;
		}

		public BigDecimal getMulta()
		{
			return multa;
		}

		public void setMulta(BigDecimal m)
		{
			multa = m;
		}

		public BigDecimal getTotal()
		{
			return total;
		}

		public void setTotal(BigDecimal t)
		{
			total = t;
		}
	}

	public void loadAll()
	{
		allFees = monthlyFeeService.findPaidByYear(filterYear);
	}

	public List<MonthlyFeeEntity> getFilteredFees()
	{
		if (allFees == null)
			return List.of();
		return allFees.stream().filter(m ->
		{
			boolean ok = true;
			if (filterMonth != null)
				ok = ok && m.getMonth().equals(filterMonth);
			if (searchStudent != null && !searchStudent.isBlank())
			{
				String s = searchStudent.toLowerCase();
				ok = ok && (m.getStudent().getCode().toLowerCase().contains(s) || m.getStudent().getFullName().toLowerCase().contains(s)
						|| m.getReference().toLowerCase().contains(s));
			}
			return ok;
		}).toList();
	}

	public void searchByStudentCode()
	{
		foundStudent = monthlyFeeService.findStudentByCode(searchCode);
		unpaidFees = monthlyFeeService.findUnpaidByStudentCode(searchCode);
	}

	public void preparePrint(MonthlyFeeEntity m)
	{
		this.printFee = m;
	}

	// 1 - include -> confirm
	public void goToConfirm()
	{
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (selectedFees == null || selectedFees.isEmpty())
		{
			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Selecione pelo menos uma referência", null));
			return;
		}
		if (paymentMethod == PaymentMethod.TRANSFERENCIA_BANCARIA
				&& (bankName == null || bankName.isBlank() || transactionNumber == null || transactionNumber.isBlank()))
		{
			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Indique o banco e o nº de transação", null));
			return;
		}
		if (paymentDate == null)
		{
			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Indique a data de pagamento", null));
			return;
		}
		dashboardBean.navigate("monthly-fee/monthly-fee-confirm", "mensalidades");
	}

	// 2 - confirm -> list
	public void confirmPaymentAndRedirect()
	{
		FacesContext ctx = FacesContext.getCurrentInstance();
		try
		{
			if (selectedFees == null || selectedFees.isEmpty())
			{
				ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Nenhuma referência selecionada", null));
				return;
			}

			for (MonthlyFeeEntity f : selectedFees)
			{
				monthlyFeeService.payFee(f.getId(), paymentMethod, bankName, transactionNumber, paymentDate);
			}

			int total = selectedFees.size();
			selectedFees = null;
			unpaidFees = null;
			foundStudent = null;
			searchCode = null;
			bankName = null;
			transactionNumber = null;
			paymentMethod = PaymentMethod.NUMERARIO;
			paymentDate = LocalDate.now();

			filterMonth = null;
			searchStudent = null;

			loadAll();

			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Pagamento registado com sucesso", total + " referência(s) paga(s)"));
			dashboardBean.navigate("monthly-fee/monthly-fee-list", "mensalidades");
		} catch (Exception e)
		{
			e.printStackTrace();
			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao registar pagamento", e.getMessage()));
		}
	}

	public BigDecimal getTotalValor()
	{
		return getFilteredFees().stream().map(m -> m.getAmount() != null ? m.getAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public void clearFilters()
	{
		filterMonth = null;
		searchStudent = null;
		filterYear = LocalDate.now().getYear();
		loadAll();
	}

	public String goToConfirms()
	{
		if (selectedFees == null || selectedFees.isEmpty())
			return null;
		payItems = new ArrayList<>();
		for (MonthlyFeeEntity f : selectedFees)
		{
			FeePayItem it = new FeePayItem(f);
			it.setMulta(calcMulta(f));
			it.setTotal(calcTotalPagoComMulta(f));
			payItems.add(it);
		}
		return "monthly-fee/monthly-fee-confirm?faces-redirect=true";
	}

	public BigDecimal getTotalSelecionado()
	{
		if (selectedFees == null)
			return BigDecimal.ZERO;
		return selectedFees.stream().map(this::calcTotalPagoComMulta).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getMultaSelecionada()
	{
		if (selectedFees == null)
			return BigDecimal.ZERO;
		return selectedFees.stream().map(this::calcMulta).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public String getStudentCodeSearch()
	{
		return searchCode;
	}

	public void setStudentCodeSearch(String s)
	{
		this.searchCode = s;
	}

	// 2 - confirm -> list
	public String confirmPaymentAndRedirectss()
	{
		if (payItems != null)
		{
			for (FeePayItem it : payItems)
			{
				MonthlyFeeEntity f = it.getFee();
				f.setPaymentMethod(it.getPaymentMethod());
				f.setBankName(it.getBankName());
				f.setTransactionNumber(it.getTransactionNumber());
				f.setPaymentDate(it.getPaymentDate());
				monthlyFeeService.payFee(f.getId(), paymentMethod, bankName, transactionNumber, paymentDate);
			}
		}
		payItems = null;
		selectedFees = null;
		unpaidFees = null;
		searchCode = null;
		loadAll();
		return "monthly-fee/monthly-fee-list?faces-redirect=true";
	}

	public BigDecimal calcMulta(MonthlyFeeEntity f)
	{
		if (f == null || f.getAmount() == null || f.getDueDate() == null)
			return BigDecimal.ZERO;
		if (f.getFineAmount() != null && f.getFineAmount().compareTo(BigDecimal.ZERO) > 0)
			return f.getFineAmount();
		LocalDate base = f.getPaymentDate() != null ? f.getPaymentDate() : LocalDate.now();
		if (f.getDueDate().isBefore(base))
			return f.getAmount().multiply(new BigDecimal("0.10"));
		return BigDecimal.ZERO;
	}

	public BigDecimal calcTotalPagoComMulta(MonthlyFeeEntity f)
	{
		if (f == null || f.getAmount() == null)
			return BigDecimal.ZERO;
		if (f.getTotalPaid() != null && f.getTotalPaid().compareTo(BigDecimal.ZERO) > 0)
			return f.getTotalPaid();
		return f.getAmount().add(calcMulta(f));
	}

	public BigDecimal getTotalPago()
	{
		return getFilteredFees().stream()
				.map(m -> m.getTotalPaid() != null && m.getTotalPaid().compareTo(BigDecimal.ZERO) > 0 ? m.getTotalPaid() : m.getAmount())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getTotalMulta()
	{
		return getFilteredFees().stream().map(this::calcMulta).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public long getTotalAlunosPagos()
	{
		return getFilteredFees().stream().map(m -> m.getStudent().getId()).distinct().count();
	}

	// GETTERS SETTERS
	public int getFilterYear()
	{
		return filterYear;
	}

	public void setFilterYear(int y)
	{
		filterYear = y;
	}

	public Integer getFilterMonth()
	{
		return filterMonth;
	}

	public void setFilterMonth(Integer m)
	{
		filterMonth = m;
	}

	public String getSearchStudent()
	{
		return searchStudent;
	}

	public void setSearchStudent(String s)
	{
		searchStudent = s;
	}

	public String getSearchCode()
	{
		return searchCode;
	}

	public void setSearchCode(String c)
	{
		searchCode = c;
	}

	public List<MonthlyFeeEntity> getAllFees()
	{
		return allFees;
	}

	public List<MonthlyFeeEntity> getUnpaidFees()
	{
		return unpaidFees;
	}

	public void setUnpaidFees(List<MonthlyFeeEntity> l)
	{
		unpaidFees = l;
	}

	public List<MonthlyFeeEntity> getSelectedFees()
	{
		return selectedFees;
	}

	public void setSelectedFees(List<MonthlyFeeEntity> l)
	{
		selectedFees = l;
	}

	public MonthlyFeeEntity getPrintFee()
	{
		return printFee;
	}

	public void setPrintFee(MonthlyFeeEntity p)
	{
		printFee = p;
	}

	public StudentEntity getFoundStudent()
	{
		return foundStudent;
	}

	public List<AcademicYearEntity> getAcademicYears()
	{
		return academicYears;
	}

	public PaymentMethod getPaymentMethod()
	{
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod p)
	{
		paymentMethod = p;
	}

	public String getBankName()
	{
		return bankName;
	}

	public void setBankName(String b)
	{
		bankName = b;
	}

	public String getTransactionNumber()
	{
		return transactionNumber;
	}

	public void setTransactionNumber(String t)
	{
		transactionNumber = t;
	}

	public LocalDate getPaymentDate()
	{
		return paymentDate;
	}

	public void setPaymentDate(LocalDate d)
	{
		paymentDate = d;
	}

	public List<String> getBanks()
	{
		return banks;
	}

	public List<MonthItem> getMonths()
	{
		return months;
	}

	public List<FeePayItem> getPayItems()
	{
		return payItems;
	}

	public void setPayItems(List<FeePayItem> p)
	{
		payItems = p;
	}
}