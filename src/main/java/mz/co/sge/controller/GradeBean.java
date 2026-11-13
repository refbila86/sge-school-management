package mz.co.sge.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.entity.GradeEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.entity.SubjectEntity;
import mz.co.sge.enumes.Trimester;
import mz.co.sge.service.IAcademicYearService;
import mz.co.sge.service.IClassroomService;
import mz.co.sge.service.IGradeService;
import mz.co.sge.service.IStudentService;
import mz.co.sge.service.ISubjectService;

@Named
@ViewScoped
public class GradeBean implements Serializable
{

	private static final long serialVersionUID = 1L;

	@Autowired
	private IGradeService gradeService;

	@Autowired
	private IAcademicYearService academicYearService;

	@Autowired
	private IClassroomService classroomService;

	@Autowired
	private ISubjectService subjectService;

	@Autowired
	private IStudentService studentService;

	private Trimester trimestre = Trimester.T1;
	private Long yearId;
	private Long classroomId;
	private Long subjectId;

	private AcademicYearEntity activeYear;
	private List<ClassroomEntity> classrooms;
	private List<SubjectEntity> subjects;
	private List<GradeRow> rows;

	@PostConstruct
	public void init()
	{
		activeYear = academicYearService.findActive();
		if (activeYear != null)
			yearId = activeYear.getId();
		classrooms = classroomService.findAllActive();
		subjects = subjectService.findAllActive();
		rows = new ArrayList<>();
	}

	public Trimester[] getTrimestres()
	{
		return Trimester.values();
	}

	public void load()
	{
		if (classroomId == null || subjectId == null)
			return;
		List<StudentEntity> alunos = studentService.findByClassroom(classroomId, yearId);
		List<GradeEntity> existentes = gradeService.findByFilters(classroomId, subjectId, yearId, trimestre);
		Map<Long, GradeEntity> map = existentes.stream().collect(Collectors.toMap(g -> g.getStudent().getId(), g -> g));

		rows = new ArrayList<>();
		for (StudentEntity s : alunos)
		{
			GradeEntity g = map.getOrDefault(s.getId(), new GradeEntity());
			if (g.getId() == null)
			{
				g.setStudent(s);
				g.setClassroom(new ClassroomEntity(classroomId));
				g.setSubject(new SubjectEntity(subjectId));
				g.setAcademicYear(activeYear);
				g.setTrimester(trimestre);
			}
			rows.add(new GradeRow(s, g));
		}
	}

	public void save()
	{
		List<GradeEntity> toSave = rows.stream().map(GradeRow::getGrade).collect(Collectors.toList());
		toSave.forEach(g -> g.setTrimester(trimestre));
		gradeService.saveAll(toSave);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Notas do " + trimestre + " salvas!"));
	}

	// getters/setters
	public Trimester getTrimestre()
	{
		return trimestre;
	}

	public void setTrimestre(Trimester t)
	{
		this.trimestre = t;
	}

	public Long getClassroomId()
	{
		return classroomId;
	}

	public void setClassroomId(Long id)
	{
		this.classroomId = id;
	}

	public Long getSubjectId()
	{
		return subjectId;
	}

	public void setSubjectId(Long id)
	{
		this.subjectId = id;
	}

	public Long getYearId()
	{
		return yearId;
	}

	public List<ClassroomEntity> getClassrooms()
	{
		return classrooms;
	}

	public List<SubjectEntity> getSubjects()
	{
		return subjects;
	}

	public List<GradeRow> getRows()
	{
		return rows;
	}

	public AcademicYearEntity getActiveYear()
	{
		return activeYear;
	}

	public static class GradeRow implements Serializable
	{
		private StudentEntity student;
		private GradeEntity grade;

		public GradeRow(StudentEntity s, GradeEntity g)
		{
			this.student = s;
			this.grade = g;
		}

		public StudentEntity getStudent()
		{
			return student;
		}

		public GradeEntity getGrade()
		{
			return grade;
		}
	}
}