package mz.co.sge.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import mz.co.sge.entity.AttendanceEntity;
import mz.co.sge.enumes.Trimester;

@Service
public interface IAttendanceService
{
	List<AttendanceEntity> findByDateAndSubject(LocalDate date, Long subjectId, Long classroomId, Trimester trimester);

	long countFaltas(Long studentId, Long subjectId, Trimester trimester);

	void saveAll(List<AttendanceEntity> list);

	List<AttendanceEntity> findByFilters(Long classroomId, Long subjectId, Long yearId, Trimester trimester);
}