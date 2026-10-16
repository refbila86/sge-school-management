package mz.co.sge.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.PaymentEntity;
import mz.co.sge.entity.PaymentItemEntity;
import mz.co.sge.entity.StudentEntity;
import mz.co.sge.enumes.PaymentMethod;
import mz.co.sge.enumes.StudentStatus;
import mz.co.sge.repository.IPaymentRepository;
import mz.co.sge.service.IPaymentService;

@Service
public class PaymentServiceImpl implements IPaymentService
{

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private IPaymentRepository paymentRepository;

	@Override
	@Transactional
	public PaymentEntity createWithItems(PaymentEntity payment, List<PaymentItemEntity> cart)
	{
		BigDecimal total = cart.stream().map(PaymentItemEntity::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		payment.setTotalAmount(total);

		payment.setItems(new ArrayList<>());

		for (PaymentItemEntity it : cart)
		{
			PaymentItemEntity newItem = new PaymentItemEntity();
			newItem.setDescricao(it.getDescricao());
			newItem.setValor(it.getValor());
			newItem.setSubtotal(it.getSubtotal());
			newItem.setQuantidade(it.getQuantidade());
			newItem.setPayment(payment);
			payment.getItems().add(newItem);
		}

		em.persist(payment);
		em.flush();

		handleMatriculaStatus(payment);

		return payment;
	}

	@Transactional
	public PaymentEntity updateWithItems(Long paymentId, List<PaymentItemEntity> cart, PaymentMethod method, String ref)
	{
		PaymentEntity managed = paymentRepository.findWithItems(paymentId).orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

		BigDecimal total = cart.stream().map(PaymentItemEntity::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

		managed.setTotalAmount(total);
		managed.setMethod(method);
		managed.setReference(ref);

		managed.getItems().clear();
		em.flush();

		for (PaymentItemEntity it : cart)
		{
			PaymentItemEntity newItem = new PaymentItemEntity();
			newItem.setDescricao(it.getDescricao());
			newItem.setValor(it.getValor());
			newItem.setSubtotal(it.getSubtotal());
			newItem.setQuantidade(it.getQuantidade());
			newItem.setPayment(managed);
			managed.getItems().add(newItem);
		}

		return em.merge(managed);
	}

	@Override
	@Transactional(readOnly = true)
	public PaymentEntity getWithItems(Long id)
	{
		return paymentRepository.findWithItems(id).orElse(null);
	}

	@Override
	public List<PaymentEntity> getBySchoolAndYearId(Long s, Long y)
	{
		return paymentRepository.findBySchoolAndYearId(s, y);
	}

	@Override
	public List<PaymentEntity> getByStudentId(Long sid, Long y)
	{
		return paymentRepository.findByStudentId(sid, y);
	}

	@Override
	public BigDecimal getTotalBySchoolAndYear(Long s, Long y)
	{
		return paymentRepository.sumTotalBySchoolAndYear(s, y);
	}

	@Override
	public long getCountBySchoolAndYear(Long s, Long y)
	{
		return paymentRepository.countBySchoolAndYear(s, y);
	}

	@Override
	@Transactional
	public void delete(Long id)
	{
		paymentRepository.delete(id);
	}

	private void handleMatriculaStatus(PaymentEntity payment)
	{
		boolean temMatricula = payment.getItems().stream().anyMatch(it -> it.getDescricao() != null && it.getDescricao().toUpperCase().contains("MATRICULA"));

		if (temMatricula && payment.getStudent() != null)
		{
			StudentEntity student = em.find(StudentEntity.class, payment.getStudent().getId());
			if (student != null && student.getStatus() == StudentStatus.PENDING_ENROLLMENT)
			{
				student.setStatus(StudentStatus.REGISTERED);
				em.merge(student);
			}
		}
	}
}