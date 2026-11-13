package mz.co.sge.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import mz.co.sge.entity.GradeEntity;
import mz.co.sge.enumes.Trimester;
import mz.co.sge.repository.IGradeRepository;
import mz.co.sge.service.IGradeService;

@Service
@Transactional
public class GradeServiceImpl implements IGradeService
{

	@Autowired
	private IGradeRepository repository;

	@Override
	public List<GradeEntity> findByFilters(Long classroomId, Long subjectId, Long yearId, Trimester trimester)
	{
		return repository.findByFilters(classroomId, subjectId, yearId, trimester);
	}

	@Override
	public List<GradeEntity> findByStudent(Long studentId, Long subjectId, Long yearId)
	{
		return repository.findByStudentAndSubject(studentId, subjectId, yearId);
	}

	@Override
	public void save(GradeEntity entity)
	{
		repository.save(entity);
	}

	@Override
	public void saveAll(List<GradeEntity> entities)
	{
		repository.saveAll(entities);
	}

	@Override
	public double calcularMediaAnual(Long studentId, Long subjectId, Long yearId)
	{
		List<GradeEntity> notas = repository.findByStudentAndSubject(studentId, subjectId, yearId);
		if (notas.isEmpty())
			return 0;
		return notas.stream().mapToDouble(GradeEntity::getMediaFinal).average().orElse(0);
	}
}