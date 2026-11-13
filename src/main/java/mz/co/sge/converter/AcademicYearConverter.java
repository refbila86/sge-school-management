package mz.co.sge.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.service.IAcademicYearService;

@Component
@FacesConverter(value = "academicYearConverter", managed = true)
public class AcademicYearConverter implements Converter<AcademicYearEntity>
{

	@Autowired
	private IAcademicYearService academicYearService;

	@Override
	public AcademicYearEntity getAsObject(FacesContext ctx, UIComponent comp, String value)
	{
		if (value == null || value.isEmpty())
			return null;
		try
		{
			Long id = Long.valueOf(value);
			return academicYearService.findByYearId(id);
		} catch (Exception e)
		{
			return null;
		}
	}

	@Override
	public String getAsString(FacesContext ctx, UIComponent comp, AcademicYearEntity year)
	{
		if (year == null || year.getId() == null)
			return "";
		return year.getId().toString();
	}
}