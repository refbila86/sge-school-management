package mz.co.sge.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mz.co.sge.entity.LicenseEntity;

@Repository
public interface ILicenseRepository extends JpaRepository<LicenseEntity, Long>
{
	Optional<LicenseEntity> findByCode(String code);

	Optional<LicenseEntity> findByLicenseKey(String licenseKey);

	Optional<LicenseEntity> findByUuid(String uuid);

	boolean existsByCode(String code);

	boolean existsByLicenseKey(String licenseKey);
}