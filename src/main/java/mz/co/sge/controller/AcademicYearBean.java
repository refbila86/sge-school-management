package mz.co.sge.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

	// ---- filtro dos cards: ACTIVE | INACTIVE | ONGOING | UPCOMING | FINISHED |
	// null ----
	private String cardFilter;
	private String viewMode = "CARDS"; // "CARDS" ou "TABELA"

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

	public String getYearLabel(Long id)
	{
		if (id == null)
			return "Geral";
		if (academicYears == null)
			return id.toString();
		return academicYears.stream().filter(ay -> ay.getId().equals(id)).map(ay -> String.valueOf(ay.getYear())).findFirst().orElse(id.toString());
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
			// List<AcademicYearEntity> all = academicYearService.loadAcademicYears();
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

	// ====================== HELPERS (independentes de isActive/getActive)
	// ======================

	private Object prop(AcademicYearEntity a, String name)
	{
		try
		{
			return new BeanWrapperImpl(a).getPropertyValue(name);
		} catch (Exception e)
		{
			return null;
		}
	}

	private boolean isActiveYear(AcademicYearEntity a)
	{
		return Boolean.TRUE.equals(prop(a, "active"));
	}

	private LocalDate toDate(Object o)
	{
		if (o instanceof LocalDateTime ldt)
			return ldt.toLocalDate();
		if (o instanceof LocalDate ld)
			return ld;
		return null;
	}

	private LocalDate start(AcademicYearEntity a)
	{
		return toDate(prop(a, "startDate"));
	}

	private LocalDate end(AcademicYearEntity a)
	{
		return toDate(prop(a, "endDate"));
	}

	// ONGOING | UPCOMING | FINISHED | null (sem datas)
	public String periodOf(AcademicYearEntity a)
	{
		LocalDate s = start(a);
		LocalDate e = end(a);
		if (s == null || e == null)
			return null;
		LocalDate hoje = LocalDate.now();
		if (hoje.isBefore(s))
			return "UPCOMING";
		if (hoje.isAfter(e))
			return "FINISHED";
		return "ONGOING";
	}

	public String periodLabel(String p)
	{
		if (p == null)
			return "Sem datas";
		return switch (p)
		{
		case "ONGOING" -> "Em curso";
		case "UPCOMING" -> "Futuro";
		case "FINISHED" -> "Concluído";
		default -> p;
		};
	}

	public String periodColor(String p)
	{
		if (p == null)
			return "#64748b";
		return switch (p)
		{
		case "ONGOING" -> "#15558a";
		case "UPCOMING" -> "#f59e0b";
		case "FINISHED" -> "#64748b";
		default -> "#64748b";
		};
	}

	// % do ano lectivo já decorrido (0-100)
	public int progress(AcademicYearEntity a)
	{
		LocalDate s = start(a);
		LocalDate e = end(a);
		if (s == null || e == null)
			return 0;
		long total = ChronoUnit.DAYS.between(s, e);
		if (total <= 0)
			return 100;
		long gasto = ChronoUnit.DAYS.between(s, LocalDate.now());
		return (int) Math.max(0, Math.min(100, gasto * 100 / total));
	}

	// ====================== FILTROS DOS CARDS ======================

	public List<AcademicYearEntity> getFilteredYears()
	{
		if (academicYears == null)
			return List.of();
		return academicYears.stream().filter(a -> cardFilter == null || matches(a, cardFilter))
				.sorted(Comparator.comparing(this::start, Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
	}

	private boolean matches(AcademicYearEntity a, String key)
	{
		return switch (key)
		{
		case "ACTIVE" -> isActiveYear(a);
		case "INACTIVE" -> !isActiveYear(a);
		default -> key.equals(periodOf(a));
		};
	}

	public void filterBy(String key)
	{
		this.cardFilter = key.equals(cardFilter) ? null : key;
	}

	public void clearFilter()
	{
		this.cardFilter = null;
	}

	public boolean isNoFilter()
	{
		return cardFilter == null;
	}

	// ====================== CONTAGENS ======================

	public int getTotalYears()
	{
		return academicYears == null ? 0 : academicYears.size();
	}

	public long countBy(String key)
	{
		if (academicYears == null)
			return 0;
		return academicYears.stream().filter(a -> matches(a, key)).count();
	}

	// ====================== GETTERS / SETTERS ======================

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

	public String getCardFilter()
	{
		return cardFilter;
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