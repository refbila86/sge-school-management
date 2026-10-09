package mz.co.sge.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mz.co.sge.entity.ClassroomEntity;
import mz.co.sge.repository.IClassroomRepository;
import mz.co.sge.service.IClassroomService;

@Service
@Transactional
public class ClassroomServiceImpl implements IClassroomService
{

	@Autowired
	private IClassroomRepository classroomRepository;

	@Override
	public ClassroomEntity save(ClassroomEntity entity)
	{
		return classroomRepository.save(entity);
	}

	@Override
	public ClassroomEntity findById(Long id)
	{
		return classroomRepository.findById(id);
	}

	@Override
	public List<ClassroomEntity> findAll()
	{
		return classroomRepository.findAll();
	}

	@Override
	public List<ClassroomEntity> findByAcademicYear(Long yearId)
	{
		return classroomRepository.findByAcademicYear(yearId);
	}

	@Override
	public List<ClassroomEntity> findBySchoolAndYear(Long schoolId, Long yearId)
	{
		// Se tens filtro por escola, adiciona no repository depois
		// Por agora filtra só por ano letivo
		return classroomRepository.findByAcademicYear(yearId);
	}

	@Override
	public List<ClassroomEntity> search(String term)
	{
		// Busca simples, podes melhorar com NamedQuery depois
		if (term == null || term.isBlank())
			return findAll();
		String lower = term.toLowerCase();
		return findAll().stream().filter(c -> c.getName().toLowerCase().contains(lower)
				|| (c.getResponsibleTeacher() != null && c.getResponsibleTeacher().getFirstName().toLowerCase().contains(lower))).toList();
	}

	@Override
	public long countByAcademicYear(Long yearId)
	{
		return findByAcademicYear(yearId).size();
	}

	@Override
	public void delete(Long id)
	{
		// implementa se precisares
	}
}