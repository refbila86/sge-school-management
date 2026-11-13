package mz.co.sge.controller;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.service.IEnrollmentService;
import mz.co.sge.service.IStudentService;

@Named("welcomeBean")
@SessionScoped
public class WelcomeBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private int totalAlunos = 0;
	private int totalTurmas = 0;
	private int totalProfessores = 0;
	private double taxaPresenca = 0.0;

	private BarChartModel barModel;
	private PieChartModel pieStatusModel;
	private PieChartModel pieGeneroModel;

	private List<AlunoResumo> ultimasMatriculas;
	private List<TurmaResumo> turmasResumo;
	
	@Autowired
	private LoginBean loginBean;
	
	@Autowired
	private IStudentService studentService;
	
	@Autowired
	private IEnrollmentService enrollmentService; 
	
	@PostConstruct
	public void init()
	{
		try
		{
			loadRealData();
		} catch (Exception e)
		{
			totalAlunos = 0;
			ultimasMatriculas = List.of();
			turmasResumo = List.of();
			createEmptyCharts();
		}
	}

	public void reload()
	{
		loadRealData();
	}

	private Long getCurrentSchoolId()
	{
		if (loginBean == null)
			return null;
		try
		{
			if (loginBean.getLoggedSchool() != null && loginBean.getLoggedSchool().getId() != null)
				return loginBean.getLoggedSchool().getId();
			return loginBean.getSchoolId();
		} catch (Exception e)
		{
			return null;
		}
	}

	private Long getCurrentAcademicYearId()
	{
		if (loginBean == null)
			return null;
		try
		{
			return loginBean.getAcademicYearId();
		} catch (Exception e)
		{
			return null;
		}
	}

	private void loadRealData()
	{
		Long schoolId = getCurrentSchoolId();
		Long yearId = getCurrentAcademicYearId();
		List<EnrollmentEntity> matriculasDoAno;
		if (schoolId != null && yearId != null)
			matriculasDoAno = enrollmentService.findBySchoolIdAndAcademicYearId(schoolId,yearId);
		else if (yearId != null)
			matriculasDoAno = enrollmentService.findByAcademicYearId(yearId);
		else
			matriculasDoAno = enrollmentService.findAll();
		if (matriculasDoAno == null)
			matriculasDoAno = List.of();
		this.totalAlunos = matriculasDoAno.size();
		this.totalTurmas = (int) matriculasDoAno.stream().map(e -> e.getClassroom() != null ? e.getClassroom().getId() : null).filter(Objects::nonNull)
				.distinct().count();
		this.totalProfessores = 0;
		this.taxaPresenca = totalAlunos > 0 ? 92.5 : 0;
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		this.ultimasMatriculas = matriculasDoAno.stream().sorted((a, b) -> b.getId().compareTo(a.getId())).limit(5).map(e ->
		{
			StudentEntity s = e.getStudent();
			String turma = e.getClassroom() != null ? e.getClassroom().getName() : "N/A";
			String estado = e.getStatus() != null ? e.getStatus().name() : "ENROLLED";
			return new AlunoResumo(s != null && s.getCode() != null ? s.getCode() : "S-" + (s != null ? s.getId() : e.getId()),
					s != null ? s.getFullName() : "Sem nome", turma, e.getCreatedAt() != null ? e.getCreatedAt().format(fmt) : "", estado);
		}).collect(Collectors.toList());
		List<StudentEntity> alunos = matriculasDoAno.stream().map(EnrollmentEntity::getStudent).filter(Objects::nonNull).collect(Collectors.toList());
		if (alunos.isEmpty() && schoolId != null && yearId != null)
		{
			alunos = studentService.findBySchoolIdAndAcademicYearId(schoolId, yearId);
		}
		this.turmasResumo = new ArrayList<>();
		createBarModel(alunos);
		createPieStatusModel(alunos);
		createPieGeneroModel(alunos);
	}

	private void loadRealDatas()
	{
		Long schoolId = getCurrentSchoolId();
		Long yearId = getCurrentAcademicYearId();

		List<StudentEntity> alunos;
		if (schoolId != null && yearId != null)
			alunos = studentService.findBySchoolIdAndAcademicYearId(schoolId, yearId);
		else if (schoolId != null)
			alunos = studentService.findBySchoolId(schoolId);
		else if (yearId != null)
			alunos = studentService.getStudentsByAcademicYearId(yearId);
		else
			alunos = studentService.getAllStudents();

		if (alunos == null)
			alunos = List.of();

		this.totalAlunos = alunos.size();
		this.totalTurmas = 0;
		this.totalProfessores = 0;
		this.taxaPresenca = totalAlunos > 0 ? 92.5 : 0;

		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		this.ultimasMatriculas = alunos.stream().sorted((a, b) ->
		{
			if (a.getCreatedAt() == null && b.getCreatedAt() == null)
				return 0;
			if (a.getCreatedAt() == null)
				return 1;
			if (b.getCreatedAt() == null)
				return -1;
			return b.getCreatedAt().compareTo(a.getCreatedAt());
		}).limit(5).map(s ->
		{
			// CORRIGIDO: pega ano da última matrícula, não s.getAcademicYear()
			String ano = "N/A";
			try
			{
				if (s.getEnrollments() != null && !s.getEnrollments().isEmpty())
				{
					EnrollmentEntity last = s.getEnrollments().get(0); // já está OrderBy DESC
					if (last.getAcademicYear() != null)
						ano = String.valueOf(last.getAcademicYear().getYear());
				}
			} catch (Exception e)
			{
				ano = s.getCode() != null && s.getCode().contains("-") ? s.getCode().substring(s.getCode().lastIndexOf("-") + 1) : "N/A";
			}
			return new AlunoResumo(s.getCode() != null ? s.getCode() : "S-" + s.getId(), s.getFullName(), ano,
					s.getCreatedAt() != null ? s.getCreatedAt().format(fmt) : "", s.getStatus() != null ? s.getStatus().name() : "Ativa");
		}).collect(Collectors.toList());

		this.turmasResumo = new ArrayList<>();
		createBarModel(alunos);
		createPieStatusModel(alunos);
		createPieGeneroModel(alunos);
	}

	private void createBarModel(List<StudentEntity> alunos)
	{
		barModel = new BarChartModel();
		ChartData data = new ChartData();
		Map<String, Long> porStatus = alunos.stream()
				.collect(Collectors.groupingBy(s -> s.getStatus() != null ? s.getStatus().name() : "SEM_STATUS", Collectors.counting()));
		BarChartDataSet dataSet = new BarChartDataSet();
		dataSet.setLabel("Alunos por Estado");
		List<Number> values = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		porStatus.forEach((k, v) ->
		{
			labels.add(k);
			values.add(v);
		});
		if (labels.isEmpty())
		{
			labels.add("Sem dados");
			values.add(0);
		}
		dataSet.setData(values);
		dataSet.setBackgroundColor(List.of("rgba(59,130,246,0.7)", "rgba(16,185,129,0.7)", "rgba(245,158,11,0.7)", "rgba(239,68,68,0.7)"));
		dataSet.setBorderColor(List.of("rgb(59,130,246)", "rgb(16,185,129)", "rgb(245,158,11)", "rgb(239,68,68)"));
		dataSet.setBorderWidth(1);
		data.addChartDataSet(dataSet);
		data.setLabels(labels);
		BarChartOptions options = new BarChartOptions();
		Title title = new Title();
		title.setDisplay(true);
		title.setText("Alunos por Estado - " + (loginBean != null ? loginBean.getSchoolName() : ""));
		options.setTitle(title);
		barModel.setData(data);
		barModel.setOptions(options);
	}

	private void createPieStatusModel(List<StudentEntity> alunos)
	{
		pieStatusModel = new PieChartModel();
		ChartData data = new ChartData();
		Map<String, Long> porStatus = alunos.stream()
				.collect(Collectors.groupingBy(s -> s.getStatus() != null ? s.getStatus().name() : "SEM_STATUS", Collectors.counting()));
		PieChartDataSet dataSet = new PieChartDataSet();
		List<Number> values = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		porStatus.forEach((k, v) ->
		{
			labels.add(k);
			values.add(v);
		});
		if (labels.isEmpty())
		{
			labels.add("Sem dados");
			values.add(1);
		}
		dataSet.setData(values);
		dataSet.setBackgroundColor(List.of("#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6"));
		data.addChartDataSet(dataSet);
		data.setLabels(labels);
		pieStatusModel.setData(data);
	}

	private void createPieGeneroModel(List<StudentEntity> alunos)
	{
		pieGeneroModel = new PieChartModel();
		ChartData data = new ChartData();
		Map<String, Long> porGenero = alunos.stream().collect(
				Collectors.groupingBy(s -> s.getGender() != null && !s.getGender().isBlank() ? s.getGender() : "Não informado", Collectors.counting()));
		PieChartDataSet dataSet = new PieChartDataSet();
		List<Number> values = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		porGenero.forEach((k, v) ->
		{
			labels.add(k);
			values.add(v);
		});
		if (labels.isEmpty())
		{
			labels.add("Sem dados");
			values.add(1);
		}
		dataSet.setData(values);
		dataSet.setBackgroundColor(List.of("#3b82f6", "#ec4899", "#94a3b8"));
		data.addChartDataSet(dataSet);
		data.setLabels(labels);
		pieGeneroModel.setData(data);
	}

	private void createEmptyCharts()
	{
		barModel = new BarChartModel();
		ChartData data = new ChartData();
		BarChartDataSet ds = new BarChartDataSet();
		ds.setLabel("Sem dados");
		ds.setData(List.of(0));
		data.addChartDataSet(ds);
		data.setLabels(List.of("Sem dados"));
		barModel.setData(data);
		pieStatusModel = new PieChartModel();
		pieStatusModel.setData(data);
		pieGeneroModel = new PieChartModel();
		pieGeneroModel.setData(data);
	}

	public int getTotalAlunos()
	{
		return totalAlunos;
	}

	public String getTotalAlunosFormatted()
	{
		if (totalAlunos >= 1000)
			return String.format("%.1fk", totalAlunos / 1000.0).replace(",", ".");
		return String.valueOf(totalAlunos);
	}

	public int getTotalTurmas()
	{
		return totalTurmas;
	}

	public int getTotalProfessores()
	{
		return totalProfessores;
	}

	public double getTaxaPresenca()
	{
		return taxaPresenca;
	}

	public BarChartModel getBarModel()
	{
		return barModel;
	}

	public PieChartModel getPieStatusModel()
	{
		return pieStatusModel;
	}

	public PieChartModel getPieGeneroModel()
	{
		return pieGeneroModel;
	}

	public List<AlunoResumo> getUltimasMatriculas()
	{
		return ultimasMatriculas;
	}

	public List<TurmaResumo> getTurmasResumo()
	{
		return turmasResumo;
	}

	public static class AlunoResumo implements Serializable
	{
		private String codigo, nome, turma, data, estado;

		public AlunoResumo(String c, String n, String t, String d, String e)
		{
			codigo = c;
			nome = n;
			turma = t;
			data = d;
			estado = e;
		}

		public String getCodigo()
		{
			return codigo;
		}

		public String getNome()
		{
			return nome;
		}

		public String getTurma()
		{
			return turma;
		}

		public String getData()
		{
			return data;
		}

		public String getEstado()
		{
			return estado;
		}
	}

	public static class TurmaResumo implements Serializable
	{
		private String nome, professor;
		private int capacidade, ocupados;
		private String situacao;

		public TurmaResumo(String n, String p, int c, int o, String s)
		{
			nome = n;
			professor = p;
			capacidade = c;
			ocupados = o;
			situacao = s;
		}

		public String getNome()
		{
			return nome;
		}

		public String getProfessor()
		{
			return professor;
		}

		public int getCapacidade()
		{
			return capacidade;
		}

		public int getOcupados()
		{
			return ocupados;
		}

		public String getSituacao()
		{
			return situacao;
		}

		public int getPercentOcupacao()
		{
			return capacidade == 0 ? 0 : (int) ((ocupados * 100.0) / capacidade);
		}
	}
}