package mz.co.sge.repository;

import java.util.List;

import mz.co.sge.entity.SubjectEntity;

public interface ISubjectRepository
{
	SubjectEntity save(SubjectEntity entity);

	SubjectEntity findById(Long id);

	List<SubjectEntity> findAll();

	List<SubjectEntity> findByAcademicYear(Long yearId);

	SubjectEntity findByCode(String code);

}
