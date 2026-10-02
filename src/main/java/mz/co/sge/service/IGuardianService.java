package mz.co.sge.service;

import mz.co.sge.entity.GuardianEntity;
import java.util.List;
import java.util.Optional;

public interface IGuardianService
{

	// ---- Leituras no mesmo estilo do IAcademicYearService ----
	List<GuardianEntity> getAllGuardians();

	Optional<GuardianEntity> getGuardianById(Long id);

	Optional<GuardianEntity> getGuardianByCode(String code);

	Optional<GuardianEntity> getGuardianByUserId(Long userId);

	Optional<GuardianEntity> getGuardianWithStudents(Long id);

	List<GuardianEntity> getGuardiansByAcademicYear(Long academicYearId);

	// Buscas equivalentes ao findByYear() do AcademicYear
	List<GuardianEntity> findByRelationship(String relationship);

	List<GuardianEntity> findByName(String name);

	List<GuardianEntity> findByPhone(String phone);

	// ---- Escrita ----
	GuardianEntity createGuardian(GuardianEntity guardian);

	GuardianEntity updateGuardian(GuardianEntity guardian);

	void deleteGuardian(Long id);

	// ---- Por Escola ----
	List<GuardianEntity> getGuardiansBySchool(Long schoolId);

	List<GuardianEntity> getGuardiansBySchoolAndYear(Long schoolId, Long academicYearId);

	// ---- Contagem ----
	long countGuardians();
}
