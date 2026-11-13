
SELECT
    aluno,
    disciplina,
    classe,
    turma,
    turno,
    ano,

    -- =================================
    -- I TRIMESTRE
    -- =================================
    MAX(CASE WHEN trimestre = 'T1' THEN acs1 END) AS I_ACS1,
    MAX(CASE WHEN trimestre = 'T1' THEN acs2 END) AS I_ACS2,
    MAX(CASE WHEN trimestre = 'T1' THEN MACS END) AS I_MACS,
    MAX(CASE WHEN trimestre = 'T1' THEN ap END) AS I_AP,
    MAX(CASE WHEN trimestre = 'T1' THEN MTrim END) AS I_MTrim,

    CASE
        WHEN MAX(CASE WHEN trimestre = 'T1' THEN MTrim END) IS NULL
            THEN 'Sem Notas'
        WHEN MAX(CASE WHEN trimestre = 'T1' THEN MTrim END) >= 10
            THEN 'Aprovado'
        WHEN MAX(CASE WHEN trimestre = 'T1' THEN MTrim END) >= 8
            THEN 'Em Risco'
        ELSE 'Reprovado'
    END AS I_Estado,

    -- =================================
    -- II TRIMESTRE
    -- =================================
    MAX(CASE WHEN trimestre = 'T2' THEN acs1 END) AS II_ACS1,
    MAX(CASE WHEN trimestre = 'T2' THEN acs2 END) AS II_ACS2,
    MAX(CASE WHEN trimestre = 'T2' THEN MACS END) AS II_MACS,
    MAX(CASE WHEN trimestre = 'T2' THEN ap END) AS II_AP,
    MAX(CASE WHEN trimestre = 'T2' THEN MTrim END) AS II_MTrim,

    CASE
        WHEN MAX(CASE WHEN trimestre = 'T2' THEN MTrim END) IS NULL
            THEN 'Sem Notas'
        WHEN MAX(CASE WHEN trimestre = 'T2' THEN MTrim END) >= 10
            THEN 'Aprovado'
        WHEN MAX(CASE WHEN trimestre = 'T2' THEN MTrim END) >= 8
            THEN 'Em Risco'
        ELSE 'Reprovado'
    END AS II_Estado,

    -- =================================
    -- III TRIMESTRE
    -- =================================
    MAX(CASE WHEN trimestre = 'T3' THEN acs1 END) AS III_ACS1,
    MAX(CASE WHEN trimestre = 'T3' THEN acs2 END) AS III_ACS2,
    MAX(CASE WHEN trimestre = 'T3' THEN MACS END) AS III_MACS,
    MAX(CASE WHEN trimestre = 'T3' THEN ap END) AS III_AP,
    MAX(CASE WHEN trimestre = 'T3' THEN MTrim END) AS III_MTrim,

    CASE
        WHEN MAX(CASE WHEN trimestre = 'T3' THEN MTrim END) IS NULL
            THEN 'Sem Notas'
        WHEN MAX(CASE WHEN trimestre = 'T3' THEN MTrim END) >= 10
            THEN 'Aprovado'
        WHEN MAX(CASE WHEN trimestre = 'T3' THEN MTrim END) >= 8
            THEN 'Em Risco'
        ELSE 'Reprovado'
    END AS III_Estado,

    -- =================================
    -- MÉDIA FINAL
    -- (MTrim I + MTrim II + MTrim III) / 3
    -- =================================
    ROUND(
        (
            COALESCE(MAX(CASE WHEN trimestre = 'T1' THEN MTrim END), 0) +
            COALESCE(MAX(CASE WHEN trimestre = 'T2' THEN MTrim END), 0) +
            COALESCE(MAX(CASE WHEN trimestre = 'T3' THEN MTrim END), 0)
        ) / 3,
        2
    ) AS MFinal,

    -- =================================
    -- ESTADO FINAL
    -- =================================
    CASE
        WHEN MAX(CASE WHEN trimestre = 'T1' THEN MTrim END) IS NULL
         AND MAX(CASE WHEN trimestre = 'T2' THEN MTrim END) IS NULL
         AND MAX(CASE WHEN trimestre = 'T3' THEN MTrim END) IS NULL
            THEN 'Sem Notas'

        WHEN (
            COALESCE(MAX(CASE WHEN trimestre = 'T1' THEN MTrim END), 0) +
            COALESCE(MAX(CASE WHEN trimestre = 'T2' THEN MTrim END), 0) +
            COALESCE(MAX(CASE WHEN trimestre = 'T3' THEN MTrim END), 0)
        ) / 3 >= 10
            THEN 'Aprovado'

        WHEN (
            COALESCE(MAX(CASE WHEN trimestre = 'T1' THEN MTrim END), 0) +
            COALESCE(MAX(CASE WHEN trimestre = 'T2' THEN MTrim END), 0) +
            COALESCE(MAX(CASE WHEN trimestre = 'T3' THEN MTrim END), 0)
        ) / 3 >= 8
            THEN 'Em Risco'

        ELSE 'Reprovado'
    END AS Estado_Final

FROM (
    SELECT
        g.student_id,
        CONCAT(s.first_name, ' ', s.last_name) AS aluno,
        sub.name AS disciplina,
        cl.name AS classe,
        c.name AS turma,
        c.shift AS turno,
        ay.`year` AS ano,
        g.trimester AS trimestre,
        g.acs1,
        g.acs2,
        g.ap,

        -- MÉDIA DAS ACS
        (g.acs1 + g.acs2) / 2 AS MACS,

        -- MÉDIA DO TRIMESTRE
        (((g.acs1 + g.acs2) / 2) + g.ap) / 2 AS MTrim

    FROM sge.grade g

    INNER JOIN sge.academic_year ay
        ON ay.id = g.academic_year_id

    INNER JOIN sge.students s
        ON s.id = g.student_id

    INNER JOIN sge.subjects sub
        ON sub.id = g.subject_id

    INNER JOIN sge.classrooms c
        ON c.id = g.classroom_id

    INNER JOIN sge.class_levels cl
        ON cl.id = c.class_level_id

    WHERE cl.name = '1ª Classe'
      AND c.shift = 'MORNING'
      AND c.name = 'Turma A'
      AND sub.name = 'Matematica'
      AND ay.`year` = 2026

) AS notas

GROUP BY
    student_id,
    aluno,
    disciplina,
    classe,
    turma,
    turno,
    ano

ORDER BY aluno;
