package mz.co.sge.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mz.co.sge.entity.TeacherEntity;
import mz.co.sge.repository.ITeacherRepository;
import mz.co.sge.service.ITeacherService;

@Service
@Transactional
public class TeacherServiceImpl implements ITeacherService
{

	@Autowired
	private ITeacherRepository repository;

	@Override
	public TeacherEntity save(TeacherEntity entity)
	{
		return repository.save(entity);
	}

	@Override
	public TeacherEntity findById(Long id)
	{
		return repository.findById(id);
	}

	@Override
	public List<TeacherEntity> findAll()
	{
		return repository.findAll();
	}

	@Override
	public List<TeacherEntity> findByAcademicYear(Long yearId)
	{
		return repository.findByAcademicYear(yearId);
	}

	@Override
	public List<TeacherEntity> search(String term, Long yearId)
	{
		return repository.search(term, yearId);
	}

	@Override
	public void delete(Long id)
	{
		repository.delete(id);
	}
}