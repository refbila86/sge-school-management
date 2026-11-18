package mz.co.sge.controller;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.entity.EnrollmentEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.service.IEnrollmentService;
import mz.co.sge.service.IStudentService;
import mz.co.sge.service.ITeacherService;

@Named("welcomeBean")
@SessionScoped
public class WelcomeBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private int totalAlunos = 0;
	private int totalTurmas = 0;
	private int totalProfessores = 0;
	private double taxaPresenca = 0.0;
	private long totalPresencasRegistradas = 0;
	private long totalPresencasRegistadas = 0;

	// Graficos como listas simples. O XHTML desenha com Chart.js (<canvas>).
	// Zero dependencia de p:chart / p:barChart / p:doughnutChart.
	private List<String> barLabels = List.of("Sem dados");
	private List<Long> barData = List.of(0L);
	private List<String> statusLabels = List.of("Sem dados");
	private List<Long> statusData = List.of(1L);
	private List<String> generoLabels = List.of("Sem dados");
	private List<Long> generoData = List.of(1L);

	private List<AlunoResumo> ultimasMatriculas = List.of();
	private List<TurmaResumo> turmasResumo = List.of();

	@Autowired
	private LoginBean loginBean;
	@Autowired
	private IStudentService studentService;
	@Autowired
	private IEnrollmentService enrollmentService;
	@Autowired(required = false)
	private ITeacherService teacherService;

	@PostConstruct
	public void init()
	{
		try
		{
			loadRealData();
		} catch (Exception e)
		{
			totalAlunos = 0;
			totalTurmas = 0;
			totalProfessores = 0;
			taxaPresenca = 0;
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

		List<EnrollmentEntity> matriculasTmp;
		if (schoolId != null && yearId != null)
			matriculasTmp = enrollmentService.findBySchoolIdAndAcademicYearId(schoolId, yearId);
		else if (yearId != null)
			matriculasTmp = enrollmentService.findByAcademicYearId(yearId);
		else
			matriculasTmp = enrollmentService.findAll();
		final List<EnrollmentEntity> matriculas = matriculasTmp == null ? List.of() : matriculasTmp;

		this.totalAlunos = matriculas.size();
		this.totalTurmas = (int) matriculas.stream().map(e -> e.getClassroom() != null ? e.getClassroom().getId() : null).filter(Objects::nonNull).distinct()
				.count();
		this.totalProfessores = resolveTotalProfessores();
		resolveTaxaPresenca();

		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		this.ultimasMatriculas = matriculas.stream()
				.sorted(Comparator.comparing(EnrollmentEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())).reversed()).limit(5).map(e ->
				{
					StudentEntity s = e.getStudent();
					String turma = e.getClassroom() != null ? e.getClassroom().getName() : "N/A";
					String estado = e.getStatus() != null ? e.getStatus().name() : "ENROLLED";
					return new AlunoResumo(s != null && s.getCode() != null ? s.getCode() : "S-" + e.getId(), s != null ? s.getFullName() : "Sem nome", turma,
							e.getCreatedAt() != null ? e.getCreatedAt().format(fmt) : "", estado);
				}).collect(Collectors.toList());

		List<StudentEntity> alunosTmp = matriculas.stream().map(EnrollmentEntity::getStudent).filter(Objects::nonNull).collect(Collectors.toList());
		if (alunosTmp.isEmpty() && schoolId != null && yearId != null)
		{
			try
			{
				List<StudentEntity> alt = studentService.findBySchoolIdAndAcademicYearId(schoolId, yearId);
				if (alt != null)
					alunosTmp = alt;
			} catch (Exception ignored)
			{
			}
		}
		final List<StudentEntity> alunos = alunosTmp == null ? List.of() : alunosTmp;

		createBarModelPorClasse(matriculas);
		createPieStatusModel(alunos);
		createPieGeneroModel(alunos);

		this.turmasResumo = matriculas.stream().collect(Collectors.groupingBy(e -> e.getClassroom() != null ? e.getClassroom().getId() : -1L)).entrySet()
				.stream().map(entry ->
				{
					ClassroomEntity c = matriculas.stream().filter(e -> e.getClassroom() != null && Objects.equals(e.getClassroom().getId(), entry.getKey()))
							.map(EnrollmentEntity::getClassroom).findFirst().orElse(null);
					if (c == null)
						return null;
					return new TurmaResumo(c.getName(), "", safeInt(c.getCapacity()), entry.getValue().size(), "Activa");
				}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	// Usa APENAS metodos que existem no ITeacherService que tu colaste:
	// save, findById, findAll, findByAcademicYear, search, delete, findUserById.
	// NAO existe countBySchoolId nem countAll - por isso nao chamo nenhum deles.
	private int resolveTotalProfessores()
	{
		try
		{
			if (teacherService != null)
			{
				List<?> professores = getCurrentAcademicYearId() != null ? teacherService.findByAcademicYear(getCurrentAcademicYearId())
						: teacherService.findAll();
				if (professores != null)
					return professores.size();
			}
		} catch (Exception ignored)
		{
		}
		return 0;
	}

	private void resolveTaxaPresenca()
	{
		// 0 honesto. Nao existe IAttendanceService no teu projeto, por isso nao o
		// chamo.
		this.taxaPresenca = 0;
		this.totalPresencasRegistradas = 0;
		this.totalPresencasRegistadas = 0;
	}

	private int safeInt(Integer v)
	{
		return v == null ? 0 : v;
	}

	// ClassroomEntity NAO tem getClasse() nem getTeacher() - so getClasseNome().
	private String resolveClasseNome(EnrollmentEntity e)
	{
		if (e == null || e.getClassroom() == null)
			return "Sem turma";
		try
		{
			if (e.getClassroom().getClasseNome() != null)
				return String.valueOf(e.getClassroom().getClasseNome());
		} catch (Exception ignored)
		{
		}
		return String.valueOf(e.getClassroom().getName());
	}

	private void createBarModelPorClasse(List<EnrollmentEntity> matriculas)
	{
		Map<String, Long> porClasse = matriculas.stream()
				.collect(Collectors.groupingBy(this::resolveClasseNome, () -> new LinkedHashMap<String, Long>(), Collectors.counting()));
		List<String> labels = new ArrayList<>();
		List<Long> values = new ArrayList<>();
		porClasse.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(en ->
		{
			labels.add(en.getKey());
			values.add(en.getValue());
		});
		if (labels.isEmpty())
		{
			labels.add("Sem dados");
			values.add(0L);
		}
		this.barLabels = labels;
		this.barData = values;
	}

	private void createPieStatusModel(List<StudentEntity> alunos)
	{
		Map<String, Long> porStatus = alunos.stream()
				.collect(Collectors.groupingBy(s -> s.getStatus() != null ? labelEstado(s.getStatus().name()) : "Sem estado", Collectors.counting()));
		List<String> labels = new ArrayList<>();
		List<Long> values = new ArrayList<>();
		porStatus.forEach((k, v) ->
		{
			labels.add(k);
			values.add(v);
		});
		if (labels.isEmpty())
		{
			labels.add("Sem dados");
			values.add(1L);
		}
		this.statusLabels = labels;
		this.statusData = values;
	}

	private void createPieGeneroModel(List<StudentEntity> alunos)
	{
		Map<String, Long> porGenero = alunos.stream().collect(Collectors
				.groupingBy(s -> s.getGender() != null && !s.getGender().isBlank() ? normalizaGenero(s.getGender()) : "Não informado", Collectors.counting()));
		List<String> labels = new ArrayList<>();
		List<Long> values = new ArrayList<>();
		porGenero.forEach((k, v) ->
		{
			labels.add(k);
			values.add(v);
		});
		if (labels.isEmpty())
		{
			labels.add("Sem dados");
			values.add(1L);
		}
		this.generoLabels = labels;
		this.generoData = values;
	}

	private String normalizaGenero(String g)
	{
		String x = g.trim().toUpperCase();
		if (x.startsWith("M") || x.equals("MASCULINO") || x.equals("MALE"))
			return "Masculino";
		if (x.startsWith("F") || x.equals("FEMININO") || x.equals("FEMALE"))
			return "Feminino";
		return g;
	}

	private String labelEstado(String raw)
	{
		if (raw == null)
			return "Sem estado";
		switch (raw.toUpperCase())
		{
		case "ACTIVE":
		case "ENROLLED":
		case "ATIVA":
		case "ACTIVO":
			return "Activa";
		case "INACTIVE":
			return "Inactiva";
		case "CANCELLED":
		case "CANCELED":
			return "Cancelada";
		case "TRANSFERRED":
			return "Transferido";
		case "GRADUATED":
			return "Graduado";
		default:
			return raw;
		}
	}

	private void createEmptyCharts()
	{
		barLabels = List.of("Sem dados");
		barData = List.of(0L);
		statusLabels = List.of("Sem dados");
		statusData = List.of(1L);
		generoLabels = List.of("Sem dados");
		generoData = List.of(1L);
	}

	private String toJsonLabels(List<String> labels)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < labels.size(); i++)
		{
			if (i > 0)
				sb.append(',');
			String v = labels.get(i) == null ? "" : labels.get(i);
			sb.append('"');
			sb.append(v.replace("\\", "\\\\").replace("\"", "\\\""));
			sb.append('"');
		}
		sb.append(']');
		return sb.toString();
	}

	private String toJsonNumbers(List<Long> vals)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < vals.size(); i++)
		{
			if (i > 0)
				sb.append(',');
			sb.append(vals.get(i) == null ? 0 : vals.get(i));
		}
		sb.append(']');
		return sb.toString();
	}

	public String getBarLabelsJson()
	{
		return toJsonLabels(barLabels);
	}

	public String getBarDataJson()
	{
		return toJsonNumbers(barData);
	}

	public String getStatusLabelsJson()
	{
		return toJsonLabels(statusLabels);
	}

	public String getStatusDataJson()
	{
		return toJsonNumbers(statusData);
	}

	public String getGeneroLabelsJson()
	{
		return toJsonLabels(generoLabels);
	}

	public String getGeneroDataJson()
	{
		return toJsonNumbers(generoData);
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

	public long getTotalPresencasRegistadas()
	{
		return totalPresencasRegistadas;
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

		public String getEstadoLabel()
		{
			if (estado == null)
				return "Activa";
			switch (estado.toUpperCase())
			{
			case "ACTIVE":
			case "ENROLLED":
			case "ATIVA":
				return "Activa";
			case "INACTIVE":
				return "Inactiva";
			case "CANCELLED":
			case "CANCELED":
				return "Cancelada";
			default:
				return estado;
			}
		}

		public boolean isActiva()
		{
			return estado != null && (estado.equalsIgnoreCase("ACTIVE") || estado.equalsIgnoreCase("ENROLLED") || estado.equalsIgnoreCase("Ativa"));
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
