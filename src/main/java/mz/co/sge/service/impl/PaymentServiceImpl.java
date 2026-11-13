package mz.co.sge.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.FeeItemEntity;
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

		// BLOQUEIO DE DUPLICIDADE
		for (PaymentItemEntity it : cart)
		{
			String desc = it.getDescricao().toLowerCase();
			if (desc.contains("inscri") && hasPaid(payment.getStudent().getId(), payment.getAcademicYear().getId(), payment.getSchool().getId(), "inscri"))
			{
				throw new IllegalStateException("Aluno já pagou INSCRIÇÃO em " + payment.getAcademicYear().getYear());
			}
			if (desc.contains("matric") && hasPaid(payment.getStudent().getId(), payment.getAcademicYear().getId(), payment.getSchool().getId(), "matric"))
			{
				throw new IllegalStateException("Aluno já pagou MATRÍCULA em " + payment.getAcademicYear().getYear());
			}
		}

		BigDecimal total = cart.stream().map(i -> i.getSubtotal() != null ? i.getSubtotal() : i.getValor()).reduce(BigDecimal.ZERO, BigDecimal::add);

		payment.setTotalAmount(total);
		payment.setItems(new ArrayList<>());

		for (PaymentItemEntity it : cart)
		{
			PaymentItemEntity newItem = new PaymentItemEntity();
			newItem.setDescricao(it.getDescricao());
			newItem.setValor(it.getValor());
			newItem.setQuantidade(it.getQuantidade() != null ? it.getQuantidade() : BigDecimal.ONE);
			newItem.calc();
			newItem.setPayment(payment);

			FeeItemEntity feeItem = resolveFeeItem(it, payment.getSchool().getId(), payment.getAcademicYear().getId());
			newItem.setFeeItem(feeItem);
			payment.getItems().add(newItem);
		}

		em.persist(payment);
		em.flush();
		handleMatriculaStatus(payment);
		return payment;
	}

	private FeeItemEntity resolveFeeItem(PaymentItemEntity it, Long schoolId, Long yearId)
	{
		if (it.getFeeItem() != null && it.getFeeItem().getId() != null)
		{
			FeeItemEntity f = em.find(FeeItemEntity.class, it.getFeeItem().getId());
			if (f != null)
				return f;
		}
		// busca por descrição aproximada
		List<FeeItemEntity> list = em
				.createQuery(
						"SELECT f FROM FeeItemEntity f WHERE f.school.id=:s AND f.academicYear.id=:y AND LOWER(f.descricao) LIKE LOWER(CONCAT('%',:d,'%'))",
						FeeItemEntity.class)
				.setParameter("s", schoolId).setParameter("y", yearId).setParameter("d", it.getDescricao().trim()).setMaxResults(1).getResultList();
		if (!list.isEmpty())
			return list.get(0);

		// fallback IDs fixos
		String u = it.getDescricao().toUpperCase();
		if (u.contains("INSCRI"))
			return em.find(FeeItemEntity.class, 1L);
		if (u.contains("MATRIC"))
			return em.find(FeeItemEntity.class, 2L);
		throw new RuntimeException("FeeItem não encontrado: " + it.getDescricao());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasPaid(Long studentId, Long yearId, Long schoolId, String keyword)
	{
		return paymentRepository.existsFeeByKeyword(studentId, yearId, keyword);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean canEnroll(Long studentId, Long yearId, Long schoolId)
	{
		return hasPaid(studentId, yearId, schoolId, "inscri") && hasPaid(studentId, yearId, schoolId, "matric");
	}

	@Override
	public List<PaymentEntity> getPaidByStudent(Long sid, Long y, Long s)
	{
		return paymentRepository.findPaidByStudentAndYear(sid, y, s);
	}

	private void handleMatriculaStatus(PaymentEntity payment)
	{
		boolean temMatricula = payment.getItems().stream().anyMatch(it -> it.getDescricao().toUpperCase().contains("MATRIC"));
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

	@Override
	@Transactional
	public PaymentEntity updateWithItems(Long paymentId, List<PaymentItemEntity> cart, PaymentMethod method, String ref)
	{
		PaymentEntity payment = paymentRepository.findWithItems(paymentId).orElse(em.find(PaymentEntity.class, paymentId));
		if (payment == null)
			throw new RuntimeException("Pagamento não encontrado");
		payment.setMethod(method);
		payment.setReference(ref);
		BigDecimal total = cart.stream().map(i -> i.getSubtotal() != null ? i.getSubtotal() : i.getValor()).reduce(BigDecimal.ZERO, BigDecimal::add);
		payment.setTotalAmount(total);
		payment.getItems().clear();
		em.flush();
		for (PaymentItemEntity it : cart)
		{
			PaymentItemEntity n = new PaymentItemEntity();
			n.setDescricao(it.getDescricao());
			n.setValor(it.getValor());
			n.setQuantidade(BigDecimal.ONE);
			n.calc();
			n.setPayment(payment);
			n.setFeeItem(resolveFeeItem(it, payment.getSchool().getId(), payment.getAcademicYear().getId()));
			payment.getItems().add(n);
		}
		return em.merge(payment);
	}

	@Override
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

}