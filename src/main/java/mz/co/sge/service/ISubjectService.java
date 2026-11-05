package mz.co.sge.service;

import mz.co.sge.entity.SubjectEntity;
import java.util.List;

public interface ISubjectService
{
	List<SubjectEntity> findAll();

	List<SubjectEntity> findByAcademicYearId(Long yearId);

	SubjectEntity findByCode(String code);

	List<SubjectEntity> findByName(String name);

	SubjectEntity findById(Long id);

	SubjectEntity save(SubjectEntity entity);

	void delete(Long id);

	String generateNextCode();

	List<SubjectEntity> findAllActive();

	List<SubjectEntity> getAllSubjects();

}