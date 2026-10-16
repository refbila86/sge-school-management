package mz.co.sge.controller;

import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.MonthlyFeeEntity;
import mz.co.sge.service.IMonthlyFeeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component("monthlyFeeBean")
@Scope("session")
public class MonthlyFeeBean implements Serializable
{

	@Autowired
	private IMonthlyFeeService monthlyFeeService;
	@Autowired
	private LoginBean loginBean;
	@Autowired
	private DashboardBean dashboardBean;

	@PersistenceContext
	private EntityManager em;

	private List<MonthlyFeeEntity> monthlyFees;
	private EnrollmentEntity currentEnrollment;

	// Campos para gerar
	private Integer startMonth;
	private List<MonthOption> months;
	private String selectedYear = "2026";
	private BigDecimal defaultAmount = new BigDecimal("2500.00");
	private Integer defaultDueDay = 10;
	private List<MonthlyFeeDTO> previewFees = new ArrayList<>();

	@PostConstruct
	public void init()
	{
		loadMonths();
	}

	public void loadMonths()
	{
		months = new ArrayList<>();
		String[] pt =
		{ "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" };
		for (int i = 1; i <= 12; i++)
			months.add(new MonthOption(i, pt[i - 1] + " (" + String.format("%02d", i) + ")"));
	}

	// Chamado a partir do enrollment-view
	public void setCurrentEnrollment(EnrollmentEntity e)
	{
		this.currentEnrollment = e;
		this.startMonth = null;
		this.previewFees = new ArrayList<>();
		if (e != null && e.getAcademicYear() != null)
		{
			try
			{
				selectedYear = e.getAcademicYear().getYear().toString().substring(0, 4);
			} catch (Exception ex)
			{
				selectedYear = "2026";
			}
		}
		loadByEnrollment();
	}

	public void loadByEnrollment()
	{
		if (currentEnrollment != null)
		{
			monthlyFees = monthlyFeeService.findByEnrollmentId(currentEnrollment.getId());
		} else
		{
			monthlyFees = List.of();
		}
	}

	public String getPreviewReference()
	{
		if (currentEnrollment == null)
			return "ESTXXX-0-2026-MM";
		String sc = currentEnrollment.getStudent() != null ? currentEnrollment.getStudent().getCode() : "SEM-COD";
		String eid = currentEnrollment.getId() != null ? currentEnrollment.getId().toString() : "0";
		String m = startMonth != null ? String.format("%02d", startMonth) : "MM";
		return sc + "-" + eid + "-" + selectedYear + "-" + m;
	}

	public void generatePreview()
	{
		if (currentEnrollment == null || startMonth == null)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Selecione mês inicial"));
			return;
		}
		String sc = currentEnrollment.getStudent() != null ? currentEnrollment.getStudent().getCode() : "SEM-COD";
		String eid = currentEnrollment.getId() != null ? currentEnrollment.getId().toString() : "0";
		int year = 2026;
		try
		{
			year = Integer.parseInt(selectedYear);
		} catch (Exception e)
		{
		}
		previewFees = new ArrayList<>();
		String[] pt =
		{ "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" };
		for (int m = startMonth; m <= 12; m++)
		{
			String ms = String.format("%02d", m);
			String ref = sc + "-" + eid + "-" + year + "-" + ms;
			int day = Math.min(defaultDueDay, LocalDate.of(year, m, 1).lengthOfMonth());
			LocalDate due = LocalDate.of(year, m, day);
			Date dueDate = Date.from(due.atStartOfDay(ZoneId.systemDefault()).toInstant());
			MonthlyFeeDTO dto = new MonthlyFeeDTO();
			dto.setMonth(m);
			dto.setMonthLabel(pt[m - 1]);
			dto.setYear(year);
			dto.setReference(ref);
			dto.setAmount(defaultAmount);
			dto.setDueDate(dueDate);
			previewFees.add(dto);
		}
	}

	@Transactional
	public void saveMonthlyReferences()
	{
		if (currentEnrollment == null || startMonth == null)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Selecione matrícula e mês"));
			return;
		}
		try
		{
			Long userId = 1L;
			try
			{
				if (loginBean != null && loginBean.getLoggedUser() != null)
					userId = loginBean.getLoggedUser().getId();
			} catch (Exception e)
			{
			}
			int year = 2026;
			try
			{
				year = Integer.parseInt(selectedYear);
			} catch (Exception e)
			{
			}
			List<MonthlyFeeEntity> saved = monthlyFeeService.generateFromMonth(currentEnrollment.getId(), startMonth, year, defaultAmount, defaultDueDay,
					userId);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso",
					saved.size() + " referências gravadas com formato COD_ESTUDANTE-COD_MATRICULA-ANO-MES"));
			previewFees.clear();
			loadByEnrollment();
		} catch (Exception e)
		{
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public void viewEnrollment(EnrollmentEntity e)
	{
		setCurrentEnrollment(e);
		dashboardBean.navigate("enrollment/enrollment-view", "mensalidades");
	}

	public double getTotalAmount()
	{
		return monthlyFees != null ? monthlyFees.stream().mapToDouble(f -> f.getAmount() != null ? f.getAmount().doubleValue() : 0).sum() : 0;
	}

	// Getters Setters
	public List<MonthlyFeeEntity> getMonthlyFees()
	{
		return monthlyFees;
	}

	public EnrollmentEntity getCurrentEnrollment()
	{
		return currentEnrollment;
	}

	public void setCurrentEnrollmentEntity(EnrollmentEntity e)
	{
		currentEnrollment = e;
	}

	public Integer getStartMonth()
	{
		return startMonth;
	}

	public void setStartMonth(Integer m)
	{
		startMonth = m;
	}

	public List<MonthOption> getMonths()
	{
		return months;
	}

	public String getSelectedYear()
	{
		return selectedYear;
	}

	public void setSelectedYear(String y)
	{
		selectedYear = y;
	}

	public BigDecimal getDefaultAmount()
	{
		return defaultAmount;
	}

	public void setDefaultAmount(BigDecimal a)
	{
		defaultAmount = a;
	}

	public Integer getDefaultDueDay()
	{
		return defaultDueDay;
	}

	public void setDefaultDueDay(Integer d)
	{
		defaultDueDay = d;
	}

	public List<MonthlyFeeDTO> getPreviewFees()
	{
		return previewFees;
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
		private BigDecimal amount;
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

		public BigDecimal getAmount()
		{
			return amount;
		}

		public void setAmount(BigDecimal a)
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
