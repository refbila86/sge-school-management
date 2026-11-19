package mz.co.sge.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import mz.co.sge.entity.LicenseEntity;
import mz.co.sge.enumes.LicenseEnum.LicenseStatus;
import mz.co.sge.enumes.LicenseEnum.LicenseType;
import mz.co.sge.service.ILicenseService;

@Component("licenseBean")
@Scope("view")
public class LicenseBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final int EXPIRING_DAYS = 30;

	private final ILicenseService licenseService;
	private final DashboardBean dashboardBean;

	private LicenseEntity license;
	private LicenseEntity selectedLicense;
	private List<LicenseEntity> licenses = new ArrayList<>();
	private String searchTerm;
	private boolean editing;

	// ---- filtros (só um activo de cada vez) ----
	private LicenseType typeFilter;
	private LicenseStatus statusFilter;
	private String dateFilter; // "EXPIRING" | "EXPIRED" | null
	private String viewMode = "CARDS"; // "CARDS" ou "TABELA"

	public LicenseBean(ILicenseService licenseService, DashboardBean dashboardBean)
	{
		this.licenseService = licenseService;
		this.dashboardBean = dashboardBean;
	}

	@PostConstruct
	public void init()
	{
		cleanForm();
		// NÃO carrega lista no PostConstruct, deixa lazy
	}

	public void loadLicenses()
	{
		try
		{
			this.licenses = licenseService.getAll();
		} catch (Exception e)
		{
			// Não derruba o dashboard
			e.printStackTrace();
			this.licenses = new ArrayList<>();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Não foi possível carregar licenças: " + e.getMessage()));
		}
	}

	public void cleanForm()
	{
		this.license = new LicenseEntity();
		this.license.setType(LicenseType.ANNUAL);
		this.license.setStatus(LicenseStatus.AVAILABLE);
		this.license.setDurationDays(365);
		this.license.setActive(true);
		this.editing = false;
	}

	public String formatDate(java.time.LocalDate date)
	{
		if (date == null)
			return "-";
		return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}

	public void prepareView(LicenseEntity lic)
	{
		this.selectedLicense = lic;
	}

	public void prepareDelete(LicenseEntity lic)
	{
		this.selectedLicense = lic;
	}

	public void prepareNew()
	{
		cleanForm();
		loadLicenses();
	}

	public void prepareEdit(LicenseEntity lic)
	{
		this.license = lic;
		this.editing = true;
		calculateExpiration();
	}

	public void generateKey()
	{
		String key = licenseService.generateLicenseKey();
		license.setLicenseKey(key);
		license.setCode(key);
	}

	public void onTypeChange()
	{
		if (license.getType() == null)
			return;
		switch (license.getType())
		{
		case MONTHLY -> license.setDurationDays(30);
		case ANNUAL -> license.setDurationDays(365);
		case TRIENNIAL -> license.setDurationDays(1095);
		case LIFETIME -> license.setDurationDays(36500);
		case TRIAL -> license.setDurationDays(15);
		}
		calculateExpiration();
	}

	public void calculateExpiration()
	{
		if (license.getActivationDate() != null && license.getDurationDays() != null)
		{
			LocalDate exp = license.getActivationDate().plusDays(license.getDurationDays());
			license.setExpirationDate(exp);
			license.setEndDate(exp);
			if (license.getStartDate() == null)
				license.setStartDate(license.getActivationDate());
		}
	}

	public void save()
	{
		try
		{
			if (license.getLicenseKey() == null || license.getLicenseKey().isBlank())
			{
				license.setLicenseKey(licenseService.generateLicenseKey());
				license.setCode(license.getLicenseKey());
			}
			if (license.getActivationDate() == null)
				license.setActivationDate(LocalDate.now());
			calculateExpiration();
			licenseService.save(license);
			loadLicenses();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Licença salva: " + license.getLicenseKey()));
			cleanForm();
			dashboardBean.navigate("licenses/license-list", "licencas");
		} catch (Exception e)
		{
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar", e.getMessage()));
		}
	}

	public void deleteLicense()
	{
		if (selectedLicense != null)
		{
			try
			{
				licenseService.delete(selectedLicense.getId());
				loadLicenses();
				selectedLicense = null;
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Licença removida"));
			} catch (Exception e)
			{
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
			}
		}
	}

	// ====================== FILTROS ======================

	public List<LicenseEntity> getFilteredLicenses()
	{
		return getLicenses().stream().filter(l -> typeFilter == null || l.getType() == typeFilter)
				.filter(l -> statusFilter == null || l.getStatus() == statusFilter)
				.filter(l -> dateFilter == null || ("EXPIRED".equals(dateFilter) && isExpired(l)) || ("EXPIRING".equals(dateFilter) && isExpiring(l)))
				.collect(Collectors.toList());
	}

	public void filterByType(LicenseType t)
	{
		LicenseType novo = (typeFilter == t) ? null : t;
		clearFilter();
		typeFilter = novo;
	}

	public void filterByStatus(LicenseStatus s)
	{
		LicenseStatus novo = (statusFilter == s) ? null : s;
		clearFilter();
		statusFilter = novo;
	}

	public void filterByDate(String kind)
	{
		String novo = kind.equals(dateFilter) ? null : kind;
		clearFilter();
		dateFilter = novo;
	}

	public void clearFilter()
	{
		typeFilter = null;
		statusFilter = null;
		dateFilter = null;
	}

	public boolean isNoFilter()
	{
		return typeFilter == null && statusFilter == null && dateFilter == null;
	}

	// ====================== CONTAGENS ======================

	public int getTotalLicenses()
	{
		return getLicenses().size();
	}

	public long countByType(LicenseType t)
	{
		return getLicenses().stream().filter(l -> l.getType() == t).count();
	}

	public long countByStatus(LicenseStatus s)
	{
		return getLicenses().stream().filter(l -> l.getStatus() == s).count();
	}

	public long getCountExpired()
	{
		return getLicenses().stream().filter(this::isExpired).count();
	}

	public long getCountExpiring()
	{
		return getLicenses().stream().filter(this::isExpiring).count();
	}

	private boolean isExpired(LicenseEntity l)
	{
		return l.getExpirationDate() != null && l.getExpirationDate().isBefore(LocalDate.now());
	}

	private boolean isExpiring(LicenseEntity l)
	{
		LocalDate exp = l.getExpirationDate();
		if (exp == null)
			return false;
		LocalDate hoje = LocalDate.now();
		return !exp.isBefore(hoje) && !exp.isAfter(hoje.plusDays(EXPIRING_DAYS));
	}

	// % do tempo da licença já consumido (0-100)
	public int progress(LicenseEntity l)
	{
		LocalDate a = l.getActivationDate();
		LocalDate e = l.getExpirationDate();
		if (a == null || e == null)
			return 0;
		long total = ChronoUnit.DAYS.between(a, e);
		if (total <= 0)
			return 100;
		long gasto = ChronoUnit.DAYS.between(a, LocalDate.now());
		return (int) Math.max(0, Math.min(100, gasto * 100 / total));
	}

	// ====================== APARÊNCIA ======================

	public String typeLabel(LicenseType t)
	{
		if (t == null)
			return "-";
		return switch (t.name())
		{
		case "MONTHLY" -> "Mensal";
		case "ANNUAL" -> "Anual";
		case "TRIENNIAL" -> "Trienal";
		case "LIFETIME" -> "Vitalícia";
		case "TRIAL" -> "Teste";
		default -> capitalize(t.name());
		};
	}

	public String typeIcon(LicenseType t)
	{
		if (t == null)
			return "pi-key";
		return switch (t.name())
		{
		case "MONTHLY" -> "pi-calendar";
		case "ANNUAL" -> "pi-calendar-plus";
		case "TRIENNIAL" -> "pi-history";
		case "LIFETIME" -> "pi-star";
		case "TRIAL" -> "pi-clock";
		default -> "pi-key";
		};
	}

	public String typeColor(LicenseType t)
	{
		if (t == null)
			return "#607d8b";
		return switch (t.name())
		{
		case "MONTHLY" -> "#0891b2";
		case "ANNUAL" -> "#1976d2";
		case "TRIENNIAL" -> "#7b1fa2";
		case "LIFETIME" -> "#ad1457";
		case "TRIAL" -> "#e65100";
		default -> "#607d8b";
		};
	}

	public String statusLabel(LicenseStatus s)
	{
		if (s == null)
			return "-";
		String n = s.name().toUpperCase();
		if (n.contains("INACTIVE"))
			return "Inactiva";
		if (n.contains("ACTIVE"))
			return "Activa";
		if (n.contains("AVAILABLE"))
			return "Disponível";
		if (n.contains("EXPIRED"))
			return "Expirada";
		if (n.contains("SUSPEND"))
			return "Suspensa";
		if (n.contains("BLOCK"))
			return "Bloqueada";
		if (n.contains("REVOK"))
			return "Revogada";
		if (n.contains("CANCEL"))
			return "Cancelada";
		if (n.contains("USED"))
			return "Utilizada";
		if (n.contains("PENDING"))
			return "Pendente";
		return capitalize(n);
	}

	public String statusIcon(LicenseStatus s)
	{
		if (s == null)
			return "pi-key";
		String n = s.name().toUpperCase();
		if (n.contains("INACTIVE"))
			return "pi-ban";
		if (n.contains("ACTIVE"))
			return "pi-check-circle";
		if (n.contains("AVAILABLE"))
			return "pi-inbox";
		if (n.contains("EXPIRED"))
			return "pi-times-circle";
		if (n.contains("SUSPEND"))
			return "pi-pause";
		if (n.contains("BLOCK") || n.contains("REVOK") || n.contains("CANCEL"))
			return "pi-lock";
		if (n.contains("PENDING"))
			return "pi-hourglass";
		return "pi-key";
	}

	public String statusColor(LicenseStatus s)
	{
		if (s == null)
			return "#607d8b";
		String n = s.name().toUpperCase();
		if (n.contains("INACTIVE"))
			return "#64748b";
		if (n.contains("ACTIVE"))
			return "#16a34a";
		if (n.contains("AVAILABLE"))
			return "#0891b2";
		if (n.contains("EXPIRED"))
			return "#dc2626";
		if (n.contains("SUSPEND"))
			return "#f59e0b";
		if (n.contains("BLOCK") || n.contains("REVOK") || n.contains("CANCEL"))
			return "#b91c1c";
		if (n.contains("PENDING"))
			return "#f59e0b";
		return "#607d8b";
	}

	private String capitalize(String name)
	{
		String s = name.replace('_', ' ').toLowerCase();
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	// ====================== GETTERS / SETTERS ======================

	public LicenseType[] getLicenseTypes()
	{
		return LicenseType.values();
	}

	public LicenseStatus[] getLicenseStatuses()
	{
		return LicenseStatus.values();
	}

	public LicenseEntity getLicense()
	{
		return license;
	}

	public void setLicense(LicenseEntity license)
	{
		this.license = license;
	}

	public LicenseEntity getSelectedLicense()
	{
		return selectedLicense;
	}

	public void setSelectedLicense(LicenseEntity s)
	{
		this.selectedLicense = s;
	}

	public List<LicenseEntity> getLicenses()
	{
		if (licenses == null || licenses.isEmpty())
		{
			loadLicenses();
		}
		return licenses;
	}

	public void setLicenses(List<LicenseEntity> l)
	{
		this.licenses = l;
	}

	public String getSearchTerm()
	{
		return searchTerm;
	}

	public void setSearchTerm(String s)
	{
		this.searchTerm = s;
	}

	public boolean isEditing()
	{
		return editing;
	}

	public void setEditing(boolean e)
	{
		this.editing = e;
	}

	public LicenseType getTypeFilter()
	{
		return typeFilter;
	}

	public LicenseStatus getStatusFilter()
	{
		return statusFilter;
	}

	public String getDateFilter()
	{
		return dateFilter;
	}

	public String getViewMode()
	{
		return viewMode;
	}

	public void setViewMode(String viewMode)
	{
		this.viewMode = viewMode;
	}
}