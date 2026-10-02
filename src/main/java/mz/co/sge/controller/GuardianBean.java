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
import mz.co.sge.service.IAcademicYearService;
import mz.co.sge.service.IGuardianService;
import mz.co.sge.service.IStudentService;

@Component("guardianBean")
@Scope("view")
public class GuardianBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Autowired
	private transient IGuardianService guardianService;
	@Autowired
	private transient IStudentService studentService;
	@Autowired
	private transient IAcademicYearService academicYearService;

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

	public GuardianBean()
	{
	}

	public GuardianBean(IGuardianService guardianService, IStudentService studentService, IAcademicYearService academicYearService)
	{
		this.guardianService = guardianService;
		this.studentService = studentService;
		this.academicYearService = academicYearService;
	}

	@PostConstruct
	public void init()
	{
		loadGuardians();
		loadAcademicYears();
	}

	public void loadGuardians()
	{
		String term = (searchTerm != null && !searchTerm.isBlank()) ? searchTerm : filterTerm;
		if (term != null && !term.isBlank())
		{
			String t = term.toLowerCase().trim();
			List<GuardianEntity> all = guardianService.getAllGuardians();
			this.guardians = all.stream()
					.filter(g -> g.getCode().toLowerCase().contains(t) || g.getFullName().toLowerCase().contains(t)
							|| (g.getPhone() != null && g.getPhone().contains(t))
							|| (g.getRelationship() != null && g.getRelationship().toLowerCase().contains(t)))
					.collect(Collectors.toList());
		} else
		{
			this.guardians = guardianService.getAllGuardians();
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

	public String getYearLabel(Long id)
	{
		if (id == null)
			return "Geral";
		if (academicYears == null)
			return id.toString();
		return academicYears.stream().filter(ay -> ay.getId().equals(id)).map(ay -> String.valueOf(ay.getYear())).findFirst().orElse(id.toString());
	}

	public void cleanForm()
	{
		this.guardian = new GuardianEntity();
		this.selectedAcademicYearId = null;
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

	public String saves()
	{
		try
		{
			saveInternal();
			addMessage(editing ? "Atualizado" : "Criado", "Guardian " + guardian.getFullName() + (editing ? " atualizado" : " cadastrado"));
			loadGuardians();
			return "/pages/guardians/guardian-list?faces-redirect=true";
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
			saveInternal();
			loadGuardians();
			this.guardian = new GuardianEntity();
			this.selectedAcademicYearId = null;
			this.studentsOfGuardian = List.of();
			this.editing = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Guardian gravado"));
		} catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	private void saveInternal()
	{
		if (selectedAcademicYearId == null)
			throw new IllegalArgumentException("Ano Letivo é obrigatório");
		AcademicYearEntity ay = academicYears.stream().filter(a -> a.getId().equals(selectedAcademicYearId)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Ano Letivo não encontrado"));
		guardian.setAcademicYear(ay);
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
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Removido", "Guardian removido"));
			}
		} catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
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
