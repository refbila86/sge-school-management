package mz.co.sge.controller;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.GuardianEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.entity.UserEntity;
import mz.co.sge.service.IAcademicYearService;
import mz.co.sge.service.IGuardianService;
import mz.co.sge.service.IStudentService;

@Component("guardianBean")
@Scope("view")
public class GuardianBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private List<GuardianEntity> guardians;
	private List<GuardianEntity> filteredGuardians;
	private List<AcademicYearEntity> academicYears;

	private GuardianEntity guardian = new GuardianEntity();
	private GuardianEntity selectedGuardian;
	private List<StudentEntity> studentsOfGuardian;

	private Long selectedAcademicYearId;
	private boolean editing = false;
	private String filterTerm;
	private String searchTerm;
	private AcademicYearEntity currentAcademicYear;

	@Autowired
	private MenuBean menuBean;

	@Autowired
	private transient IGuardianService guardianService;

	@Autowired
	private transient IStudentService studentService;

	@Autowired
	private transient IAcademicYearService academicYearService;

	@Autowired
	private transient LoginBean loginBean;

	@Autowired
	private transient DashboardBean dashboardBean;

	@PostConstruct
	public void init()
	{
		loadCurrentAcademicYear();
		loadAcademicYears();
		loadGuardians();

//		try
//		{
//			menuBean.refreshCounts();
//		} catch (Exception e)
//		{
//		}
	}

	public void loadGuardians()
	{
		Long yearId = getCurrentAcademicYearId();
		if (yearId == null)
			yearId = loginBean != null ? loginBean.getAcademicYearId() : null;

		List<GuardianEntity> all;

		// BUSCA SEMPRE POR ANO
		if (yearId != null)
		{
			all = guardianService.getGuardiansByAcademicYear(yearId);
		} else
		{
			all = guardianService.getAllGuardians();
		}

		String term = (searchTerm != null && !searchTerm.isBlank()) ? searchTerm : filterTerm;
		if (term != null && !term.isBlank())
		{
			String t = term.toLowerCase().trim();
			this.guardians = all.stream()
					.filter(g -> (g.getCode() != null && g.getCode().toLowerCase().contains(t))
							|| (g.getFirstName() != null && g.getFirstName().toLowerCase().contains(t))
							|| (g.getLastName() != null && g.getLastName().toLowerCase().contains(t))
							|| (g.getFullName() != null && g.getFullName().toLowerCase().contains(t)) || (g.getPhone() != null && g.getPhone().contains(t))
							|| (g.getRelationship() != null && g.getRelationship().toLowerCase().contains(t)))
					.collect(Collectors.toList());
		} else
		{
			this.guardians = all;
		}
	}

	private Long getCurrentAcademicYearId()
	{
		if (loginBean != null && loginBean.getAcademicYearId() != null)
			return loginBean.getAcademicYearId();

		try
		{
			Object obj = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("selectedAcademicYearId");
			if (obj != null)
				return obj instanceof Long ? (Long) obj : Long.valueOf(obj.toString());
		} catch (Exception ignored)
		{
		}

		return selectedAcademicYearId;
	}

	public void loadCurrentAcademicYear()
	{
		try
		{
			Long yearId = getCurrentAcademicYearId();

			if (yearId == null)
			{
				Object objId = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("selectedAcademicYearId");
				if (objId != null)
				{
					yearId = objId instanceof Long ? (Long) objId : Long.valueOf(objId.toString());
				}
			}

			if (yearId != null)
			{
				currentAcademicYear = academicYearService.findByYearId(yearId);
				this.selectedAcademicYearId = yearId;
			} else
			{
				currentAcademicYear = null;
				this.selectedAcademicYearId = null;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			currentAcademicYear = null;
		}
	}

	public void search()
	{
		loadGuardians();
	}

	public void filter()
	{
		loadGuardians();
	}

	public void loadAcademicYears()
	{
		this.academicYears = academicYearService.getAllAcademicYears();
	}

	public void cleanForm()
	{
		this.guardian = new GuardianEntity();
		this.selectedAcademicYearId = loginBean != null ? loginBean.getAcademicYearId() : null;
		this.studentsOfGuardian = List.of();
		this.editing = false;
	}

	public void prepareNew()
	{
		cleanForm();
	}

	public void prepareEdit(GuardianEntity g)
	{
		this.guardian = guardianService.getGuardianWithStudents(g.getId()).orElse(g);
		this.selectedAcademicYearId = guardian.getAcademicYear() != null ? guardian.getAcademicYear().getId() : null;
		this.studentsOfGuardian = guardian.getStudents();
		this.editing = true;
	}

	public void prepareView(GuardianEntity g)
	{
		GuardianEntity full = guardianService.getGuardianWithStudents(g.getId()).orElse(g);
		this.selectedGuardian = full;
		this.studentsOfGuardian = full.getStudents();
	}

	public void prepareDelete(GuardianEntity g)
	{
		this.selectedGuardian = guardianService.getGuardianWithStudents(g.getId()).orElse(g);
		this.studentsOfGuardian = selectedGuardian.getStudents();
	}

	public void quickSaveForStudent()
	{
		try
		{
			if (guardian.getCode() == null || guardian.getCode().isBlank())
				guardian.setCode("ENC-" + (System.currentTimeMillis() % 100000));

			if (guardian.getAcademicYear() == null)
			{
				Long yearId = selectedAcademicYearId != null ? selectedAcademicYearId : (loginBean != null ? loginBean.getAcademicYearId() : null);
				if (yearId != null)
				{
					AcademicYearEntity ay = academicYearService.getAcademicYearById(yearId).orElse(null);
					guardian.setAcademicYear(ay);
				}
			}

			if (guardian.getUser() == null)
			{
				UserEntity loggedUser = null;
				try
				{
					loggedUser = loginBean.getLoggedUser();
				} catch (Exception ignored)
				{
				}
				if (loggedUser == null)
				{
					try
					{
						Long userId = loginBean.getLoggedUserId();
						if (userId != null)
						{
							loggedUser = new UserEntity();
							loggedUser.setId(userId);
						}
					} catch (Exception ignored)
					{
					}
				}
				if (loggedUser == null)
					throw new IllegalArgumentException("Usuário logado não encontrado");
				guardian.setUser(loggedUser);
			}

			GuardianEntity saved = guardianService.createGuardian(guardian);
			Long newId = saved != null ? saved.getId() : guardian.getId();

			// *** ATUALIZA O MENU ***
			menuBean.refreshCounts();

			try
			{
				FacesContext ctx = FacesContext.getCurrentInstance();
				StudentBean studentBean = ctx.getApplication().evaluateExpressionGet(ctx, "#{studentBean}", StudentBean.class);
				if (studentBean != null)
				{
					studentBean.loadGuardians();
					if (newId != null)
					{
						studentBean.setSelectedGuardianId(newId);
						studentBean.onGuardianChange();
					}
				}
			} catch (Exception ex)
			{
				loadGuardians();
			}

			addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", guardian.getFirstName() + " salvo! (" + menuBean.getGuardianCount() + " cadastrados)");
			guardian = new GuardianEntity();
			selectedAcademicYearId = loginBean.getAcademicYearId();
			loadGuardians();

		} catch (Exception e)
		{
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
		}
	}

	public void quickSaveForStudents()
	{
		try
		{
			if (guardian.getCode() == null || guardian.getCode().isBlank())
				guardian.setCode("ENC-" + (System.currentTimeMillis() % 100000));

			if (guardian.getAcademicYear() == null)
			{
				Long yearId = selectedAcademicYearId != null ? selectedAcademicYearId : (loginBean != null ? loginBean.getAcademicYearId() : null);
				if (yearId != null)
				{
					AcademicYearEntity ay = academicYearService.getAcademicYearById(yearId).orElse(null);
					guardian.setAcademicYear(ay);
				}
			}

			// USER LOGADO
			if (guardian.getUser() == null)
			{
				UserEntity loggedUser = null;
				try
				{
					loggedUser = loginBean.getLoggedUser();
				} catch (Exception ignored)
				{
				}
				if (loggedUser == null)
				{
					try
					{
						Long userId = loginBean.getLoggedUserId();
						if (userId != null)
						{
							loggedUser = new UserEntity();
							loggedUser.setId(userId);
						}
					} catch (Exception ignored)
					{
					}
				}
				if (loggedUser == null)
					throw new IllegalArgumentException("Usuário logado não encontrado");
				guardian.setUser(loggedUser);
			}

			// SALVA
			GuardianEntity saved = guardianService.createGuardian(guardian);
			Long newId = saved != null ? saved.getId() : guardian.getId();
			if (newId == null)
				newId = guardian.getId();

			// *** ATUALIZA O STUDENTBEAN PRA APARECER NA LISTA ***
			try
			{
				FacesContext ctx = FacesContext.getCurrentInstance();
				StudentBean studentBean = ctx.getApplication().evaluateExpressionGet(ctx, "#{studentBean}", StudentBean.class);
				if (studentBean != null)
				{
					studentBean.loadGuardians(); // recarrega lista
					if (newId != null)
					{
						studentBean.setSelectedGuardianId(newId);
						studentBean.onGuardianChange(); // carrega detalhe
					}
				}
			} catch (Exception ex)
			{
				// se não achar studentBean (view diferente), só recarrega lista local
				loadGuardians();
			}

			addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", guardian.getFirstName() + " " + guardian.getLastName() + " salvo! Agora já pode selecionar.");

			// limpa pro próximo
			guardian = new GuardianEntity();
			selectedAcademicYearId = loginBean.getAcademicYearId();
			loadGuardians();

		} catch (Exception e)
		{
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
		}
	}

	public void save()
	{
		try
		{
			saveInternal();
			loadGuardians();
			menuBean.refreshCounts();
			addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Guardian gravado - Total: " + menuBean.getGuardianCount());
			dashboardBean.navigate("guardians/guardian-list", "encarregados");
		} catch (Exception e)
		{
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
		}
	}

	public void saveGuardian()
	{
		try
		{
			saveInternal();
			loadGuardians();
			addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Guardian gravado");
			dashboardBean.navigate("guardians/guardian-list", "encarregados");
		} catch (Exception e)
		{
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage());
		}
	}

	private void saveInternal()
	{
		if (selectedAcademicYearId == null)
			selectedAcademicYearId = loginBean != null ? loginBean.getAcademicYearId() : null;
		if (selectedAcademicYearId == null)
			throw new IllegalArgumentException("Ano Letivo é obrigatório");

		AcademicYearEntity ay = academicYears != null ? academicYears.stream().filter(a -> a.getId().equals(selectedAcademicYearId)).findFirst().orElse(null)
				: null;
		if (ay == null)
			ay = academicYearService.getAcademicYearById(selectedAcademicYearId).orElseThrow(() -> new IllegalArgumentException("Ano Letivo não encontrado"));
		guardian.setAcademicYear(ay);

		// USER LOGADO
		if (guardian.getUser() == null)
		{
			UserEntity loggedUser = null;
			try
			{
				loggedUser = loginBean.getLoggedUser();
			} catch (Exception ignored)
			{
			}
			if (loggedUser == null)
			{
				try
				{
					Long userId = loginBean.getLoggedUser().getId();
					if (userId != null)
					{
						loggedUser = new UserEntity();
						loggedUser.setId(userId);
					}
				} catch (Exception ignored)
				{
				}
			}
			if (loggedUser == null)
				throw new IllegalArgumentException("Usuário logado não encontrado");
			guardian.setUser(loggedUser);
		}

		if (guardian.getCode() == null || guardian.getCode().isBlank())
			guardian.setCode("ENC-" + (System.currentTimeMillis() % 100000));

		if (editing)
			guardianService.updateGuardian(guardian);
		else
			guardianService.createGuardian(guardian);
	}

	public void delete()
	{
		try
		{
			if (selectedGuardian != null)
			{
				guardianService.deleteGuardian(selectedGuardian.getId());
				loadGuardians();
				menuBean.refreshCounts(); // <-- ATUALIZA MENU AO APAGAR
				addMessage("Removido", "Guardian removido - Restam: " + menuBean.getGuardianCount());
			}
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

	// Getters / Setters
	public List<GuardianEntity> getGuardians()
	{
		return guardians;
	}

	public void setGuardians(List<GuardianEntity> guardians)
	{
		this.guardians = guardians;
	}

	public List<GuardianEntity> getFilteredGuardians()
	{
		return filteredGuardians;
	}

	public void setFilteredGuardians(List<GuardianEntity> filteredGuardians)
	{
		this.filteredGuardians = filteredGuardians;
	}

	public List<AcademicYearEntity> getAcademicYears()
	{
		return academicYears;
	}

	public void setAcademicYears(List<AcademicYearEntity> academicYears)
	{
		this.academicYears = academicYears;
	}

	public GuardianEntity getGuardian()
	{
		return guardian;
	}

	public void setGuardian(GuardianEntity guardian)
	{
		this.guardian = guardian;
	}

	public GuardianEntity getSelectedGuardian()
	{
		return selectedGuardian;
	}

	public void setSelectedGuardian(GuardianEntity selectedGuardian)
	{
		this.selectedGuardian = selectedGuardian;
	}

	public List<StudentEntity> getStudentsOfGuardian()
	{
		return studentsOfGuardian;
	}

	public void setStudentsOfGuardian(List<StudentEntity> studentsOfGuardian)
	{
		this.studentsOfGuardian = studentsOfGuardian;
	}

	public Long getSelectedAcademicYearId()
	{
		return selectedAcademicYearId;
	}

	public void setSelectedAcademicYearId(Long selectedAcademicYearId)
	{
		this.selectedAcademicYearId = selectedAcademicYearId;
	}

	public boolean isEditing()
	{
		return editing;
	}

	public void setEditing(boolean editing)
	{
		this.editing = editing;
	}

	public String getFilterTerm()
	{
		return filterTerm;
	}

	public void setFilterTerm(String filterTerm)
	{
		this.filterTerm = filterTerm;
	}

	public String getSearchTerm()
	{
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm)
	{
		this.searchTerm = searchTerm;
	}
}