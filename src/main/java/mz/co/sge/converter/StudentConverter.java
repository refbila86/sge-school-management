package mz.co.sge.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.repository.IStudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FacesConverter(value = "studentConverter", managed = true)
public class StudentConverter implements Converter<StudentEntity>
{
	@Autowired
	private IStudentRepository studentRepository;

	@Override
	public StudentEntity getAsObject(FacesContext ctx, UIComponent comp, String value)
	{
		if (value == null || value.isBlank() || value.equals("null"))
			return null;
		try
		{
			Long id = Long.parseLong(value.trim());
			// 1. tenta achar no ano logado
			// 2. fallback geral por ID - ESSENCIAL pro autocomplete não apagar
			return studentRepository.findById(id).orElseGet(() ->
			{
				try
				{
					return studentRepository.findByIdAndSchoolIdAndAcademicYearId(id, null, null).orElse(null);
				} catch (Exception e2)
				{
					return null;
				}
			});
		} catch (Exception e)
		{
			return null;
		}
	}

	@Override
	public String getAsString(FacesContext ctx, UIComponent comp, StudentEntity s)
	{
		if (s == null || s.getId() == null)
			return "";
		return String.valueOf(s.getId());
	}
}