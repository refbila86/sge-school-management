package mz.co.sge.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.repository.IClassroomRepository;
import mz.co.sge.service.IClassroomService;

@Service
public class ClassroomServiceImpl implements IClassroomService
{
	@Autowired
	private IClassroomRepository repository;

	@Override
	@Transactional
	public ClassroomEntity save(ClassroomEntity entity)
	{
		return repository.save(entity);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public ClassroomEntity findById(Long id)
	{
		try
		{
			return repository.findById(id);
		} catch (Exception e)
		{
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public List<ClassroomEntity> findAll()
	{
		try
		{
			return repository.findAll();
		} catch (Exception e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public List<ClassroomEntity> findByAcademicYear(Long yearId)
	{
		try
		{
			return repository.findByAcademicYear(yearId);
		} catch (Exception e)
		{
			return findAll();
		}
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public List<ClassroomEntity> findBySchoolAndYear(Long schoolId, Long yearId)
	{
		// Se não filtra por escola, filtra só por ano
		return findByAcademicYear(yearId);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public List<ClassroomEntity> search(String term)
	{
		try
		{
			return repository.search(term, null);
		} catch (Exception e)
		{
			return new ArrayList<>();
		}
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public List<ClassroomEntity> search(String term, Long yearId)
	{
		try
		{
			return repository.search(term, yearId);
		} catch (Exception e)
		{
			return new ArrayList<>();
		}
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public long countByAcademicYear(Long yearId)
	{
		try
		{
			return repository.countByAcademicYear(yearId);
		} catch (Exception e)
		{
			return 0;
		}
	}

	@Override
	@Transactional
	public void delete(Long id)
	{
		repository.delete(id);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public List<ClassroomEntity> getAllClassrooms()
	{
		return findAll();
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public List<ClassroomEntity> findAllActive()
	{
		try
		{
			return repository.findAllActive();
		} catch (Exception e)
		{
			return findAll();
		}
	}

	@Override
	@Transactional
	public void incrementStudents(Long classroomId)
	{
		repository.incrementStudents(classroomId);
	}

	@Override
	@Transactional
	public void decrementStudents(Long classroomId)
	{
		repository.decrementStudents(classroomId);
	}
}