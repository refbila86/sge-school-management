package mz.co.sge.service;

import java.util.List;
import java.util.Optional;

import mz.co.sge.entity.SchoolEntity;

public interface ISchoolService
{
	SchoolEntity save(SchoolEntity school);

	Optional<SchoolEntity> findById(Long id);

	Optional<SchoolEntity> findByCode(String code);

	Optional<SchoolEntity> findByLicenseId(Long licenseId);

	List<SchoolEntity> findAll();

	List<SchoolEntity> findAllActive();

	List<SchoolEntity> search(String term);

	boolean codeExists(String code);

	boolean licenseExists(Long licenseId);

	void delete(Long id);

	void toggleActive(Long id);
}