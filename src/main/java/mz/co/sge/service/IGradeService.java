package mz.co.sge.service;

import java.util.List;

import org.springframework.stereotype.Service;

import mz.co.sge.entity.GradeEntity;
import mz.co.sge.enumes.Trimester;

@Service
public interface IGradeService
{
	List<GradeEntity> findByFilters(Long classroomId, Long subjectId, Long yearId, Trimester trimester);

	List<GradeEntity> findByStudent(Long studentId, Long subjectId, Long yearId);

	void save(GradeEntity entity);

	void saveAll(List<GradeEntity> entities);

	double calcularMediaAnual(Long studentId, Long subjectId, Long yearId);
}