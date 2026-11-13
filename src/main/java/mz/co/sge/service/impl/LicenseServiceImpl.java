package mz.co.sge.service.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import mz.co.sge.entity.LicenseEntity;
import mz.co.sge.repository.ILicenseRepository;
import mz.co.sge.service.ILicenseService;

@Service
public class LicenseServiceImpl implements ILicenseService
{
	private final ILicenseRepository licenseRepository;

	private static final SecureRandom RANDOM = new SecureRandom();
	
	private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sem I,O,0,1 para não confundir

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
		if (license.getUuid() == null)
			license.setUuid(UUID.randomUUID().toString());

		// GERA AUTOMATICAMENTE SE VAZIO
		if (license.getLicenseKey() == null || license.getLicenseKey().isBlank())
		{
			license.setLicenseKey(generateLicenseKey());
		}
		if (license.getCode() == null || license.getCode().isBlank())
		{
			license.setCode(license.getLicenseKey());
		}

		if (license.getStartDate() == null && license.getActivationDate() == null)
		{
			license.setStartDate(LocalDate.now());
			license.setActivationDate(LocalDate.now());
		}
		if (license.getActivationDate() == null)
			license.setActivationDate(license.getStartDate());
		if (license.getStartDate() == null)
			license.setStartDate(license.getActivationDate());

		if (license.getEndDate() == null && license.getExpirationDate() == null && license.getDurationDays() != null)
		{
			LocalDate base = license.getStartDate();
			license.setEndDate(base.plusDays(license.getDurationDays()));
			license.setExpirationDate(license.getEndDate());
		}
		if (license.getExpirationDate() == null)
			license.setExpirationDate(license.getEndDate());

		return licenseRepository.save(license);
	}

	public LicenseEntity saveLicense(LicenseEntity license)
	{
		if (license.getUuid() == null)
		{
			license.setUuid(UUID.randomUUID().toString());
		}
		if (license.getLicenseKey() == null && license.getCode() != null)
		{
			license.setLicenseKey(license.getCode());
		}
		if (license.getCode() == null && license.getLicenseKey() != null)
		{
			license.setCode(license.getLicenseKey());
		}

		// sua lógica original mantida
		if (license.getStartDate() == null && license.getActivationDate() == null)
		{
			license.setStartDate(LocalDate.now());
			license.setActivationDate(LocalDate.now());
		}
		if (license.getActivationDate() == null)
		{
			license.setActivationDate(license.getStartDate());
		}
		if (license.getStartDate() == null)
		{
			license.setStartDate(license.getActivationDate());
		}

		if (license.getEndDate() == null && license.getExpirationDate() == null && license.getDurationDays() != null)
		{
			LocalDate base = license.getStartDate() != null ? license.getStartDate() : LocalDate.now();
			license.setEndDate(base.plusDays(license.getDurationDays()));
			license.setExpirationDate(license.getEndDate());
		}
		if (license.getExpirationDate() == null)
		{
			license.setExpirationDate(license.getEndDate());
		}

		return licenseRepository.save(license);
	}

	// GERADOR AUTOMÁTICO - formato SGE-MZ-XXXX-XXXX-XXXX-XXXX
	public String generateLicenseKey()
	{
		String key;
		do
		{
			key = String.format("SGE-MZ-%s-%s-%s-%s", randomBlock(4), randomBlock(4), randomBlock(4), randomBlock(4));
		} while (licenseRepository.existsByLicenseKey(key) || licenseRepository.existsByCode(key));
		return key;
	}

	private String randomBlock(int len)
	{
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
		{
			sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		}
		return sb.toString();
	}

	@Override
	public void delete(Long id)
	{
		licenseRepository.deleteById(id);
	}

	
	 
}