package mz.co.sge.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.repository.IStudentRepository;
import mz.co.sge.controller.LoginBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FacesConverter(value = "studentConverter", managed = true)
public class StudentConverter implements Converter<StudentEntity>
{

	@Autowired
	private IStudentRepository studentRepository;
	@Autowired
	private LoginBean loginBean;

	@Override
	public StudentEntity getAsObject(FacesContext ctx, UIComponent comp, String value)
	{
		if (value == null || value.isBlank())
			return null;
		try
		{
			Long id = Long.parseLong(value);
			return studentRepository.findByIdAndSchoolIdAndAcademicYearId(id, loginBean.getSchoolId(), loginBean.getAcademicYearId()).orElse(null);
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