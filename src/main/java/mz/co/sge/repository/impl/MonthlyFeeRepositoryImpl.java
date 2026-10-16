package mz.co.sge.repository.impl;

import mz.co.sge.entity.MonthlyFeeEntity;
import mz.co.sge.repository.IMonthlyFeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class MonthlyFeeRepositoryImpl implements IMonthlyFeeRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public MonthlyFeeEntity save(MonthlyFeeEntity entity) {
        if (entity.getId() == null) {
            em.persist(entity);
            return entity;
        } else {
            return em.merge(entity);
        }
    }

    @Override
    public List<MonthlyFeeEntity> saveAll(List<MonthlyFeeEntity> entities) {
        List<MonthlyFeeEntity> saved = new java.util.ArrayList<>();
        for (MonthlyFeeEntity e : entities) {
            saved.add(save(e));
        }
        return saved;
    }

    @Override
    public Optional<MonthlyFeeEntity> findById(Long id) {
        return Optional.ofNullable(em.find(MonthlyFeeEntity.class, id));
    }

    @Override
    public Optional<MonthlyFeeEntity> findByReference(String reference) {
        try {
            MonthlyFeeEntity e = em.createQuery("SELECT m FROM MonthlyFeeEntity m WHERE m.reference = :ref", MonthlyFeeEntity.class)
                    .setParameter("ref", reference).getSingleResult();
            return Optional.of(e);
        } catch (Exception ex) { return Optional.empty(); }
    }

    @Override
    public List<MonthlyFeeEntity> findByEnrollmentId(Long enrollmentId) {
        return em.createQuery("SELECT m FROM MonthlyFeeEntity m LEFT JOIN FETCH m.enrollment LEFT JOIN FETCH m.student WHERE m.enrollment.id = :enrollId ORDER BY m.year, m.month", MonthlyFeeEntity.class)
                .setParameter("enrollId", enrollmentId).getResultList();
    }

    @Override
    public List<MonthlyFeeEntity> findByStudentId(Long studentId) {
        return em.createQuery("SELECT m FROM MonthlyFeeEntity m WHERE m.student.id = :studId ORDER BY m.year DESC, m.month DESC", MonthlyFeeEntity.class)
                .setParameter("studId", studentId).getResultList();
    }

    @Override
    public List<MonthlyFeeEntity> findByYear(Integer year) {
        return em.createQuery("SELECT m FROM MonthlyFeeEntity m WHERE m.year = :year ORDER BY m.month", MonthlyFeeEntity.class)
                .setParameter("year", year).getResultList();
    }

    @Override
    public boolean existsByEnrollmentAndMonthYear(Long enrollmentId, Integer month, Integer year) {
        Long count = em.createQuery("SELECT COUNT(m) FROM MonthlyFeeEntity m WHERE m.enrollment.id = :enrollId AND m.month = :month AND m.year = :year", Long.class)
                .setParameter("enrollId", enrollmentId).setParameter("month", month).setParameter("year", year).getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public long countByEnrollmentId(Long enrollmentId) {
        return em.createQuery("SELECT COUNT(m) FROM MonthlyFeeEntity m WHERE m.enrollment.id = :enrollId", Long.class)
                .setParameter("enrollId", enrollmentId).getSingleResult();
    }

    @Override
    public void deleteByEnrollmentId(Long enrollmentId) {
        em.createQuery("DELETE FROM MonthlyFeeEntity m WHERE m.enrollment.id = :enrollId").setParameter("enrollId", enrollmentId).executeUpdate();
    }

    @Override
    public List<MonthlyFeeEntity> findAll() {
        return em.createQuery("SELECT m FROM MonthlyFeeEntity m LEFT JOIN FETCH m.enrollment LEFT JOIN FETCH m.student ORDER BY m.year DESC, m.month DESC", MonthlyFeeEntity.class).getResultList();
    }
}
