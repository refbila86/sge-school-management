package mz.co.sge.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mz.co.sge.entity.FeeItemEntity;
import mz.co.sge.repository.IFeeItemsRepository;
import mz.co.sge.service.IFeeItemsService;

@Service
public class FeeItemsServiceImpl implements IFeeItemsService
{

	@Autowired
	private IFeeItemsRepository repository;

	@Override
	@Transactional
	public FeeItemEntity create(FeeItemEntity entity)
	{
		return repository.save(entity);
	}

	@Override
	@Transactional
	public FeeItemEntity update(FeeItemEntity entity)
	{
		return repository.update(entity);
	}

	@Override
	@Transactional
	public void delete(Long id)
	{
		repository.delete(id);
	}

	@Override
	public FeeItemEntity getById(Long id)
	{
		return repository.findById(id).orElse(null);
	}

	@Override
	public List<FeeItemEntity> getAll()
	{
		return repository.findAll();
	}

	@Override
	public List<FeeItemEntity> getBySchoolId(Long schoolId)
	{
		return repository.findBySchoolId(schoolId);
	}

	@Override
	public List<FeeItemEntity> getBySchoolAndYearId(Long schoolId, Long yearId)
	{
		return repository.findBySchoolAndYearId(schoolId, yearId);
	}

	@Override
	public List<FeeItemEntity> getActiveBySchoolAndYearId(Long schoolId, Long yearId)
	{
		return repository.findActiveBySchoolAndYearId(schoolId, yearId);
	}

	@Override
	public long countBySchoolId(Long schoolId)
	{
		return repository.countBySchoolId(schoolId);
	}
}