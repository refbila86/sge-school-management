package mz.co.sge.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.AttendanceEntity;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.entity.SubjectEntity;
import mz.co.sge.enumes.Trimester;
import mz.co.sge.service.IAcademicYearService;
import mz.co.sge.service.IAttendanceService;
import mz.co.sge.service.IClassroomService;
import mz.co.sge.service.IStudentService;
import mz.co.sge.service.ISubjectService;

@Named
@ViewScoped
public class GradeManagementBean implements Serializable
{

	private static final long serialVersionUID = 1L;

	@Autowired
	private IAttendanceService attendanceService;
	@Autowired
	private IStudentService studentService;
	@Autowired
	private IClassroomService classroomService;
	@Autowired
	private ISubjectService subjectService;
	@Autowired
	private IAcademicYearService academicYearService;

	private Long classroomId;
	private Long subjectId;
	private Long academicYearId;
	private Trimester trimester = Trimester.T1;
	private LocalDate attendanceDate = LocalDate.now();

	private List<ClassroomEntity> classrooms = new ArrayList<>();
	private List<SubjectEntity> subjects = new ArrayList<>();
	private List<AcademicYearEntity> academicYears = new ArrayList<>();
	private Trimester[] trimesters = Trimester.values();

	private List<AttendanceWrapper> attendances = new ArrayList<>();
	private List<StudentEntity> students = new ArrayList<>();

	@PostConstruct
	public void init()
	{
		try
		{
			try
			{
				classrooms = classroomService.findAllActive();
			} catch (Exception e)
			{
				try
				{
					classrooms = classroomService.findAll();
				} catch (Exception e2)
				{
					classrooms = classroomService.getAllClassrooms();
				}
			}
			if (classrooms == null)
				classrooms = new ArrayList<>();

			try
			{
				subjects = subjectService.findAllActive();
			} catch (Exception e)
			{
				try
				{
					subjects = subjectService.findAll();
				} catch (Exception e2)
				{
					subjects = subjectService.getAllSubjects();
				}
			}
			if (subjects == null)
				subjects = new ArrayList<>();

			academicYears = academicYearService.findAll();
			if (academicYears != null && !academicYears.isEmpty())
			{
				AcademicYearEntity active = academicYears.stream().filter(y -> y.getActive() != null && y.getActive()).findFirst().orElse(academicYears.get(0));
				academicYearId = active.getId();
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void loadStudents()
	{
		if (classroomId == null || academicYearId == null || subjectId == null)
		{
			addMsg(FacesMessage.SEVERITY_WARN, "Selecione turma, disciplina, ano e trimestre");
			return;
		}

		students = studentService.findByClassroom(classroomId, academicYearId);

		if (students == null || students.isEmpty())
		{
			attendances.clear();
			addMsg(FacesMessage.SEVERITY_WARN, "Nenhum aluno encontrado para esta turma/ano");
			return;
		}

		// Busca faltas já existentes - com data
		List<AttendanceEntity> existing = attendanceService.findByFilters(classroomId, subjectId, academicYearId, trimester);

		Map<Long, AttendanceEntity> existingMap = existing.stream().collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a, (a, b) -> a));

		attendances = new ArrayList<>();
		for (StudentEntity s : students)
		{
			AttendanceEntity att = existingMap.get(s.getId());
			if (att == null)
			{
				att = new AttendanceEntity();
				att.setStudent(s);
				att.setClassroom(new ClassroomEntity(classroomId));
				att.setSubject(new SubjectEntity(subjectId));
				att.setAcademicYear(new AcademicYearEntity(academicYearId));
				att.setTrimester(trimester);
				att.setAttendanceDate(attendanceDate);
				att.setPresent(true);
				att.setJustified(false);
			}
			attendances.add(new AttendanceWrapper(att));
		}
	}

	public void save()
	{
		if (attendances.isEmpty())
		{
			addMsg(FacesMessage.SEVERITY_WARN, "Nada para salvar");
			return;
		}
		try
		{
			List<AttendanceEntity> toSave = attendances.stream().map(AttendanceWrapper::getEntity).collect(Collectors.toList());
			for (AttendanceEntity a : toSave)
			{
				a.setAttendanceDate(attendanceDate);
				a.setTrimester(trimester);
			}
			attendanceService.saveAll(toSave);
			addMsg(FacesMessage.SEVERITY_INFO, "Presenças salvas: " + toSave.size());
		} catch (Exception e)
		{
			addMsg(FacesMessage.SEVERITY_ERROR, "Erro: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void markAllPresent()
	{
		attendances.forEach(w -> w.getEntity().setPresent(true));
	}

	public void markAllAbsent()
	{
		attendances.forEach(w -> w.getEntity().setPresent(false));
	}

	private void addMsg(FacesMessage.Severity severity, String msg)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, msg, null));
	}

	public static class AttendanceWrapper implements Serializable
	{
		private static final long serialVersionUID = 1L;
		private AttendanceEntity entity;

		public AttendanceWrapper(AttendanceEntity entity)
		{
			this.entity = entity;
		}

		public AttendanceEntity getEntity()
		{
			return entity;
		}

		public void setEntity(AttendanceEntity entity)
		{
			this.entity = entity;
		}
	}

	// Getters e Setters
	public Long getClassroomId()
	{
		return classroomId;
	}

	public void setClassroomId(Long classroomId)
	{
		this.classroomId = classroomId;
	}

	public Long getSubjectId()
	{
		return subjectId;
	}

	public void setSubjectId(Long subjectId)
	{
		this.subjectId = subjectId;
	}

	public Long getAcademicYearId()
	{
		return academicYearId;
	}

	public void setAcademicYearId(Long academicYearId)
	{
		this.academicYearId = academicYearId;
	}

	public Trimester getTrimester()
	{
		return trimester;
	}

	public void setTrimester(Trimester trimester)
	{
		this.trimester = trimester;
	}

	public LocalDate getAttendanceDate()
	{
		return attendanceDate;
	}

	public void setAttendanceDate(LocalDate attendanceDate)
	{
		this.attendanceDate = attendanceDate;
	}

	public List<ClassroomEntity> getClassrooms()
	{
		return classrooms;
	}

	public List<SubjectEntity> getSubjects()
	{
		return subjects;
	}

	public List<AcademicYearEntity> getAcademicYears()
	{
		return academicYears;
	}

	public Trimester[] getTrimesters()
	{
		return trimesters;
	}

	public List<AttendanceWrapper> getAttendances()
	{
		return attendances;
	}

	public List<StudentEntity> getStudents()
	{
		return students;
	}
}