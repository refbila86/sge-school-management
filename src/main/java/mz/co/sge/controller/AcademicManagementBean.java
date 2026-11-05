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
import mz.co.sge.entity.GradeEntity;
import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.entity.SubjectEntity;
import mz.co.sge.enumes.Trimester;
import mz.co.sge.service.IAcademicYearService;
import mz.co.sge.service.IAttendanceService;
import mz.co.sge.service.IClassroomService;
import mz.co.sge.service.IGradeService;
import mz.co.sge.service.ISchoolService;
import mz.co.sge.service.IStudentService;
import mz.co.sge.service.ISubjectService;

@Named
@ViewScoped
public class AcademicManagementBean implements Serializable
{

	@Autowired
	private IStudentService studentService;

	@Autowired
	private IClassroomService classroomService;

	@Autowired
	private ISubjectService subjectService;

	@Autowired
	private IAcademicYearService academicYearService;

	@Autowired
	private IGradeService gradeService;

	@Autowired
	private IAttendanceService attendanceService;

	@Autowired
	private ISchoolService schoolService;

	private Long classroomId;
	private Long subjectId;
	private Long academicYearId;
	private AcademicYearEntity activeYear;
	private Trimester trimestreSelecionado = Trimester.T1;
	private LocalDate dataAula = LocalDate.now();

	private List<ClassroomEntity> classrooms = new ArrayList<>();
	private List<SubjectEntity> subjects = new ArrayList<>();
	private Trimester[] trimestres = Trimester.values();

	private List<NotaRow> notasRows = new ArrayList<>();
	private List<FaltaRow> faltasRows = new ArrayList<>();
	private Stats stats = new Stats();

	private Double mediaTurma = 0.0;
	private Double notaMaxima = 0.0;
	private Double notaMinima = 0.0;
	private long totalAprovados = 0;
	private long totalReprovados = 0;
	private long totalMuitoBom = 0;

	private SchoolEntity school;

	@PostConstruct
	public void init()
	{
		try
		{
			try
			{
				classrooms = classroomService.findAllActive();
			} catch (Exception ex)
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
			} catch (Exception ex)
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

			List<AcademicYearEntity> years = academicYearService.findAll();
			if (years != null && !years.isEmpty())
			{
				activeYear = years.stream().filter(y -> y.getActive() != null && y.getActive()).findFirst().orElse(years.get(0));
				academicYearId = activeYear.getId();
			}
			if (activeYear == null)
			{
				activeYear = new AcademicYearEntity();
				activeYear.setYear("2025");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			activeYear = new AcademicYearEntity();
			activeYear.setYear("2025");
			if (classrooms == null)
				classrooms = new ArrayList<>();
			if (subjects == null)
				subjects = new ArrayList<>();
		}

		try
		{
			setSchool(schoolService.getSchool());
		} catch (Exception e)
		{
			setSchool(new SchoolEntity());
			getSchool().setName("ESCOLA");
		}
	}

	public String getSelectedClassroomName()
	{
		if (classroomId == null || classrooms == null)
			return "-";
		return classrooms.stream().filter(c -> c.getId().equals(classroomId)).map(ClassroomEntity::getName).findFirst().orElse("-");
	}

	public String getSelectedSubjectName()
	{
		if (subjectId == null || subjects == null)
			return "-";
		return subjects.stream().filter(s -> s.getId().equals(subjectId)).map(SubjectEntity::getName).findFirst().orElse("-");
	}

	public void calcularStatsNotas()
	{
		if (notasRows == null || notasRows.isEmpty())
		{
			mediaTurma = 0.0;
			notaMaxima = 0.0;
			notaMinima = 0.0;
			totalAprovados = 0;
			totalReprovados = 0;
			totalMuitoBom = 0;
			return;
		}
		double soma = 0;
		int count = 0;
		double max = -1;
		double min = 21;
		long aprov = 0;
		long reprov = 0;
		long mBom = 0;
		for (NotaRow r : notasRows) // CORRIGIDO: era GradeRow
		{
			Double m = r.getGrade().getMediaFinal();
			if (m == null)
				continue;
			soma += m;
			count++;
			if (m > max)
				max = m;
			if (m < min)
				min = m;
			if (m >= 10)
				aprov++;
			else
				reprov++;
			if (m >= 16)
				mBom++;
		}
		mediaTurma = count > 0 ? Math.round((soma / count) * 10) / 10.0 : 0;
		notaMaxima = max == -1 ? 0 : max;
		notaMinima = min == 21 ? 0 : min;
		totalAprovados = aprov;
		totalReprovados = reprov;
		totalMuitoBom = mBom;
	}

