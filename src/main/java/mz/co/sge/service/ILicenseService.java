package mz.co.sge.service;

import java.util.List;
import java.util.Optional;

import mz.co.sge.entity.LicenseEntity;

public interface ILicenseService
{
	List<LicenseEntity> getAll();

	Optional<LicenseEntity> getById(Long id);

	Optional<LicenseEntity> getByCode(String code);

	LicenseEntity save(LicenseEntity license);

	void delete(Long id);
}