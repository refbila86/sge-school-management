package mz.co.sge.service;

import java.util.List;
import mz.co.sge.entity.FeeItemEntity;

public interface IFeeItemsService
{
	FeeItemEntity create(FeeItemEntity entity);

	FeeItemEntity update(FeeItemEntity entity);

	void delete(Long id);

	FeeItemEntity getById(Long id);

	List<FeeItemEntity> getAll();

	List<FeeItemEntity> getBySchoolId(Long schoolId);

	List<FeeItemEntity> getBySchoolAndYearId(Long schoolId, Long yearId);

	List<FeeItemEntity> getActiveBySchoolAndYearId(Long schoolId, Long yearId);

	long countBySchoolId(Long schoolId);
}