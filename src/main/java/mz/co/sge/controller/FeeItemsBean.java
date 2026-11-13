package mz.co.sge.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import mz.co.sge.entity.FeeItemEntity;
import mz.co.sge.service.IFeeItemsService;

@Component("feeItemsBean")
@Scope("view")
public class FeeItemsBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private List<FeeItemEntity> items;
	private FeeItemEntity item;
	private FeeItemEntity selected;
	private boolean editing = false;
	private String searchTerm;
	private List<FeeItemEntity> filteredItems;

	@Autowired
	private IFeeItemsService service;
	@Autowired
	private LoginBean loginBean;

	@PostConstruct
	public void init()
	{
		load();
		item = new FeeItemEntity(); // <-- ADICIONA ISTO
		item.setActivo(true);
	}

	public void load()
	{
		Long schoolId = loginBean.getSchoolId();
		Long yearId = loginBean.getAcademicYearId();
		if (schoolId != null && yearId != null)
			items = service.getBySchoolAndYearId(schoolId, yearId);
		else if (schoolId != null)
			items = service.getBySchoolId(schoolId);
		else
			items = service.getAll();
	}

	public void prepareNew()
	{
		item = new FeeItemEntity();
		item.setActivo(true);
		item.setValor(BigDecimal.ZERO);
		item.setSchool(loginBean.getLoggedSchool());
		item.setAcademicYear(loginBean.getLoggedAcademicYear());
		editing = false;
	}

	public void prepareEdit(FeeItemEntity f)
	{
		item = f;
		editing = true;
	}

	public void prepareDelete(FeeItemEntity f)
	{
		selected = f;
	}

	public void save()
	{
		try
		{
			if (item.getSchool() == null)
				item.setSchool(loginBean.getLoggedSchool());
			if (item.getAcademicYear() == null)
				item.setAcademicYear(loginBean.getLoggedAcademicYear());
			if (editing)
				service.update(item);
			else
				service.create(item);
			load();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Item salvo"));
		} catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public void delete()
	{
		try
		{
			service.delete(selected.getId());
			load();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Removido"));
		} catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public String formatDate(java.time.LocalDateTime dt)
	{
		if (dt == null)
			return "-";
		return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
	}

	// getters/setters
	public List<FeeItemEntity> getItems()
	{
		return items;
	}

	public FeeItemEntity getItem()
	{
		return item;
	}

	public void setItem(FeeItemEntity i)
	{
		this.item = i;
	}

	public FeeItemEntity getSelected()
	{
		return selected;
	}

	public void setSelected(FeeItemEntity s)
	{
		this.selected = s;
	}

	public boolean isEditing()
	{
		return editing;
	}

	public String getSearchTerm()
	{
		return searchTerm;
	}

	public void setSearchTerm(String s)
	{
		this.searchTerm = s;
	}

	// getter e setter
	public List<FeeItemEntity> getFilteredItems()
	{
		return filteredItems;
	}

	public void setFilteredItems(List<FeeItemEntity> filteredItems)
	{
		this.filteredItems = filteredItems;
	}

}