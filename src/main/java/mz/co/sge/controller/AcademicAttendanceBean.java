package mz.co.sge.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.AttendanceEntity;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.entity.SubjectEntity;
import mz.co.sge.service.IAcademicYearService;
import mz.co.sge.service.IAttendanceService;
import mz.co.sge.service.IClassroomService;
import mz.co.sge.service.IStudentService;
import mz.co.sge.service.ISubjectService;

@Named
@ViewScoped
public class AcademicAttendanceBean implements Serializable
{
	@Autowired
	private IAcademicYearService academicYearService;
	@Autowired
	private IClassroomService classroomService;
	@Autowired
	private ISubjectService subjectService;
	@Autowired
	private IStudentService studentService;
	@Autowired
	private IAttendanceService attendanceService;
	// Filtros - só o que importa para falta
	private Long classroomId;
	private Long subjectId;
	private LocalDate dataAula = LocalDate.now();
	private AcademicYearEntity activeYear;
	private List<ClassroomEntity> classrooms;
	private List<SubjectEntity> subjects;
	private List<FaltaRow> faltasRows = new ArrayList<>();

	@PostConstruct
	public void init()
	{
		activeYear = academicYearService.findActive();
		classrooms = classroomService.getAllClassrooms();
		subjects = subjectService.getAllSubjects();
	}

	public void loadPresencas()
	{
		if (classroomId == null || subjectId == null)
		{
			//addWarn("Selecione Turma e Disciplina");
			return;
		}
		// Busca alunos
		List<StudentEntity> alunos = studentService.findByClassroom(classroomId, activeYear.getId());
		List<Long> ids = alunos.stream().map(StudentEntity::getId).toList();
		// 1 query: faltas deste dia
		List<AttendanceEntity> doDia = attendanceService.findByDateAndSubject(dataAula, subjectId, classroomId, null);
		Map<Long, AttendanceEntity> mapDia = doDia.stream().collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));
		// 1 query: total de faltas do aluno na disciplina (para alerta)
		//Map<Long, Long> totalFaltas = attendanceService.countFaltasBatch(ids, subjectId, null);
		faltasRows = new ArrayList<>();
		for (StudentEntity aluno : alunos)
		{
			AttendanceEntity att = mapDia.getOrDefault(aluno.getId(), new AttendanceEntity());
			if (att.getId() == null)
			{
				att.setStudent(aluno);
				att.setClassroom(new ClassroomEntity(classroomId));
				att.setSubject(new SubjectEntity(subjectId));
				att.setAcademicYear(activeYear);
				att.setAttendanceDate(dataAula);
				att.setPresent(true); // default presente
				att.setJustified(false);
			}
		//	long total = totalFaltas.getOrDefault(aluno.getId(), 0L);
		//	faltasRows.add(new FaltaRow(att, total));
		}
	}

	public void savePresencas()
	{
		List<AttendanceEntity> toSave = faltasRows.stream().map(FaltaRow::getAttendance).toList();
		attendanceService.saveAll(toSave);
//		addInfo("Chamada do dia " + dataAula + " salva!");
	}

	public void marcarTodosPresentes()
	{
		faltasRows.forEach(r -> r.getAttendance().setPresent(true));
	}

	public void marcarTodosFaltas()
	{
		faltasRows.forEach(r -> r.getAttendance().setPresent(false));
	}

	// getters/setters
	public static class FaltaRow implements Serializable
	{
		private AttendanceEntity attendance;
		private long totalAcumulado;

		public FaltaRow(AttendanceEntity a, long total)
		{
			this.attendance = a;
			this.totalAcumulado = total;
		}

		public AttendanceEntity getAttendance()
		{
			return attendance;
		}

		public long getTotalAcumulado()
		{
			return totalAcumulado;
		}

		public String getStatusCor()
		{
			return totalAcumulado >= 10 ? "vermelho" : totalAcumulado >= 5 ? "amarelo" : "verde";
		}
	}
}