	public void loadStudents()
	{
		if (classroomId == null || subjectId == null)
			return;
		if (academicYearId == null && activeYear != null)
			academicYearId = activeYear.getId();
		if (academicYearId == null)
			return;

		List<StudentEntity> students = studentService.findByClassroom(classroomId, academicYearId);
		if (students == null)
			students = new ArrayList<>();

		String className = getSelectedClassroomName();

		List<GradeEntity> grades = gradeService.findByFilters(classroomId, subjectId, academicYearId, trimestreSelecionado);
		Map<Long, GradeEntity> gradeMap = grades.stream().collect(Collectors.toMap(g -> g.getStudent().getId(), g -> g, (a, b) -> a));

		notasRows = new ArrayList<>();
		for (StudentEntity s : students)
		{
			GradeEntity g = gradeMap.getOrDefault(s.getId(), new GradeEntity());
			if (g.getId() == null)
			{
				g.setStudent(s);
				g.setClassroom(new ClassroomEntity(classroomId));
				g.getClassroom().setName(className);
				g.setSubject(new SubjectEntity(subjectId));
				g.setAcademicYear(new AcademicYearEntity(academicYearId));
				g.setTrimester(trimestreSelecionado);
			}
			notasRows.add(new NotaRow(s, g, className));
		}
		calcularStatsNotas();

		List<AttendanceEntity> atts = attendanceService.findByFilters(classroomId, subjectId, academicYearId, trimestreSelecionado);
		Map<Long, AttendanceEntity> attMap = atts.stream().collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a, (a, b) -> a));

		faltasRows = new ArrayList<>();
		long totalFaltas = 0;
		for (StudentEntity s : students)
		{
			AttendanceEntity a = attMap.getOrDefault(s.getId(), new AttendanceEntity());
			if (a.getId() == null)
			{
				a.setStudent(s);
				ClassroomEntity c = new ClassroomEntity(classroomId);
				c.setName(className);
				a.setClassroom(c);
				a.setSubject(new SubjectEntity(subjectId));
				a.setAcademicYear(new AcademicYearEntity(academicYearId));
				a.setTrimester(trimestreSelecionado);
				a.setAttendanceDate(dataAula);
				a.setPresent(true);
			}
			if (a.getPresent() != null && !a.getPresent())
			{
				totalFaltas++;
			}
			faltasRows.add(new FaltaRow(a, new FaltaStats(0)));
		}
		stats.setFaltasTrimestre((int) totalFaltas);
	}

	public String getAproveitamento(Double media)
	{
		if (media == null)
			return "-";
		if (media < 10)
			return "Mau";
		if (media < 14)
			return "Suficiente";
		if (media < 16)
			return "Bom";
		return "Muito Bom";
	}

	public String getSeveridade(Double media)
	{
		if (media == null)
			return "info";
		if (media < 10)
			return "danger";
		if (media < 14)
			return "warning";
		if (media < 16)
			return "info";
		return "success";
	}

	public void recalculate()
	{
		calcularStatsNotas();
	}

	public Double getMediaTurma()
	{
		return mediaTurma;
	}

	public Double getNotaMaxima()
	{
		return notaMaxima;
	}

	public Double getNotaMinima()
	{
		return notaMinima;
	}

	public long getTotalAprovados()
	{
		return totalAprovados;
	}

	public long getTotalReprovados()
	{
		return totalReprovados;
	}

	public long getTotalMuitoBom()
	{
		return totalMuitoBom;
	}

	public double getTaxaAprovacao()
	{
		if (notasRows.isEmpty())
			return 0;
		return Math.round((totalAprovados * 100.0 / notasRows.size()) * 10) / 10.0;
	}

	public void onDateChange()
	{
		loadStudents();
	}

	public void saveNotas()
	{
		gradeService.saveAll(notasRows.stream().map(NotaRow::getGrade).collect(Collectors.toList()));
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Notas salvas"));
	}

	public void saveFaltas()
	{
		List<AttendanceEntity> list = faltasRows.stream().map(FaltaRow::getAttendance).peek(a ->
		{
			a.setAttendanceDate(dataAula);
			a.setTrimester(trimestreSelecionado);
		}).collect(Collectors.toList());
		attendanceService.saveAll(list);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Faltas salvas"));
	}

	// Inner classes
	public static class NotaRow implements Serializable
	{
		private StudentEntity student;
		private GradeEntity grade;
		private String classroomName;

		public NotaRow(StudentEntity s, GradeEntity g)
		{
			student = s;
			grade = g;
			// pega turma do grade se tiver
			if (g != null && g.getClassroom() != null)
			{
				classroomName = g.getClassroom().getName();
			}
		}

		public NotaRow(StudentEntity s, GradeEntity g, String className)
		{
			student = s;
			grade = g;
			classroomName = className;
		}

		public StudentEntity getStudent()
		{
			return student;
		}

		public GradeEntity getGrade()
		{
			return grade;
		}

		public String getClassroomName()
		{
			return classroomName;
		}
	}

	public String getCurrentSubjectName()
	{
		if (subjectId == null || subjects == null)
			return "-";
		return subjects.stream().filter(s -> s.getId().equals(subjectId)).map(SubjectEntity::getName).findFirst().orElse("-");
	}

	public String getCurrentClassroomName()
	{
		if (classroomId == null || classrooms == null)
			return "-";
		return classrooms.stream().filter(c -> c.getId().equals(classroomId)).map(ClassroomEntity::getName).findFirst().orElse("-");
	}

	public static class FaltaRow implements Serializable
	{
		private AttendanceEntity attendance;
		private FaltaStats stats;

		public FaltaRow(AttendanceEntity a, FaltaStats s)
		{
			attendance = a;
			stats = s;
		}

		public AttendanceEntity getAttendance()
		{
			return attendance;
		}

		public FaltaStats getStats()
		{
			return stats;
		}

		public StudentEntity getStudent()
		{
			return attendance != null ? attendance.getStudent() : null;
		}

		public String getClassroomName()
		{
			if (attendance != null && attendance.getClassroom() != null)
			{
				return attendance.getClassroom().getName();
			}
			return "-";
		}
	}

	public static class FaltaStats implements Serializable
	{
		private long totalTrimestre;

		public FaltaStats(long t)
		{
			totalTrimestre = t;
		}

		public long getTotalTrimestre()
		{
			return totalTrimestre;
		}
	}

	public static class Stats implements Serializable
	{
		private int faltasTrimestre;

		public int getFaltasTrimestre()
		{
			return faltasTrimestre;
		}

		public void setFaltasTrimestre(int v)
		{
			faltasTrimestre = v;
		}
	}

	// Getters/Setters
	public Long getClassroomId()
	{
		return classroomId;
	}

	public void setClassroomId(Long v)
	{
		classroomId = v;
	}

	public Long getSubjectId()
	{
		return subjectId;
	}

	public void setSubjectId(Long v)
	{
		subjectId = v;
	}

	public Long getAcademicYearId()
	{
		return academicYearId;
	}

	public void setAcademicYearId(Long v)
	{
		academicYearId = v;
	}

	public Trimester getTrimestreSelecionado()
	{
		return trimestreSelecionado;
	}

	public void setTrimestreSelecionado(Trimester v)
	{
		trimestreSelecionado = v;
	}

	public LocalDate getDataAula()
	{
		return dataAula;
	}

	public void setDataAula(LocalDate v)
	{
		dataAula = v;
	}

	public List<ClassroomEntity> getClassrooms()
	{
		return classrooms;
	}

	public List<SubjectEntity> getSubjects()
	{
		return subjects;
	}

	public Trimester[] getTrimestres()
	{
		return trimestres;
	}

	public List<NotaRow> getNotasRows()
	{
		return notasRows;
	}

	public List<FaltaRow> getFaltasRows()
	{
		return faltasRows;
	}

	public AcademicYearEntity getActiveYear()
	{
		return activeYear;
	}

	public Stats getStats()
	{
		return stats;
	}

	public SchoolEntity getSchool()
	{
		return school;
	}

	public void setSchool(SchoolEntity school)
	{
		this.school = school;
	}
}