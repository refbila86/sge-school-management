package mz.co.sge.controller;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.service.IAcademicYearService;

@Component("academicYearBean")
@Scope("view")
public class AcademicYearBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Autowired
	private transient IAcademicYearService academicYearService;

	private List<AcademicYearEntity> academicYears;

	private AcademicYearEntity academicYear = new AcademicYearEntity();

	private boolean editing = false;

	private String filterYear;

	@PostConstruct
	public void init()
	{
		loadAcademicYears();
	}

	public void loadAcademicYears()
	{
		if (filterYear != null && !filterYear.isEmpty())
		{
			academicYearService.getAcademicYearByYear(filterYear).ifPresentOrElse(ay -> academicYears = List.of(ay),
					() -> academicYears = academicYearService.getAllAcademicYears());
		} else
		{
			academicYears = academicYearService.getAllAcademicYears();
		}
	}

	public AcademicYearBean(IAcademicYearService academicYearService)
	{
		this.academicYearService = academicYearService;
	}

	public void filter()
	{
		if (filterYear == null || filterYear.isBlank())
		{
			loadAcademicYears();
			return;
		}
		try
		{
			String term = filterYear.trim();
			// carrega tudo uma vez e filtra - não depende de método no service
			//List<AcademicYearEntity> all = academicYearService.loadAcademicYears();
			academicYears = academicYearService.getAllAcademicYears();

			// tenta filtrar por número exato ou contém
			this.academicYears = academicYears.stream().filter(a -> String.valueOf(a.getYear()).contains(term)).collect(Collectors.toList());

		} catch (Exception e)
		{
			loadAcademicYears();
		}
	}

	public void prepareNew()
	{
		this.academicYear = new AcademicYearEntity();
		this.academicYear.setActive(true);
		this.editing = false;
	}

	public void prepareEdit(AcademicYearEntity a)
	{
		this.academicYear = a;
		this.editing = true;
	}

	public String saves()
	{
		try
		{
			if (editing)
			{
				academicYearService.updateAcademicYear(academicYear);
				addMessage("Atualizado com sucesso", "Ano " + academicYear.getYear() + " atualizado");
			} else
			{
				academicYearService.createAcademicYear(academicYear);
				addMessage("Criado com sucesso", "Ano " + academicYear.getYear() + " cadastrado");
			}
			loadAcademicYears();
			return "/pages/academic-year/academic-year-list?faces-redirect=true";
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
			return null;
		}
	}

	public void save()
	{
		try
		{
			if (editing)
				academicYearService.updateAcademicYear(academicYear);
			else
				academicYearService.createAcademicYear(academicYear);
			loadAcademicYears();
			this.academicYear = new AcademicYearEntity();
			this.editing = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Ano gravado"));
		} catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public void delete(AcademicYearEntity entity)
	{
		try
		{
			academicYearService.deleteAcademicYear(entity.getId());
			loadAcademicYears();
			addMessage("Removido", "Ano removido com sucesso");
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
		}
	}

	public void activate(AcademicYearEntity entity)
	{
		try
		{
			academicYearService.activateAcademicYear(entity.getId());
			loadAcademicYears();
			addMessage("Ativado", "Ano " + entity.getYear() + " agora é o ativo");
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
		}
	}

	public void deactivate(AcademicYearEntity entity)
	{
		try
		{
			entity.setActive(false);
			academicYearService.updateAcademicYear(entity);
			loadAcademicYears();
			addMessage("Inativado", "Ano " + entity.getYear() + " foi inativado");
		} catch (Exception e)
		{
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
		}
	}

	private void addMessage(String summary, String detail)
	{
		addMessage(FacesMessage.SEVERITY_INFO, summary, detail);
	}

	private void addMessage(FacesMessage.Severity severity, String summary, String detail)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
	}

	// Getters Setters
	public List<AcademicYearEntity> getAcademicYears()
	{
		return academicYears;
	}

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity academicYear)
	{
		this.academicYear = academicYear;
	}

	public boolean isEditing()
	{
		return editing;
	}

	public void setEditing(boolean editing)
	{
		this.editing = editing;
	}

	public String getFilterYear()
	{
		return filterYear;
	}

	public void setFilterYear(String filterYear)
	{
		this.filterYear = filterYear;
	}
}