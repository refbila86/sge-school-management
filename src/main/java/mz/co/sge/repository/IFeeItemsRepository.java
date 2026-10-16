package mz.co.sge.repository;

import java.util.List;
import java.util.Optional;
import mz.co.sge.entity.FeeItemEntity;

public interface IFeeItemsRepository
{
	FeeItemEntity save(FeeItemEntity entity);

	FeeItemEntity update(FeeItemEntity entity);

	void delete(Long id);

	Optional<FeeItemEntity> findById(Long id);

	List<FeeItemEntity> findAll();

	List<FeeItemEntity> findBySchoolId(Long schoolId);

	List<FeeItemEntity> findBySchoolAndYearId(Long schoolId, Long yearId);

	List<FeeItemEntity> findActiveBySchoolAndYearId(Long schoolId, Long yearId);

	List<FeeItemEntity> findByDescricao(String term, Long schoolId, Long yearId);

	long countBySchoolId(Long schoolId);
}