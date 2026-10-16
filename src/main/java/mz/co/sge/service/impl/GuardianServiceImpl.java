package mz.co.sge.service.impl;

import mz.co.sge.entity.GuardianEntity;
import mz.co.sge.repository.IGuardianRepository;
import mz.co.sge.service.IGuardianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GuardianServiceImpl implements IGuardianService
{

	@Autowired
	private IGuardianRepository guardianRepository;

	public GuardianServiceImpl()
	{
	}

	public GuardianServiceImpl(IGuardianRepository guardianRepository)
	{
		this.guardianRepository = guardianRepository;
	}

	@Override
	@Transactional
	public GuardianEntity createGuardian(GuardianEntity guardian)
	{
		if (guardian.getCode() == null || guardian.getCode().isBlank())
		{
			// guardian.setCode("ENC-" + UUID.randomUUID().toString().substring(0,
			// 8).toUpperCase());

			guardian.setCode("ENC-" + (System.currentTimeMillis() % 100000));
		}
		return guardianRepository.save(guardian);
	}

	@Override
	@Transactional
	public GuardianEntity updateGuardian(GuardianEntity guardian)
	{
		if (guardian.getId() == null)
		{
			throw new IllegalArgumentException("ID do encarregado é obrigatório para atualização.");
		}
		GuardianEntity existing = guardianRepository.findById(guardian.getId())
				.orElseThrow(() -> new IllegalArgumentException("Encarregado não encontrado com ID: " + guardian.getId()));

		existing.setFirstName(guardian.getFirstName());
		existing.setLastName(guardian.getLastName());
		existing.setPhone(guardian.getPhone());
		existing.setEmail(guardian.getEmail());
		existing.setAddress(guardian.getAddress());
		existing.setRelationship(guardian.getRelationship());
		existing.setAcademicYear(guardian.getAcademicYear());
		if (guardian.getUser() != null)
		{
			existing.setUser(guardian.getUser());
		}
		return guardianRepository.update(existing);
	}

	@Override
	@Transactional
	public void deleteGuardian(Long id)
	{
		GuardianEntity guardian = guardianRepository.findWithStudents(id)
				.orElseThrow(() -> new IllegalArgumentException("Guardian não encontrado com ID: " + id));

		if (guardian.getStudents() != null && !guardian.getStudents().isEmpty())
		{
			throw new IllegalStateException("Não pode remover guardian com " + guardian.getStudents().size() + " estudante(s) associado(s).");
		}
		guardianRepository.delete(id);
	}

	@Override
	public Optional<GuardianEntity> getGuardianById(Long id)
	{
		return guardianRepository.findById(id);
	}

	@Override
	public Optional<GuardianEntity> getGuardianWithStudents(Long id)
	{
		return guardianRepository.findWithStudents(id);
	}

	@Override
	public Optional<GuardianEntity> getGuardianByCode(String code)
	{
		return guardianRepository.findByCode(code);
	}

	@Override
	public Optional<GuardianEntity> getGuardianByUserId(Long userId)
	{
		return guardianRepository.findByUserId(userId);
	}

	@Override
	public List<GuardianEntity> getAllGuardians()
	{
		return guardianRepository.findAllWithStudents(); // com FETCH - sem LAZY
	}

	@Override
	public List<GuardianEntity> getGuardiansByAcademicYear(Long academicYearId)
	{
		return guardianRepository.findByAcademicYearId(academicYearId);
	}

	@Override
	public List<GuardianEntity> findByRelationship(String relationship)
	{
		if (relationship == null || relationship.isBlank())
			return getAllGuardians();
		return guardianRepository.findAll().stream().filter(g -> relationship.equalsIgnoreCase(g.getRelationship())).collect(Collectors.toList());
	}

	@Override
	public List<GuardianEntity> findByName(String name)
	{
		if (name == null || name.isBlank())
			return getAllGuardians();
		String term = name.toLowerCase();
		return guardianRepository.findAll().stream().filter(g -> g.getFullName().toLowerCase().contains(term) || g.getCode().toLowerCase().contains(term))
				.collect(Collectors.toList());
	}

	@Override
	public List<GuardianEntity> findByPhone(String phone)
	{
		if (phone == null || phone.isBlank())
			return getAllGuardians();
		return guardianRepository.findAll().stream().filter(g -> g.getPhone() != null && g.getPhone().contains(phone)).collect(Collectors.toList());
	}

	@Override
	public List<GuardianEntity> getGuardiansBySchool(Long schoolId)
	{
		if (schoolId == null)
			return getAllGuardians();
		return guardianRepository.findBySchoolId(schoolId);
	}

	@Override
	public List<GuardianEntity> getGuardiansBySchoolAndYear(Long schoolId, Long academicYearId)
	{
		if (schoolId == null || academicYearId == null)
		{
			if (schoolId != null)
				return getGuardiansBySchool(schoolId);
			return getAllGuardians();
		}
		return guardianRepository.findBySchoolIdAndAcademicYearId(schoolId, academicYearId);
	}

	@Override
	public long countGuardians()
	{
		return guardianRepository.countAll();
	}

	@Override
	public long countGuardiansByAcademicYear(Long academicYearId)
	{
		return guardianRepository.countAllByAcademicYearId(academicYearId);
	}

	@Override
	public List<GuardianEntity> findByAcademicYearId(Long academicYearId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long countAllByAcademicYearId(Long academicYearId)
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
