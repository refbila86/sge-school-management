package mz.co.sge.controller;

import java.io.Serializable;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.service.ISchoolService;

@Component("schoolBean")
@Scope("view")
public class SchoolBean implements Serializable
{

	private static final long serialVersionUID = 1L;

	private final ISchoolService schoolService;
	private final DashboardBean dashboardBean;

	private SchoolEntity school;
	private SchoolEntity selectedSchool;
	private List<SchoolEntity> schools;
	private List<SchoolEntity> filteredSchools;

	private String searchTerm;
	private boolean editing;

	public SchoolBean(ISchoolService schoolService, DashboardBean dashboardBean)
	{
		this.schoolService = schoolService;
		this.dashboardBean = dashboardBean;
	}

	@PostConstruct
	public void init()
	{
		cleanForm();
		loadSchools();
	}

	public void loadSchools()
	{
		this.schools = schoolService.findAll();
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
	}

	public void prepareNew()
	{
		cleanForm();
		// Redireciona para include dentro do dashboard
		// O dashboardBean vai carregar school/school-include
	}

	public void prepareEdit(SchoolEntity schoolToEdit)
	{
		this.school = schoolToEdit;
		this.editing = true;
	}

	public void prepareView(SchoolEntity schoolToView)
	{
		this.selectedSchool = schoolToView;
	}

	public void prepareDelete(SchoolEntity schoolToDelete)
	{
		this.selectedSchool = schoolToDelete;
	}

	public void save()
	{
		try
		{
			// Valida código duplicado ao criar
			if (!editing && schoolService.codeExists(school.getCode()))
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Já existe uma escola com o código: " + school.getCode());
				return;
			}

			// Valida licenseId duplicado
			if (school.getLicenseId() != null && !editing && schoolService.licenseExists(school.getLicenseId()))
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "License ID já está em uso.");
				return;
			}

			boolean wasEditing = editing;
			schoolService.save(school);
			addMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", (wasEditing ? "Escola atualizada" : "Escola cadastrada") + " com sucesso.");
			cleanForm();
			loadSchools();

			// Só navega de volta para a lista quando o save tiver sucesso
			dashboardBean.navigate("schools/school-list", "escolas");

		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Erro ao salvar: " + e.getMessage());
		}
	}

	public void delete()
	{
		try
		{
			if (selectedSchool != null && selectedSchool.getId() != null)
			{
				schoolService.delete(selectedSchool.getId());
				addMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Escola removida com sucesso.");
				loadSchools();
				selectedSchool = null;
			}
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Erro ao remover: " + e.getMessage());
		}
	}

	public void toggleActive(SchoolEntity s)
	{
		try
		{
			schoolService.toggleActive(s.getId());
			addMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Estado alterado para " + (!s.getActive() ? "Ativa" : "Inativa"));
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
}