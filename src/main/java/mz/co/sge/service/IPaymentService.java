package mz.co.sge.service;

import java.math.BigDecimal;
import java.util.List;
import mz.co.sge.entity.PaymentEntity;
import mz.co.sge.entity.PaymentItemEntity;
import mz.co.sge.enumes.PaymentMethod;
import mz.co.sge.enumes.PaymentStatus;

public interface IPaymentService
{
	PaymentEntity createWithItems(PaymentEntity payment, List<PaymentItemEntity> cart);

	PaymentEntity getWithItems(Long id);

	List<PaymentEntity> getBySchoolAndYearId(Long schoolId, Long yearId);

	List<PaymentEntity> getByStudentId(Long studentId, Long yearId);

	BigDecimal getTotalBySchoolAndYear(Long schoolId, Long yearId);

	long getCountBySchoolAndYear(Long schoolId, Long yearId);

	void delete(Long id);
	
	boolean hasPaid(Long studentId, Long yearId, Long schoolId, String keyword);
	
	List<PaymentEntity> getPaidByStudent(Long studentId, Long yearId, Long schoolId);
	
	boolean canEnroll(Long studentId, Long yearId, Long schoolId);
	
	public PaymentEntity updateWithItems(Long paymentId, List<PaymentItemEntity> cart, PaymentMethod paymentMethod, String ref);
}