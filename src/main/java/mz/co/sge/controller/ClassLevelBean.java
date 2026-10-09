package mz.co.sge.controller;

import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.ClassLevelEntity;
import mz.co.sge.service.IAcademicYearService;
import mz.co.sge.service.IClassLevelService;

@Named
@ViewScoped
public class ClassLevelBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Autowired
	private IClassLevelService service;
	@Autowired
	private IAcademicYearService academicYearService;

	@Inject
	private LoginBean loginBean;
	@Inject
	private DashboardBean dashboardBean;

	private List<ClassLevelEntity> levels;
	private ClassLevelEntity level = new ClassLevelEntity();
	private boolean editing = false;
	private String searchTerm;
	private Long selectedAcademicYearId;
	private List<AcademicYearEntity> academicYears;

	@PostConstruct
	public void init()
	{
		selectedAcademicYearId = loginBean.getAcademicYearId();
		load();
		loadAcademicYears();
	}

	public void load()
	{
		Long yearId = selectedAcademicYearId != null ? selectedAcademicYearId : loginBean.getAcademicYearId();
		if (yearId != null)
		{
			levels = service.findByAcademicYear(yearId);
		} else
		{
			levels = service.findAll();
		}
	}

	public void loadAcademicYears()
	{
		try
		{
			academicYears = academicYearService.getAllAcademicYears();
		} catch (Exception e)
		{
			academicYears = List.of();
		}
	}

	public void cleanForm()
	{
		level = new ClassLevelEntity();
		level.setActive(true);
		editing = false;
		if (loginBean.getAcademicYearId() != null)
		{
			selectedAcademicYearId = loginBean.getAcademicYearId();
		}
	}

	public void search()
	{
		levels = service.search(searchTerm, loginBean.getAcademicYearId());
	}

	public void save()
	{
		try
		{
			Long yearIdToUse = selectedAcademicYearId != null ? selectedAcademicYearId : loginBean.getAcademicYearId();
			if (yearIdToUse != null)
			{
				// teu service retorna Optional ou entity direta, trata os dois
				try
				{
					AcademicYearEntity year = academicYearService.findByYearId(yearIdToUse);
					level.setAcademicYear(year);
				} catch (Exception ex)
				{
					// fallback se findByYearId falhar, tenta Optional
					academicYearService.getAcademicYearById(yearIdToUse).ifPresent(level::setAcademicYear);
				}
			}

			service.save(level);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Classe salva!"));
			load();
			dashboardBean.navigate("classes/class-list", "classes");
		} catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
			e.printStackTrace();
		}
	}

	public List<ClassLevelEntity> getLevels()
	{
		return levels;
	}

	public void setLevels(List<ClassLevelEntity> levels)
	{
		this.levels = levels;
	}

	public ClassLevelEntity getLevel()
	{
		return level;
	}

	public void setLevel(ClassLevelEntity level)
	{
		this.level = level;
	}

	public boolean isEditing()
	{
		return editing;
	}

	public void setEditing(boolean editing)
	{
		this.editing = editing;
	}

	public String getSearchTerm()
	{
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm)
	{
		this.searchTerm = searchTerm;
	}

	public Long getSelectedAcademicYearId()
	{
		return selectedAcademicYearId;
	}

	public void setSelectedAcademicYearId(Long selectedAcademicYearId)
	{
		this.selectedAcademicYearId = selectedAcademicYearId;
	}

	public List<AcademicYearEntity> getAcademicYears()
	{
		return academicYears;
	}

	public void setAcademicYears(List<AcademicYearEntity> academicYears)
	{
		this.academicYears = academicYears;
	}
}