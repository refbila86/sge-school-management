package mz.co.sge.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.GuardianEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.StudentStatus;
import mz.co.sge.repository.IGuardianRepository;
import mz.co.sge.repository.IStudentRepository;
import mz.co.sge.service.IStudentService;

@Service
public class StudentServiceImpl implements IStudentService
{

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private IStudentRepository studentRepository;

	@Autowired
	private IGuardianRepository guardianRepository;

	@Override
	@Transactional
	public StudentEntity createStudent(StudentEntity student)
	{
		if (student.getGuardian() == null || student.getGuardian().getId() == null)
			throw new IllegalArgumentException("Guardian é obrigatório");

		GuardianEntity guardian = guardianRepository.findById(student.getGuardian().getId())
				.orElseThrow(() -> new IllegalArgumentException("Guardian não encontrado"));
		student.setGuardian(guardian);

		// GERAÇÃO AUTOMÁTICA STU-00001-2026
		if (student.getCode() == null || student.getCode().isBlank())
		{
			int year = LocalDate.now().getYear();
			String yearStr = String.valueOf(year);
			String pattern = "STU-%-" + yearStr;

			// Busca último código do ano
			List<String> lastCodes = em.createQuery("SELECT s.code FROM StudentEntity s WHERE s.code LIKE :pattern ORDER BY s.code DESC", String.class)
					.setParameter("pattern", pattern).setMaxResults(1).getResultList();

			int nextSeq = 1;
			if (!lastCodes.isEmpty())
			{
				try
				{
					// STU-00001-2026 -> extrai 00001
					String last = lastCodes.get(0);
					String seqPart = last.substring(4, 9); // 00001
					nextSeq = Integer.parseInt(seqPart) + 1;
				} catch (Exception e)
				{
					nextSeq = 1;
				}
			}

			String code = String.format("STU-%05d-%s", nextSeq, yearStr);
			student.setCode(code);
		}

		if (student.getDocumentNumber() != null)
		{
			studentRepository.findByDocument(student.getDocumentNumber()).ifPresent(s ->
			{
				throw new IllegalStateException("Já existe aluno com BI " + student.getDocumentNumber());
			});
		}

		return studentRepository.save(student);
	}

	@Override
	@Transactional
	public StudentEntity registerStudent(StudentEntity student, Long guardianId)
	{
		student.setGuardian(new GuardianEntity()
		{
			{
				setId(guardianId);
			}
		});
		return createStudent(student);
	}

	@Override
	@Transactional
	public StudentEntity updateStudent(StudentEntity student)
	{
		StudentEntity existing = studentRepository.findById(student.getId()).orElseThrow(() -> new IllegalArgumentException("Estudante não encontrado"));
		existing.setFirstName(student.getFirstName());
		existing.setLastName(student.getLastName());
		existing.setBirthDate(student.getBirthDate());
		existing.setGender(student.getGender());
		existing.setPhone(student.getPhone());
		existing.setEmail(student.getEmail());
		existing.setAddress(student.getAddress());
		existing.setDocumentNumber(student.getDocumentNumber());
		existing.setStatus(student.getStatus());
		if (student.getGuardian() != null)
		{
			GuardianEntity g = guardianRepository.findById(student.getGuardian().getId()).orElse(null);
			if (g != null)
				existing.setGuardian(g);
		}
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
		return studentRepository.findByIdWithDetails(id);
	}

	@Override
	public Optional<StudentEntity> getStudentByCode(String code)
	{
		return studentRepository.findByCode(code);
	}

	@Override
	public List<StudentEntity> getAllStudents()
	{
		return studentRepository.findAllWithDetails();
	}

	@Override
	public List<StudentEntity> getStudentsByGuardianId(Long guardianId)
	{
		return studentRepository.findByGuardianId(guardianId);
	}

	@Override
	public List<StudentEntity> getStudentsByStatus(StudentStatus status)
	{
		return studentRepository.findByStatus(status);
	}

	@Override
	public List<StudentEntity> findByName(String name)
	{
		return studentRepository.findByName(name);
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
	public List<StudentEntity> getStudentsByAcademicYearId(Long academicYearId)
	{
		return studentRepository.findByAcademicYearId(academicYearId);
	}

	@Override
	public long countStudents()
	{
		return studentRepository.countAll();
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

	@Override
	public long countByGuardianId(Long guardianId)
	{
		return studentRepository.countByGuardianId(guardianId);
	}

	@Override
	public StudentEntity getByIdAndSchoolAndYear(Long id, Long schoolId, Long yearId)
	{
		return studentRepository.findByIdAndSchoolIdAndAcademicYearId(id, schoolId, yearId).orElse(null);
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
}