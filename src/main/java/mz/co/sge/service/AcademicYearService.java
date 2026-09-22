package mz.co.sge.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.enterprise.context.ApplicationScoped;
import mz.co.sge.entity.AcademicYearEntity;

@ApplicationScoped
public class AcademicYearService {

    @Autowired
    private IAcademicYearService academicYearServiceImpl;

    public List<AcademicYearEntity> findAll() {
        return academicYearServiceImpl.getAllAcademicYears();
    }

    public Optional<AcademicYearEntity> findActive() {
        return academicYearServiceImpl.getActiveAcademicYear();
    }

    public AcademicYearEntity save(AcademicYearEntity entity) {
        if (entity.getId() == null) {
            return academicYearServiceImpl.createAcademicYear(entity);
        }
        return academicYearServiceImpl.updateAcademicYear(entity);
    }
    
    
}