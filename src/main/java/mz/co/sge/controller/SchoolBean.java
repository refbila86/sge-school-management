package mz.co.sge.controller;

import java.io.Serializable;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.service.SchoolsService;

@Component("schoolBean")
@Scope("view")
public class SchoolBean implements Serializable
{

	private static final long serialVersionUID = 1L;

	private final SchoolsService schoolService;

	private SchoolEntity school;
	private List<SchoolEntity> schools;
	private boolean editing;

	public SchoolBean(SchoolsService schoolService)
	{
		this.schoolService = schoolService;
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

	public void cleanForm()
	{
		this.school = new SchoolEntity();
		this.editing = false;
	}

	public void prepareEdit(SchoolEntity schoolToEdit)
	{
		this.school = schoolToEdit;
		this.editing = true;
	}

	public void save()
	{
		try
		{
			// Valida código duplicado ao criar nova escola
			if (!editing && schoolService.codeExists(school.getCode()))
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Já existe uma escola com este código.");
				return;
			}

			schoolService.save(school);
			addMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Escola salva com sucesso.");
			cleanForm();
			loadSchools();

		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Erro ao salvar escola: " + e.getMessage());
		}
	}

	public void delete(SchoolEntity schoolToDelete)
	{
		try
		{
			schoolService.delete(schoolToDelete.getId());
			addMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Escola removida com sucesso.");
			loadSchools();

			if (this.school.getId() != null && this.school.getId().equals(schoolToDelete.getId()))
			{
				cleanForm();
			}
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Erro ao remover escola: " + e.getMessage());
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

	public List<SchoolEntity> getSchools()
	{
		return schools;
	}

	public boolean isEditing()
	{
		return editing;
	}
}