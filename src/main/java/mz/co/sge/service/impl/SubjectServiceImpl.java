package mz.co.sge.service.impl;

import mz.co.sge.entity.SubjectEntity;
import mz.co.sge.repository.ISubjectRepository;
import mz.co.sge.service.ISubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class SubjectServiceImpl implements ISubjectService
{

	@Autowired
	private ISubjectRepository repository;

	@Override
	public List<SubjectEntity> findAll()
	{
		return repository.findAll();
	}

	@Override
	public List<SubjectEntity> findByAcademicYearId(Long yearId)
	{
		return repository.findByAcademicYearId(yearId);
	}

	@Override
	public SubjectEntity findByCode(String code)
	{
		return repository.findByCode(code);
	}

	@Override
	public List<SubjectEntity> findByName(String name)
	{
		return repository.findByName(name);
	}

	@Override
	public SubjectEntity findById(Long id)
	{
		return repository.findById(id);
	}

	@Override
	public void delete(Long id)
	{
		repository.delete(id);
	}

	@Override
	public SubjectEntity save(SubjectEntity entity)
	{
		if (entity.getCode() == null || entity.getCode().isEmpty())
		{
			entity.setCode(generateNextCode());
		}
		return repository.save(entity);
	}

	@Override
	public String generateNextCode()
	{
		List<SubjectEntity> all = repository.findAll();
		int next = all.size() + 1;
		return String.format("SUB-%03d", next); // SUB-001, SUB-002
	}

	@Override
	public List<SubjectEntity> findAllActive()
	{
		return repository.findAllActive();
	}

	@Override
	public List<SubjectEntity> getAllSubjects()
	{
		return repository.findAll();
	}
}