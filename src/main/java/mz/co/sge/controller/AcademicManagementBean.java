package mz.co.sge.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mz.co.sge.dto.PautaAnualDTO;
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.AttendanceEntity;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.entity.GradeEntity;
import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.entity.SubjectEntity;
import mz.co.sge.enumes.Trimester;
import mz.co.sge.repository.IPautaAnualRepository;
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

	@Autowired
	private IPautaAnualRepository pautaAnualRepository;

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

	private List<PautaAnualDTO> pautaAnual = new ArrayList<>();
	private boolean pautaGerada = false;

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

	// ============ ANUAL - NOVO ============
	public boolean isAnual()
	{
		return trimestreSelecionado == Trimester.ANUAL;
	}

	public void gerarPautaAnual()
	{
		if (classroomId == null || subjectId == null)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Seleciona a Turma e a Disciplina", null));
			return;
		}

		Long ano = null;
		try
		{
			ano = Long.valueOf(activeYear.getYear());
		} catch (Exception e)
		{
			ano = null;
		}

		pautaAnual = pautaAnualRepository.findPautaAnual(ano, null, getCurrentClassroomName(), getCurrentSubjectName());
		pautaGerada = true;
	}

	public void gerarPautaAnualExcel()
	{
		if (classroomId == null || subjectId == null)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Seleciona a Turma e a Disciplina", null));
			return;
		}

		Long ano = null;
		try
		{
			ano = Long.valueOf(activeYear.getYear());
		} catch (Exception e)
		{
			ano = null;
		}

		pautaAnual = pautaAnualRepository.findPautaAnual(ano, null, getCurrentClassroomName(), getCurrentSubjectName());
		pautaGerada = true;

		if (school == null)
		{
			try
			{
				school = schoolService.getSchool();
			} catch (Exception e)
			{
				school = new SchoolEntity();
				school.setName("ESCOLA");
			}
		}

		try
		{
			Workbook wb = new XSSFWorkbook();
			Sheet sheet = wb.createSheet("Pauta Anual");

			// --- Estilos - TUDO ALINHADO À ESQUERDA ---
			CellStyle headerStyle = wb.createCellStyle();
			headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			Font hf = wb.createFont();
			hf.setBold(true);
			hf.setColor(IndexedColors.WHITE.getIndex());
			headerStyle.setFont(hf);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);

			CellStyle titleStyle = wb.createCellStyle();
			Font tf = wb.createFont();
			tf.setBold(true);
			tf.setFontHeightInPoints((short) 16);
			tf.setColor(IndexedColors.DARK_BLUE.getIndex());
			titleStyle.setFont(tf);
			titleStyle.setAlignment(HorizontalAlignment.LEFT);

			CellStyle subStyle = wb.createCellStyle();
			Font sf = wb.createFont();
			sf.setBold(true);
			sf.setFontHeightInPoints((short) 11);
			sf.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
			subStyle.setFont(sf);
			subStyle.setAlignment(HorizontalAlignment.LEFT);

			CellStyle infoStyle = wb.createCellStyle();
			Font inf = wb.createFont();
			inf.setBold(true);
			inf.setFontHeightInPoints((short) 11);
			infoStyle.setFont(inf);
			infoStyle.setAlignment(HorizontalAlignment.LEFT);

			CellStyle infoValueStyle = wb.createCellStyle();
			Font infV = wb.createFont();
			infV.setFontHeightInPoints((short) 11);
			infoValueStyle.setFont(infV);
			infoValueStyle.setAlignment(HorizontalAlignment.LEFT);

			String nomeEscola = school != null && school.getName() != null ? school.getName() : "";
			String codigoEscola = school != null && school.getCode() != null ? school.getCode() : "";
			String nuitEscola = school != null && school.getNuit() != null ? school.getNuit() : "";
			String telEscola = school != null && school.getPhone() != null ? school.getPhone() : "";
			String emailEscola = school != null && school.getEmail() != null ? school.getEmail() : "";
			String endEscola = school != null && school.getAddress() != null ? school.getAddress() : "";

			String turma = pautaAnual != null && !pautaAnual.isEmpty() && pautaAnual.get(0).getTurma() != null ? pautaAnual.get(0).getTurma()
					: getCurrentClassroomName();
			String classe = pautaAnual != null && !pautaAnual.isEmpty() && pautaAnual.get(0).getClasse() != null ? pautaAnual.get(0).getClasse() : "";
			String disciplina = pautaAnual != null && !pautaAnual.isEmpty() && pautaAnual.get(0).getDisciplina() != null ? pautaAnual.get(0).getDisciplina()
					: getCurrentSubjectName();
			String turno = pautaAnual != null && !pautaAnual.isEmpty() && pautaAnual.get(0).getTurno() != null ? pautaAnual.get(0).getTurno() : "";

			// Grelha SEM Classe / Turma / Disciplina: 21 colunas (0 a 20)
			int totalCols = 21;
			int lastCol = totalCols - 1;

			// --- CABEÇALHO - DADOS DA ESCOLA À ESQUERDA ---
			Row r0 = sheet.createRow(0);
			Cell c0 = r0.createCell(0);
			c0.setCellValue(nomeEscola);
			c0.setCellStyle(titleStyle);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastCol));

			Row r1 = sheet.createRow(1);
			Cell c1 = r1.createCell(0);
			c1.setCellValue("Código: " + codigoEscola + "  |  NUIT: " + nuitEscola + "  |  Tel: " + telEscola + "  |  Email: " + emailEscola);
			c1.setCellStyle(subStyle);
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, lastCol));

			Row r2 = sheet.createRow(2);
			Cell c2 = r2.createCell(0);
			c2.setCellValue(endEscola);
			c2.setCellStyle(subStyle);
			sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, lastCol));

			Row r3 = sheet.createRow(3);
			Cell c3 = r3.createCell(0);
			c3.setCellValue("PAUTA ANUAL  -  Ano Lectivo " + activeYear.getYear());
			c3.setCellStyle(titleStyle);
			sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, lastCol));

			// --- CABEÇALHO FIXO - UM POR LINHA ---
			Row r4 = sheet.createRow(4);
			Cell c4a = r4.createCell(0);
			c4a.setCellValue("Classe:");
			c4a.setCellStyle(infoStyle);
			Cell c4b = r4.createCell(1);
			c4b.setCellValue(classe);
			c4b.setCellStyle(infoValueStyle);
			sheet.addMergedRegion(new CellRangeAddress(4, 4, 1, lastCol));

			Row r5 = sheet.createRow(5);
			Cell c5a = r5.createCell(0);
			c5a.setCellValue("Turma:");
			c5a.setCellStyle(infoStyle);
			Cell c5b = r5.createCell(1);
			c5b.setCellValue(turma);
			c5b.setCellStyle(infoValueStyle);
			sheet.addMergedRegion(new CellRangeAddress(5, 5, 1, lastCol));

			Row r6 = sheet.createRow(6);
			Cell c6a = r6.createCell(0);
			c6a.setCellValue("Disciplina:");
			c6a.setCellStyle(infoStyle);
			Cell c6b = r6.createCell(1);
			c6b.setCellValue(disciplina);
			c6b.setCellStyle(infoValueStyle);
			sheet.addMergedRegion(new CellRangeAddress(6, 6, 1, lastCol));

			Row r7 = sheet.createRow(7);
			Cell c7a = r7.createCell(0);
			c7a.setCellValue("Turno:");
			c7a.setCellStyle(infoStyle);
			Cell c7b = r7.createCell(1);
			c7b.setCellValue(turno);
			c7b.setCellStyle(infoValueStyle);
			sheet.addMergedRegion(new CellRangeAddress(7, 7, 1, lastCol));

			Row r8 = sheet.createRow(8);
			Cell c8a = r8.createCell(0);
			c8a.setCellValue("Total de alunos:");
			c8a.setCellStyle(infoStyle);
			Cell c8b = r8.createCell(1);
			c8b.setCellValue(pautaAnual.size());
			c8b.setCellStyle(infoValueStyle);
			sheet.addMergedRegion(new CellRangeAddress(8, 8, 1, lastCol));

			sheet.createRow(9); // linha vazia

			// --- CABEÇALHO DA GRELHA - SEM Classe / Turma / Disciplina ---
			String[] headers =
			{ "Aluno", "I ACS1", "I ACS2", "I MACS", "I AP", "I MTrim", "I Estado", "II ACS1", "II ACS2", "II MACS", "II AP", "II MTrim", "II Estado",
					"III ACS1", "III ACS2", "III MACS", "III AP", "III MTrim", "III Estado", "MFinal", "Estado Final" };
			Row hr = sheet.createRow(10);
			for (int i = 0; i < headers.length; i++)
			{
				Cell c = hr.createCell(i);
				c.setCellValue(headers[i]);
				c.setCellStyle(headerStyle);
			}

			// Congela escola + Classe/Turma/Disciplina/Turno/Total + cabeçalho
			sheet.createFreezePane(0, 11);
			sheet.setAutoFilter(new CellRangeAddress(10, 10, 0, headers.length - 1));
			sheet.setRepeatingRows(CellRangeAddress.valueOf("1:11"));

			int rowNum = 11;
			for (PautaAnualDTO d : pautaAnual)
			{
				Row r = sheet.createRow(rowNum++);
				int col = 0;
				r.createCell(col++).setCellValue(d.getAluno() != null ? d.getAluno() : "");
				setNum(r, col++, d.getIAcs1());
				setNum(r, col++, d.getIAcs2());
				setNum(r, col++, d.getIMacs());
				setNum(r, col++, d.getIAp());
				setNum(r, col++, d.getIMTrim());
				r.createCell(col++).setCellValue(d.getIEstado() != null ? d.getIEstado() : "");
				setNum(r, col++, d.getIiAcs1());
				setNum(r, col++, d.getIiAcs2());
				setNum(r, col++, d.getIiMacs());
				setNum(r, col++, d.getIiAp());
				setNum(r, col++, d.getIiMTrim());
				r.createCell(col++).setCellValue(d.getIiEstado() != null ? d.getIiEstado() : "");
				setNum(r, col++, d.getIiiAcs1());
				setNum(r, col++, d.getIiiAcs2());
				setNum(r, col++, d.getIiiMacs());
				setNum(r, col++, d.getIiiAp());
				setNum(r, col++, d.getIiiMTrim());
				r.createCell(col++).setCellValue(d.getIiiEstado() != null ? d.getIiiEstado() : "");
				setNum(r, col++, d.getMFinal());
				r.createCell(col++).setCellValue(d.getEstadoFinal() != null ? d.getEstadoFinal() : "");
			}

			sheet.setColumnWidth(0, 35 * 256);
			for (int i = 1; i < headers.length; i++)
				sheet.setColumnWidth(i, 13 * 256);

			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext();
			ec.responseReset();
			ec.setResponseContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			String fileName = "Pauta_Anual_" + getCurrentSubjectName().replaceAll(" ", "_") + "_" + getCurrentClassroomName().replaceAll(" ", "_") + "_"
					+ activeYear.getYear() + ".xlsx";
			ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			wb.write(ec.getResponseOutputStream());
			wb.close();
			fc.responseComplete();

		} catch (Exception e)
		{
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao gerar Excel: " + e.getMessage(), null));
		}
	}

	private void setNum(Row r, int col, Double v)
	{
		Cell c = r.createCell(col);
		if (v != null)
			c.setCellValue(v);
		else
			c.setCellValue("");
	}

	public List<PautaAnualDTO> getPautaAnual()
	{
		return pautaAnual;
	}

	public void setPautaAnual(List<PautaAnualDTO> pautaAnual)
	{
		this.pautaAnual = pautaAnual;
	}

	public boolean isPautaGerada()
	{
		return pautaGerada;
	}

	public void setPautaGerada(boolean pautaGerada)
	{
		this.pautaGerada = pautaGerada;
	}
	// ============ FIM ANUAL ============

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
		for (NotaRow r : notasRows)
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
		pautaGerada = false;
		pautaAnual = new ArrayList<>();
		if (isAnual())
		{
			notasRows = new ArrayList<>();
			faltasRows = new ArrayList<>();
			return;
		}

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

	public static class NotaRow implements Serializable
	{
		private StudentEntity student;
		private GradeEntity grade;
		private String classroomName;

		public NotaRow(StudentEntity s, GradeEntity g)
		{
			student = s;
			grade = g;
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