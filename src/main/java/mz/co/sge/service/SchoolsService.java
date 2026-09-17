package mz.co.sge.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.repository.SchoolRepository;

@Service
@Transactional
public class SchoolsService
{

	private final SchoolRepository schoolRepository;

	public SchoolsService(SchoolRepository schoolRepository)
	{
		this.schoolRepository = schoolRepository;
	}

	public SchoolEntity save(SchoolEntity school)
	{
		return schoolRepository.save(school);
	}

	@Transactional(readOnly = true)
	public Optional<SchoolEntity> findById(Long id)
	{
		return schoolRepository.findById(id);
	}

	@Transactional(readOnly = true)
	public Optional<SchoolEntity> findByCode(String code)
	{
		return schoolRepository.findByCode(code);
	}

	@Transactional(readOnly = true)
	public Optional<SchoolEntity> findByLicenseId(Long licenseId)
	{
		return schoolRepository.findByLicenseId(licenseId);
	}

	@Transactional(readOnly = true)
	public List<SchoolEntity> findAll()
	{
		return schoolRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<SchoolEntity> findAllActive()
	{
		return schoolRepository.findAllActiveOrderByNameAsc();
	}

	@Transactional(readOnly = true)
	public boolean codeExists(String code)
	{
		return schoolRepository.existsByCode(code);
	}

	public void delete(Long id)
	{
		schoolRepository.deleteById(id);
	}
}