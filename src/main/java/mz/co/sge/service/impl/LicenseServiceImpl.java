package mz.co.sge.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import mz.co.sge.entity.LicenseEntity;
import mz.co.sge.repository.ILicenseRepository;
import mz.co.sge.service.ILicenseService;

@Service
public class LicenseServiceImpl implements ILicenseService
{

	private final ILicenseRepository licenseRepository;

	public LicenseServiceImpl(ILicenseRepository licenseRepository)
	{
		this.licenseRepository = licenseRepository;
	}

	@Override
	public List<LicenseEntity> getAll()
	{
		return licenseRepository.findAll();
	}

	@Override
	public Optional<LicenseEntity> getById(Long id)
	{
		return licenseRepository.findById(id);
	}

	@Override
	public Optional<LicenseEntity> getByCode(String code)
	{
		return licenseRepository.findByCode(code);
	}

	@Override
	public LicenseEntity save(LicenseEntity license)
	{
		if (license.getStartDate() == null)
		{
			license.setStartDate(LocalDate.now());
		}
		if (license.getEndDate() == null && license.getDurationDays() != null)
		{
			license.setEndDate(license.getStartDate().plusDays(license.getDurationDays()));
		}
		return licenseRepository.save(license);
	}

	@Override
	public void delete(Long id)
	{
		licenseRepository.deleteById(id);
	}
}