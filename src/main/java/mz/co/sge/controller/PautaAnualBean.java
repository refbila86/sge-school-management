package mz.co.sge.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import mz.co.sge.dto.PautaAnualDTO;
import mz.co.sge.repository.IPautaAnualRepository;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("pautaAnualBean")
@ViewScoped
public class PautaAnualBean implements Serializable
{

	@Inject
	private IPautaAnualRepository pautaAnualRepository;

	@Inject
	private LoginBean loginBean;

	private List<PautaAnualDTO> pauta = new ArrayList<>();
	private Long ano;
	private String filtroAluno;
	private String filtroTurma;
	private String filtroDisciplina;

	@PostConstruct
	public void init()
	{
		this.ano = loginBean.getAcademicYearId();
		carregar();
	}

	public void carregar()
	{
		pauta = pautaAnualRepository.findPautaAnual(ano, filtroAluno, filtroTurma, filtroDisciplina);
	}

	public void limpar()
	{
		filtroAluno = null;
		filtroTurma = null;
		filtroDisciplina = null;
		carregar();
	}

	// É ESTE GETTER QUE FALTAVA - é ele que a tela usa em #{pautaAnualBean.pauta}
	public List<PautaAnualDTO> getPauta()
	{
		return pauta;
	}

	public void setPauta(List<PautaAnualDTO> pauta)
	{
		this.pauta = pauta;
	}

	public Long getAno()
	{
		return ano;
	}

	public void setAno(Long ano)
	{
		this.ano = ano;
	}

	public String getFiltroAluno()
	{
		return filtroAluno;
	}

	public void setFiltroAluno(String v)
	{
		this.filtroAluno = v;
	}

	public String getFiltroTurma()
	{
		return filtroTurma;
	}

	public void setFiltroTurma(String v)
	{
		this.filtroTurma = v;
	}

	public String getFiltroDisciplina()
	{
		return filtroDisciplina;
	}

	public void setFiltroDisciplina(String v)
	{
		this.filtroDisciplina = v;
	}
}