package mz.co.sge.repository.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.dto.PautaAnualDTO;
import mz.co.sge.repository.IPautaAnualRepository;

@Repository
public class PautaAnualRepositoryImpl implements IPautaAnualRepository
{

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<PautaAnualDTO> findPautaAnual(Long ano, String filtroAluno, String filtroTurma, String filtroDisciplina)
	{
		String sql = """
				SELECT aluno, disciplina, classe, turma, turno, ano,
				  I_ACS1, I_ACS2, I_MACS, I_AP, I_MTrim, I_Estado,
				  II_ACS1, II_ACS2, II_MACS, II_AP, II_MTrim, II_Estado,
				  III_ACS1, III_ACS2, III_MACS, III_AP, III_MTrim, III_Estado,
				  MFinal, Estado_Final
				FROM (
				  SELECT aluno, disciplina, classe, turma, turno, ano,
				    MAX(CASE WHEN trimestre='T1' THEN acs1 END) AS I_ACS1,
				    MAX(CASE WHEN trimestre='T1' THEN acs2 END) AS I_ACS2,
				    MAX(CASE WHEN trimestre='T1' THEN MACS END) AS I_MACS,
				    MAX(CASE WHEN trimestre='T1' THEN ap END) AS I_AP,
				    MAX(CASE WHEN trimestre='T1' THEN MTrim END) AS I_MTrim,
				    CASE WHEN MAX(CASE WHEN trimestre='T1' THEN MTrim END) IS NULL THEN 'Sem Notas'
				         WHEN MAX(CASE WHEN trimestre='T1' THEN MTrim END) >= 10 THEN 'Aprovado'
				         WHEN MAX(CASE WHEN trimestre='T1' THEN MTrim END) >= 8 THEN 'Em Risco'
				         ELSE 'Reprovado' END AS I_Estado,
				    MAX(CASE WHEN trimestre='T2' THEN acs1 END) AS II_ACS1,
				    MAX(CASE WHEN trimestre='T2' THEN acs2 END) AS II_ACS2,
				    MAX(CASE WHEN trimestre='T2' THEN MACS END) AS II_MACS,
				    MAX(CASE WHEN trimestre='T2' THEN ap END) AS II_AP,
				    MAX(CASE WHEN trimestre='T2' THEN MTrim END) AS II_MTrim,
				    CASE WHEN MAX(CASE WHEN trimestre='T2' THEN MTrim END) IS NULL THEN 'Sem Notas'
				         WHEN MAX(CASE WHEN trimestre='T2' THEN MTrim END) >= 10 THEN 'Aprovado'
				         WHEN MAX(CASE WHEN trimestre='T2' THEN MTrim END) >= 8 THEN 'Em Risco'
				         ELSE 'Reprovado' END AS II_Estado,
				    MAX(CASE WHEN trimestre='T3' THEN acs1 END) AS III_ACS1,
				    MAX(CASE WHEN trimestre='T3' THEN acs2 END) AS III_ACS2,
				    MAX(CASE WHEN trimestre='T3' THEN MACS END) AS III_MACS,
				    MAX(CASE WHEN trimestre='T3' THEN ap END) AS III_AP,
				    MAX(CASE WHEN trimestre='T3' THEN MTrim END) AS III_MTrim,
				    CASE WHEN MAX(CASE WHEN trimestre='T3' THEN MTrim END) IS NULL THEN 'Sem Notas'
				         WHEN MAX(CASE WHEN trimestre='T3' THEN MTrim END) >= 10 THEN 'Aprovado'
				         WHEN MAX(CASE WHEN trimestre='T3' THEN MTrim END) >= 8 THEN 'Em Risco'
				         ELSE 'Reprovado' END AS III_Estado,
				    ROUND((COALESCE(MAX(CASE WHEN trimestre='T1' THEN MTrim END),0) +
				           COALESCE(MAX(CASE WHEN trimestre='T2' THEN MTrim END),0) +
				           COALESCE(MAX(CASE WHEN trimestre='T3' THEN MTrim END),0)) / 3, 2) AS MFinal,
				    CASE WHEN MAX(CASE WHEN trimestre='T1' THEN MTrim END) IS NULL
				          AND MAX(CASE WHEN trimestre='T2' THEN MTrim END) IS NULL
				          AND MAX(CASE WHEN trimestre='T3' THEN MTrim END) IS NULL THEN 'Sem Notas'
				         WHEN (COALESCE(MAX(CASE WHEN trimestre='T1' THEN MTrim END),0) +
				              COALESCE(MAX(CASE WHEN trimestre='T2' THEN MTrim END),0) +
				              COALESCE(MAX(CASE WHEN trimestre='T3' THEN MTrim END),0)) / 3 >= 10 THEN 'Aprovado'
				         WHEN (COALESCE(MAX(CASE WHEN trimestre='T1' THEN MTrim END),0) +
				              COALESCE(MAX(CASE WHEN trimestre='T2' THEN MTrim END),0) +
				              COALESCE(MAX(CASE WHEN trimestre='T3' THEN MTrim END),0)) / 3 >= 8 THEN 'Em Risco'
				         ELSE 'Reprovado' END AS Estado_Final
				  FROM (
				    SELECT g.student_id, CONCAT(s.first_name,' ',s.last_name) AS aluno,
				      sub.name AS disciplina, cl.name AS classe, c.name AS turma, c.shift AS turno, ay.`year` AS ano,
				      g.trimester AS trimestre, g.acs1, g.acs2, g.ap,
				      (g.acs1+g.acs2)/2 AS MACS, (((g.acs1+g.acs2)/2)+g.ap)/2 AS MTrim
				    FROM sge.grade g
				    INNER JOIN sge.academic_year ay ON ay.id=g.academic_year_id
				    INNER JOIN sge.students s ON s.id=g.student_id
				    INNER JOIN sge.subjects sub ON sub.id=g.subject_id
				    INNER JOIN sge.classrooms c ON c.id=g.classroom_id
				    INNER JOIN sge.class_levels cl ON cl.id=c.class_level_id
				    WHERE ay.`year` = :ano
				  ) AS notas
				  GROUP BY student_id, aluno, disciplina, classe, turma, turno, ano
				) t WHERE 1=1
				""";

		if (has(filtroAluno))
			sql += " AND aluno LIKE :aluno ";
		if (has(filtroTurma))
			sql += " AND turma LIKE :turma ";
		if (has(filtroDisciplina))
			sql += " AND disciplina LIKE :disc ";
		sql += " ORDER BY classe, turma, aluno, disciplina ";

		var q = em.createNativeQuery(sql);
		// ay.`year` na BD é VARCHAR, por isso passa como String
		q.setParameter("ano", ano == null ? null : String.valueOf(ano));
		if (has(filtroAluno))
			q.setParameter("aluno", "%" + filtroAluno + "%");
		if (has(filtroTurma))
			q.setParameter("turma", "%" + filtroTurma + "%");
		if (has(filtroDisciplina))
			q.setParameter("disc", "%" + filtroDisciplina + "%");

		List<Object[]> rows = q.getResultList();
		List<PautaAnualDTO> lista = new ArrayList<>();
		for (Object[] r : rows)
		{
			PautaAnualDTO d = new PautaAnualDTO();
			int i = 0;
			d.setAluno(toStr(r[i++]));
			d.setDisciplina(toStr(r[i++]));
			d.setClasse(toStr(r[i++]));
			d.setTurma(toStr(r[i++]));
			d.setTurno(toStr(r[i++]));
			d.setAno(toLong(r[i++]));
			d.setIAcs1(num(r[i++]));
			d.setIAcs2(num(r[i++]));
			d.setIMacs(num(r[i++]));
			d.setIAp(num(r[i++]));
			d.setIMTrim(num(r[i++]));
			d.setIEstado(toStr(r[i++]));
			d.setIiAcs1(num(r[i++]));
			d.setIiAcs2(num(r[i++]));
			d.setIiMacs(num(r[i++]));
			d.setIiAp(num(r[i++]));
			d.setIiMTrim(num(r[i++]));
			d.setIiEstado(toStr(r[i++]));
			d.setIiiAcs1(num(r[i++]));
			d.setIiiAcs2(num(r[i++]));
			d.setIiiMacs(num(r[i++]));
			d.setIiiAp(num(r[i++]));
			d.setIiiMTrim(num(r[i++]));
			d.setIiiEstado(toStr(r[i++]));
			d.setMFinal(num(r[i++]));
			d.setEstadoFinal(toStr(r[i++]));
			lista.add(d);
		}
		return lista;
	}

	private boolean has(String v)
	{
		return v != null && !v.isBlank();
	}

	private String toStr(Object o)
	{
		if (o == null)
			return null;
		return String.valueOf(o);
	}

	private Long toLong(Object o)
	{
		if (o == null)
			return null;
		if (o instanceof Number)
			return ((Number) o).longValue();
		if (o instanceof String)
		{
			try
			{
				return Long.valueOf(((String) o).trim());
			} catch (Exception e)
			{
				return null;
			}
		}
		return null;
	}

	private Double num(Object o)
	{
		if (o == null)
			return null;
		if (o instanceof Number)
			return ((Number) o).doubleValue();
		if (o instanceof String)
		{
			try
			{
				return Double.valueOf(((String) o).trim());
			} catch (Exception e)
			{
				return null;
			}
		}
		return null;
	}
}