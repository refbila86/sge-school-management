package mz.co.sge.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import mz.co.sge.entity.PaymentEntity;

public interface IPaymentRepository
{
	PaymentEntity save(PaymentEntity entity);

	PaymentEntity update(PaymentEntity entity);

	void delete(Long id);

	Optional<PaymentEntity> findById(Long id);

	Optional<PaymentEntity> findWithItems(Long id);

	List<PaymentEntity> findBySchoolAndYearId(Long schoolId, Long yearId);

	List<PaymentEntity> findByStudentId(Long studentId, Long yearId);

	List<PaymentEntity> findByReceipt(String term, Long schoolId);

	long countBySchoolAndYear(Long schoolId, Long yearId);

	BigDecimal sumTotalBySchoolAndYear(Long schoolId, Long yearId);
}