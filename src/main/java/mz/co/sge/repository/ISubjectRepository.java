package mz.co.sge.repository;

import mz.co.sge.entity.SubjectEntity;
import java.util.List;

public interface ISubjectRepository
{
	List<SubjectEntity> findAll();

	List<SubjectEntity> findByAcademicYearId(Long yearId);

	SubjectEntity findByCode(String code);

	List<SubjectEntity> findByName(String name);

	SubjectEntity findById(Long id);

	SubjectEntity save(SubjectEntity entity);

	void delete(Long id);

	boolean existsByCode(String code);
	
    List<SubjectEntity> findAllActive();
}