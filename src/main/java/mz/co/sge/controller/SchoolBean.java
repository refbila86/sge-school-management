package mz.co.sge.controller;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import mz.co.sge.entity.LicenseEntity;
import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.service.ILicenseService;
import mz.co.sge.service.ISchoolService;

@Component("schoolBean")
@Scope("view")
public class SchoolBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final ISchoolService schoolService;
	private final ILicenseService licenseService;
	private final DashboardBean dashboardBean;

	private List<LicenseEntity> availableLicenses = new ArrayList<>();
	private LicenseEntity selectedLicense;
	private Map<Long, LicenseEntity> licenseCache = new HashMap<>();

	private SchoolEntity school;
	private SchoolEntity selectedSchool;
	private List<SchoolEntity> schools;
	private List<SchoolEntity> filteredSchools;
	private String searchTerm;
	private boolean editing;

	public SchoolBean(ISchoolService schoolService, ILicenseService licenseService, DashboardBean dashboardBean)
	{
		this.schoolService = schoolService;
		this.licenseService = licenseService;
		this.dashboardBean = dashboardBean;
	}

	@PostConstruct
	public void init()
	{
		cleanForm();
		loadSchools();
		try
		{
			this.availableLicenses = licenseService.getAll();
		} catch (Exception e)
		{
			this.availableLicenses = new ArrayList<>();
		}
	}

	public void loadSchools()
	{
		this.schools = schoolService.findAll();
		// preenche cache
		licenseCache.clear();
		if (schools != null)
		{
			for (SchoolEntity s : schools)
			{
				if (s.getLicenseId() != null)
				{
					licenseService.getById(s.getLicenseId()).ifPresent(lic -> licenseCache.put(s.getLicenseId(), lic));
				}
			}
		}
	}

	// ESTE MÉTODO QUE FALTAVA - O ERRO ESTAVA AQUI
	public LicenseEntity getLicenseForSchool(Long licenseId)
	{
		if (licenseId == null)
			return null;
		if (licenseCache.containsKey(licenseId))
			return licenseCache.get(licenseId);
		Optional<LicenseEntity> opt = licenseService.getById(licenseId);
		if (opt.isPresent())
		{
			licenseCache.put(licenseId, opt.get());
			return opt.get();
		}
		return null;
	}

	public void onLicenseChange()
	{
		if (school != null && school.getLicenseId() != null)
		{
			selectedLicense = getLicenseForSchool(school.getLicenseId());
		} else
		{
			selectedLicense = null;
		}
	}

	public void search()
	{
		this.schools = schoolService.search(searchTerm);
	}

	public void cleanForm()
	{
		this.school = new SchoolEntity();
		this.school.setActive(true);
		this.editing = false;
		this.selectedLicense = null;
	}

	public void prepareView(SchoolEntity schoolToView)
	{
		this.selectedSchool = schoolToView;
	}

	public void prepareDelete(SchoolEntity schoolToDelete)
	{
		this.selectedSchool = schoolToDelete;
	}

	public void prepareEdit(SchoolEntity schoolToEdit)
	{
		this.school = schoolToEdit;
		this.editing = true;
		onLicenseChange();
	}

	public void save()
	{
		try
		{
			if (!editing && schoolService.codeExists(school.getCode()))
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Código já existe: " + school.getCode());
				return;
			}
			schoolService.save(school);
			addMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Escola salva com sucesso.");
			cleanForm();
			loadSchools();
			dashboardBean.navigate("schools/school-list", "escolas");
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", e.getMessage());
		}
	}

	public void delete()
	{
		try
		{
			if (selectedSchool != null && selectedSchool.getId() != null)
			{
				schoolService.delete(selectedSchool.getId());
				addMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Escola removida.");
				loadSchools();
				selectedSchool = null;
			}
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", e.getMessage());
		}
	}

	public void toggleActive(SchoolEntity s)
	{
		try
		{
			schoolService.toggleActive(s.getId());
			loadSchools();
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", e.getMessage());
		}
	}

	private void addMessage(FacesMessage.Severity severity, String summary, String detail)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
	}

	// GETTERS E SETTERS
	public SchoolEntity getSchool()
	{
		return school;
	}

	public void setSchool(SchoolEntity school)
	{
		this.school = school;
	}

	public SchoolEntity getSelectedSchool()
	{
		return selectedSchool;
	}

	public void setSelectedSchool(SchoolEntity selectedSchool)
	{
		this.selectedSchool = selectedSchool;
	}

	public List<SchoolEntity> getSchools()
	{
		return schools;
	}

	public void setSchools(List<SchoolEntity> schools)
	{
		this.schools = schools;
	}

	public List<SchoolEntity> getFilteredSchools()
	{
		return filteredSchools;
	}

	public void setFilteredSchools(List<SchoolEntity> filteredSchools)
	{
		this.filteredSchools = filteredSchools;
	}

	public String getSearchTerm()
	{
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm)
	{
		this.searchTerm = searchTerm;
	}

	public boolean isEditing()
	{
		return editing;
	}

	public void setEditing(boolean editing)
	{
		this.editing = editing;
	}

	public List<LicenseEntity> getAvailableLicenses()
	{
		return availableLicenses;
	}

	public LicenseEntity getSelectedLicense()
	{
		return selectedLicense;
	}

	public void setSelectedLicense(LicenseEntity selectedLicense)
	{
		this.selectedLicense = selectedLicense;
	}
}