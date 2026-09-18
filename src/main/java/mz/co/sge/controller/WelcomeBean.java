package mz.co.sge.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.annotation.PostConstruct;

@Component("welcomeBean")
@RequestScope
public class WelcomeBean implements Serializable
{

	private static final long serialVersionUID = 1L;

	private int totalAlunos = 1248;
	private int totalTurmas = 32;
	private int totalProfessores = 48;
	private double taxaPresenca = 92.5;

	private List<AlunoResumo> ultimasMatriculas;
	private List<TurmaResumo> turmasResumo;

	@PostConstruct
	public void init()
	{
		ultimasMatriculas = new ArrayList<>();
		ultimasMatriculas.add(new AlunoResumo("A2026-001", "Ana Paula Massingue", "10ª A", "12/08/2026", "Ativa"));
		ultimasMatriculas.add(new AlunoResumo("A2026-002", "João Carlos Mabjaia", "9ª B", "11/08/2026", "Ativa"));
		ultimasMatriculas.add(new AlunoResumo("A2026-003", "Fatima Issa", "8ª C", "10/08/2026", "Pendente"));
		ultimasMatriculas.add(new AlunoResumo("A2026-004", "Carlos Tembe", "11ª A", "09/08/2026", "Ativa"));
		ultimasMatriculas.add(new AlunoResumo("A2026-005", "Sonia Muianga", "10ª B", "08/08/2026", "Ativa"));

		turmasResumo = new ArrayList<>();
		turmasResumo.add(new TurmaResumo("10ª Classe A - Ciências", "Prof. Manuel", 42, 38, "Normal"));
		turmasResumo.add(new TurmaResumo("9ª Classe B - Geral", "Profª. Luisa", 38, 35, "Normal"));
		turmasResumo.add(new TurmaResumo("11ª Classe A - Letras", "Prof. Alberto", 35, 33, "Atenção"));
		turmasResumo.add(new TurmaResumo("8ª Classe C - Geral", "Profª. Rosa", 45, 44, "Lotada"));
	}

	public int getTotalAlunos()
	{
		return totalAlunos;
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
			return (int) ((ocupados * 100.0) / capacidade);
		}
	}
}