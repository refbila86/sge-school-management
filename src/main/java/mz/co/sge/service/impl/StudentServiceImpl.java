package mz.co.sge.service.impl;

import mz.co.sge.entity.GuardianEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.StudentStatus;
import mz.co.sge.repository.IGuardianRepository;
import mz.co.sge.repository.IStudentRepository;
import mz.co.sge.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements IStudentService
{

	@Autowired
	private IStudentRepository studentRepository;

	@Autowired
	private IGuardianRepository guardianRepository;

	public StudentServiceImpl()
	{
	}

	public StudentServiceImpl(IStudentRepository studentRepository, IGuardianRepository guardianRepository)
	{
		this.studentRepository = studentRepository;
		this.guardianRepository = guardianRepository;
	}

	@Override
	@Transactional
	public StudentEntity createStudent(StudentEntity student)
	{
		if (student.getGuardian() == null || student.getGuardian().getId() == null)
			throw new IllegalArgumentException("Guardian é obrigatório no cadastro do estudante.");
		GuardianEntity guardian = guardianRepository.findById(student.getGuardian().getId())
				.orElseThrow(() -> new IllegalArgumentException("Guardian não encontrado: " + student.getGuardian().getId()));
		student.setGuardian(guardian);
		if (student.getCode() == null || student.getCode().isBlank())
			student.setCode("STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		return studentRepository.save(student);
	}

	@Override
	@Transactional
	public StudentEntity registerStudent(StudentEntity student, Long guardianId)
	{
		if (guardianId == null)
			throw new IllegalArgumentException("Deve indicar o guardian (obrigatório).");
		GuardianEntity guardian = guardianRepository.findById(guardianId)
				.orElseThrow(() -> new IllegalArgumentException("Guardian não encontrado: " + guardianId));
		student.setGuardian(guardian);
		if (student.getCode() == null || student.getCode().isBlank())
			student.setCode("STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		return studentRepository.save(student);
	}

	@Override
	@Transactional
	public StudentEntity updateStudent(StudentEntity student)
	{
		if (student.getId() == null)
			throw new IllegalArgumentException("ID do estudante é obrigatório.");
		StudentEntity existing = studentRepository.findById(student.getId()).orElseThrow(() -> new IllegalArgumentException("Estudante não encontrado."));
		if (student.getGuardian() != null && student.getGuardian().getId() != null)
		{
			GuardianEntity guardian = guardianRepository.findById(student.getGuardian().getId())
					.orElseThrow(() -> new IllegalArgumentException("Guardian não encontrado."));
			existing.setGuardian(guardian);
		}
		existing.setFirstName(student.getFirstName());
		existing.setLastName(student.getLastName());
		existing.setBirthDate(student.getBirthDate());
		existing.setGender(student.getGender());
		existing.setPhone(student.getPhone());
		existing.setEmail(student.getEmail());
		existing.setAddress(student.getAddress());
		existing.setStatus(student.getStatus());
		existing.setAcademicYear(student.getAcademicYear());
		existing.setSchool(student.getSchool());
		return studentRepository.update(existing);
	}

	@Override
	@Transactional
	public void deleteStudent(Long id)
	{
		studentRepository.delete(id);
	}

	@Override
	public Optional<StudentEntity> getStudentById(Long id)
	{
		return studentRepository.findById(id);
	}

	@Override
	public Optional<StudentEntity> getStudentByCode(String code)
	{
		return studentRepository.findByCode(code);
	}

	@Override
	public List<StudentEntity> getAllStudents()
	{
		return studentRepository.findAll();
	}

	@Override
	public List<StudentEntity> getStudentsByGuardianId(Long guardianId)
	{
		return studentRepository.findByGuardianId(guardianId);
	}

	@Override
	public List<StudentEntity> getStudentsByAcademicYearId(Long academicYearId)
	{
		return studentRepository.findByAcademicYearId(academicYearId);
	}

	@Override
	public List<StudentEntity> getStudentsByStatus(StudentStatus status)
	{
		return studentRepository.findByStatus(status);
	}

	@Override
	public List<StudentEntity> findByName(String name)
	{
		if (name == null || name.isBlank())
			return getAllStudents();
		String term = name.toLowerCase();
		return studentRepository.findAll().stream().filter(s -> s.getFullName().toLowerCase().contains(term) || s.getCode().toLowerCase().contains(term))
				.collect(Collectors.toList());
	}

	@Override
	public long countStudents()
	{
		return studentRepository.countAll();
	}

	@Override
	public long countByGuardianId(Long guardianId)
	{
		return studentRepository.countByGuardianId(guardianId);
	}

	@Override
	public List<StudentEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId)
	{
		return studentRepository.findBySchoolIdAndAcademicYearId(schoolId, academicYearId);
	}

	@Override
	public List<StudentEntity> findBySchoolId(Long schoolId)
	{
		return studentRepository.findBySchoolId(schoolId);
	}

	@Override
	public List<StudentEntity> getStudentsBySchoolId(Long schoolId)
	{
		return studentRepository.findBySchoolId(schoolId);
	}

	@Override
	public List<StudentEntity> getStudentsBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId)
	{
		return studentRepository.findBySchoolIdAndAcademicYearId(schoolId, academicYearId);
	}

	@Override
	public long countBySchoolId(Long schoolId)
	{
		return studentRepository.countBySchoolId(schoolId);
	}

	@Override
	public long countBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId)
	{
		return studentRepository.countBySchoolIdAndAcademicYearId(schoolId, academicYearId);
	}

	@Override
	public long countByAcademicYearId(Long academicYearId)
	{
		return studentRepository.countByAcademicYearId(academicYearId);
	}
}
