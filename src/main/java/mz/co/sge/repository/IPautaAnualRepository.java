package mz.co.sge.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import mz.co.sge.dto.PautaAnualDTO;

@Repository
public interface IPautaAnualRepository
{
 
		List<PautaAnualDTO> findPautaAnual(Long ano, String filtroAluno, String filtroTurma, String filtroDisciplina);
 

}
