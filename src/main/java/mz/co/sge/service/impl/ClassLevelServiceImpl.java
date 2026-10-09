package mz.co.sge.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mz.co.sge.entity.ClassLevelEntity;
import mz.co.sge.repository.IClassLevelRepository;
import mz.co.sge.service.IClassLevelService;

@Service
@Transactional
public class ClassLevelServiceImpl implements IClassLevelService
{

	@Autowired
	private IClassLevelRepository repository;

	@Override
	public ClassLevelEntity save(ClassLevelEntity entity)
	{
		return repository.save(entity);
	}

	@Override
	public ClassLevelEntity findById(Long id)
	{
		return repository.findById(id);
	}

	@Override
	public List<ClassLevelEntity> findAll()
	{
		return repository.findAll();
	}

	@Override
	public List<ClassLevelEntity> findByAcademicYear(Long yearId)
	{
		return repository.findByAcademicYear(yearId);
	}

	@Override
	public void delete(Long id)
	{
		repository.delete(id);
	}

	@Override
	public List<ClassLevelEntity> search(String term, Long yearId)
	{
		List<ClassLevelEntity> base = yearId != null ? findByAcademicYear(yearId) : findAll();
		if (term == null || term.isBlank())
			return base;
		String lower = term.toLowerCase();
		return base.stream().filter(c -> c.getName().toLowerCase().contains(lower)).toList();
	}
}