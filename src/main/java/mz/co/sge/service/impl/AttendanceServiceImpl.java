package mz.co.sge.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import mz.co.sge.entity.AttendanceEntity;
import mz.co.sge.enumes.Trimester;
import mz.co.sge.repository.IAttendanceRepository;
import mz.co.sge.service.IAttendanceService;

@Service
@Transactional
public class AttendanceServiceImpl implements IAttendanceService
{

	@Autowired
	private IAttendanceRepository repository;

	@Override
	public List<AttendanceEntity> findByDateAndSubject(LocalDate date, Long subjectId, Long classroomId, Trimester trimester)
	{
		return repository.findByDateAndSubject(date, subjectId, classroomId, trimester);
	}

	@Override
	public long countFaltas(Long studentId, Long subjectId, Trimester trimester)
	{
		return repository.countFaltasByStudentAndTrimester(studentId, subjectId, trimester);
	}

	@Override
	public void saveAll(List<AttendanceEntity> list)
	{
		repository.saveAll(list);
	}

	@Override
	public List<AttendanceEntity> findByFilters(Long classroomId, Long subjectId, Long yearId, Trimester trimester)
	{
		return repository.findByFilters(classroomId, subjectId, yearId, trimester);
	}
}