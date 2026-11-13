package mz.co.sge.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import mz.co.sge.entity.AttendanceEntity;
import mz.co.sge.enumes.Trimester;

@Repository
public interface IAttendanceRepository
{
	List<AttendanceEntity> findByDateAndSubject(LocalDate date, Long subjectId, Long classroomId, Trimester trimester);

	List<AttendanceEntity> findByFilters(Long classroomId, Long subjectId, Long yearId, Trimester trimester);

	long countFaltasByStudentAndTrimester(Long studentId, Long subjectId, Trimester trimester);

	List<AttendanceEntity> findFaltasByStudentAndTrimester(Long studentId, Long subjectId, Trimester trimester);

	void save(AttendanceEntity entity);

	void saveAll(List<AttendanceEntity> entities);
}