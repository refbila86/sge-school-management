package mz.co.sge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mz.co.sge.entity.SchoolEntity;

@Repository
public interface SchoolRepository extends JpaRepository<SchoolEntity, Long>
{

	Optional<SchoolEntity> findByCode(@Param("code") String code);

	Optional<SchoolEntity> findByLicenseId(@Param("licenseId") Long licenseId);

	List<SchoolEntity> findAllActiveOrderByNameAsc();

	boolean existsByCode(@Param("code") String code);
}