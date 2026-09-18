package mz.co.sge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mz.co.sge.entity.SchoolEntity;

@Repository
public interface SchoolRepository extends JpaRepository<SchoolEntity, Long>
{
    Optional<SchoolEntity> findByCode(String code);

    Optional<SchoolEntity> findByLicenseId(Long licenseId);

    boolean existsByCode(String code);

    boolean existsByLicenseId(Long licenseId);
    
    List<SchoolEntity> findAllActiveActiveSchools();

    @Query("SELECT s FROM SchoolEntity s WHERE s.active = true ORDER BY s.name ASC")
    List<SchoolEntity> findAllActiveOrderByNameAsc();

    @Query("SELECT s FROM SchoolEntity s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<SchoolEntity> searchByTerm(@Param("term") String term);
}