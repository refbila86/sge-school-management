package mz.co.sge.controller;

import java.io.Serializable;
import java.util.ArrayList;
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
	private IClassLevelService classLevelService;
	@Autowired
	private IAcademicYearService academicYearService;

	@Inject
	private LoginBean loginBean;
	@Inject
	private DashboardBean dashboardBean;

	private List<ClassLevelEntity> levels = new ArrayList<>();
	private ClassLevelEntity level = new ClassLevelEntity();
	private boolean editing = false;
	private String searchTerm;
	private Long selectedAcademicYearId;
	private List<AcademicYearEntity> academicYears = new ArrayList<>();

	@PostConstruct
	public void init()
	{
		try
		{
			if (loginBean != null)
			{
				selectedAcademicYearId = loginBean.getAcademicYearId();
			}
		} catch (Exception e)
		{
			selectedAcademicYearId = null;
		}
		load();
		loadAcademicYears();
	}

	public void load()
	{
		try
		{
			Long yearId = selectedAcademicYearId;
			if (yearId == null)
			{
				try
				{
					if (loginBean != null)
						yearId = loginBean.getAcademicYearId();
				} catch (Exception e)
				{
				}
			}
			if (yearId != null)
			{
				levels = classLevelService.findByAcademicYear(yearId);
			} else
			{
				levels = classLevelService.findAll();
			}
			if (levels == null)
				levels = new ArrayList<>();
		} catch (Exception e)
		{
			e.printStackTrace();
			levels = new ArrayList<>();
		}
	}

	public void loadAcademicYears()
	{
		try
		{
			academicYears = academicYearService.getAllAcademicYears();
			if (academicYears == null)
				academicYears = new ArrayList<>();
		} catch (Exception e)
		{
			academicYears = new ArrayList<>();
		}
	}

	public void cleanForm()
	{
		level = new ClassLevelEntity();
		level.setActive(true);
		level.setHasExam(false); // GARANTE EXAME DESLIGADO POR PADRÃO
		editing = false;
		try
		{
			if (loginBean != null && loginBean.getAcademicYearId() != null)
			{
				selectedAcademicYearId = loginBean.getAcademicYearId();
			}
		} catch (Exception e)
		{
			selectedAcademicYearId = null;
		}
	}

	public void search()
	{
		try
		{
			Long yearId = selectedAcademicYearId;
			if (yearId == null)
			{
				try
				{
					if (loginBean != null)
						yearId = loginBean.getAcademicYearId();
				} catch (Exception ex)
				{
				}
			}
			if (searchTerm == null || searchTerm.isBlank())
			{
				load();
			} else
			{
				levels = classLevelService.search(searchTerm, yearId);
				if (levels == null)
					levels = new ArrayList<>();
			}
		} catch (Exception e)
		{
			levels = new ArrayList<>();
		}
	}

	public void save()
	{
		try
		{
			Long yearIdToUse = selectedAcademicYearId;
			if (yearIdToUse == null)
			{
				try
				{
					if (loginBean != null)
						yearIdToUse = loginBean.getAcademicYearId();
				} catch (Exception ex)
				{
				}
			}
			if (yearIdToUse != null)
			{
				try
				{
					AcademicYearEntity year = academicYearService.findByYearId(yearIdToUse);
					level.setAcademicYear(year);
				} catch (Exception ex)
				{
					academicYearService.getAcademicYearById(yearIdToUse).ifPresent(level::setAcademicYear);
				}
			}

			if (level.getId() == null && classLevelService.existsByNameAndYear(level.getName(), yearIdToUse))
			{
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Já existe esta classe neste ano lectivo"));
				return;
			}

			classLevelService.save(level);
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