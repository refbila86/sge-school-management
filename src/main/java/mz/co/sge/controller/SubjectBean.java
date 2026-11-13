package mz.co.sge.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.SubjectEntity;
import mz.co.sge.service.IAcademicYearService;
import mz.co.sge.service.ISubjectService;

@Component("subjectBean")
@Scope("view")
public class SubjectBean implements Serializable
{

	@Autowired
	private ISubjectService subjectService;

	@Autowired
	private IAcademicYearService academicYearService;

	private List<SubjectEntity> allSubjects;

	private List<SubjectEntity> filteredSubjects;

	private SubjectEntity selected = new SubjectEntity();

	private String searchText;

	private Long filterYearId;

	private List<AcademicYearEntity> academicYears;

//	@PostConstruct
//	public void init()
//	{
//		academicYears = academicYearService.getAllAcademicYears();
//		if (!academicYears.isEmpty())
//			filterYearId = academicYears.get(0).getId();
//		loadAll();
//	}
	
	@PostConstruct
	public void init() {
	    academicYears = academicYearService.getAllAcademicYears();
	    if (academicYears != null && !academicYears.isEmpty()) {
	        filterYearId = null;
	    }
	    loadAll();
	}

	private Long selectedYearId;

	// getter setter
	public void save()
	{
		if (selectedYearId != null)
		{
			AcademicYearEntity ay = academicYearService.findByYearId(selectedYearId);
			selected.setAcademicYear(ay);
		}
		subjectService.save(selected);
		selected = new SubjectEntity();
		selectedYearId = null;
		loadAll();
	}

	public void prepareEdit(SubjectEntity s)
	{
		selected = s;
		selectedYearId = s.getAcademicYear() != null ? s.getAcademicYear().getId() : null;
	}

	public void loadAll()
	{
		if (filterYearId == null)
		{
			allSubjects = subjectService.findAll();
		} else
		{
			allSubjects = subjectService.findByAcademicYearId(filterYearId);
		}
		if (allSubjects == null)
			allSubjects = new ArrayList<>();
		applyFilter();
	}

	public void applyFilter()
	{
		if (searchText == null || searchText.trim().isEmpty())
		{
			filteredSubjects = allSubjects;
		} else
		{
			String q = searchText.toLowerCase();
			filteredSubjects = allSubjects.stream().filter(s -> s.getName().toLowerCase().contains(q) || s.getCode().toLowerCase().contains(q))
					.collect(Collectors.toList());
		}
	}

	public void prepareNew()
	{
		selected = new SubjectEntity();
		selectedYearId = null;
	}

	public void delete(SubjectEntity s)
	{
		subjectService.delete(s.getId());
		loadAll();
	}

	public void clearFilters()
	{
		searchText = null;
		loadAll();
	}

	// getters setters
	public List<SubjectEntity> getFilteredSubjects()
	{
		return filteredSubjects;
	}

	public SubjectEntity getSelected()
	{
		return selected;
	}

	public void setSelected(SubjectEntity selected)
	{
		this.selected = selected;
	}

	public String getSearchText()
	{
		return searchText;
	}

	public void setSearchText(String searchText)
	{
		this.searchText = searchText;
	}

	public Long getFilterYearId()
	{
		return filterYearId;
	}

	public void setFilterYearId(Long filterYearId)
	{
		this.filterYearId = filterYearId;
	}

	public List<AcademicYearEntity> getAcademicYears()
	{
		return academicYears;
	}

	public List<SubjectEntity> getAllSubjects()
	{
		return allSubjects;
	}

	public Long getSelectedYearId()
	{
		return selectedYearId;
	}

	public void setSelectedYearId(Long selectedYearId)
	{
		this.selectedYearId = selectedYearId;
	}

}