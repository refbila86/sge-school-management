package mz.co.sge.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

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
	
	private final ILicenseService licenseService;
	
	private final DashboardBean dashboardBean;
	
	private LicenseEntity license;
	
	private LicenseEntity selectedLicense;
	private List<LicenseEntity> licenses;
	private String searchTerm;
	private boolean editing;

	public LicenseBean(ILicenseService licenseService, DashboardBean dashboardBean)
	{
		this.licenseService = licenseService;
		this.dashboardBean = dashboardBean;
	}

	@PostConstruct
	public void init()
	{
		cleanForm();
		loadLicenses();
	}

	public void loadLicenses()
	{
		this.licenses = licenseService.getAll();
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
	}

	public void deleteLicense()
	{
		if (selectedLicense != null)
		{
			licenseService.delete(selectedLicense.getId());
			loadLicenses();
			selectedLicense = null;
		}
	}

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
}