package mz.co.sge.service;

import mz.co.sge.entity.AcademicYearEntity;
import java.util.List;
import java.util.Optional;

public interface IAcademicYearService {

    List<AcademicYearEntity> getAllAcademicYears();
    
    Optional<AcademicYearEntity> getActiveAcademicYear();
    
    Optional<AcademicYearEntity> getAcademicYearById(Long id);
    
    Optional<AcademicYearEntity> getAcademicYearByYear(String year);
    
    AcademicYearEntity createAcademicYear(AcademicYearEntity academicYear);
    
    AcademicYearEntity updateAcademicYear(AcademicYearEntity academicYear);
    
    void deleteAcademicYear(Long id);
    
    AcademicYearEntity activateAcademicYear(Long id);
    
    List<AcademicYearEntity> findByYear(String year);
}