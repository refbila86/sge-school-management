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
		BigDecimal total = cart.stream().map(i -> i.getSubtotal() != null ? i.getSubtotal() : (i.getValor() != null ? i.getValor() : BigDecimal.ZERO))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		payment.setTotalAmount(total);
		payment.setItems(new ArrayList<>());

		for (PaymentItemEntity it : cart)
		{
			PaymentItemEntity newItem = new PaymentItemEntity();
			newItem.setDescricao(it.getDescricao());
			newItem.setValor(it.getValor());
			newItem.setSubtotal(it.getSubtotal());
			newItem.setQuantidade(it.getQuantidade() != null ? it.getQuantidade() : BigDecimal.ONE);
			newItem.setPayment(payment);

			FeeItemEntity feeItem = null;

			// 1. Se veio com ID do cart
			if (it.getFeeItem() != null && it.getFeeItem().getId() != null)
			{
				feeItem = em.find(FeeItemEntity.class, it.getFeeItem().getId());
			}

			// 2. Busca por descricao usando a NamedQuery que já existe
			if (feeItem == null && it.getDescricao() != null)
			{
				try
				{
					List<FeeItemEntity> list = em.createNamedQuery("FeeItem.findByDescricao", FeeItemEntity.class)
							.setParameter("schoolId", payment.getSchool() != null ? payment.getSchool().getId() : 1L)
							.setParameter("yearId", payment.getAcademicYear() != null ? payment.getAcademicYear().getId() : 1L)
							.setParameter("term", it.getDescricao().trim()).setMaxResults(1).getResultList();
					if (!list.isEmpty())
						feeItem = list.get(0);
				} catch (Exception e)
				{
					// fallback direto por descricao LIKE
					try
					{
						List<FeeItemEntity> list2 = em
								.createQuery("SELECT f FROM FeeItemEntity f WHERE LOWER(f.descricao) LIKE LOWER(:desc)", FeeItemEntity.class)
								.setParameter("desc", "%" + it.getDescricao().trim() + "%").setMaxResults(1).getResultList();
						if (!list2.isEmpty())
							feeItem = list2.get(0);
					} catch (Exception ignore)
					{
					}
				}
			}

			// 3. Último fallback - busca ID 1,2,3,4 que tu mostraste
			if (feeItem == null && it.getDescricao() != null)
			{
				String upper = it.getDescricao().toUpperCase();
				try
				{
					Long idFallback = null;
					if (upper.contains("INSCRI"))
						idFallback = 1L;
					else if (upper.contains("MATRIC"))
						idFallback = 2L;
					else if (upper.contains("5") || upper.contains("1ª"))
						idFallback = 3L;
					else if (upper.contains("6"))
						idFallback = 4L;
					if (idFallback != null)
						feeItem = em.find(FeeItemEntity.class, idFallback);
				} catch (Exception ignore)
				{
				}
			}

			if (feeItem == null)
			{
				throw new RuntimeException("FeeItem não encontrado para item: " + it.getDescricao() + " - verifica se existe na tabela fee_items");
			}

			newItem.setFeeItem(feeItem);
			// garante que descricao e valor vêm do FeeItem se estiverem vazios
			if (newItem.getDescricao() == null)
				newItem.setDescricao(feeItem.getDescricao());
			if (newItem.getValor() == null)
				newItem.setValor(feeItem.getValor());
			if (newItem.getSubtotal() == null)
				newItem.calc();

			payment.getItems().add(newItem);
		}

		em.persist(payment);
		em.flush();
		handleMatriculaStatus(payment);
		return payment;
	}

	public PaymentEntity updateWithItemsS(Long paymentId, List<PaymentItemEntity> cart, PaymentMethod method, String ref)
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

	@Override
	@Transactional
	public PaymentEntity updateWithItems(Long paymentId, List<PaymentItemEntity> cart, PaymentMethod paymentMethod, String ref)
	{
		PaymentEntity payment = null;

		// 1. Busca pagamento - tenta NamedQuery depois em.find
		try
		{
			List<PaymentEntity> list = em.createNamedQuery("Payment.findWithItems", PaymentEntity.class).setParameter("id", paymentId).getResultList();
			if (!list.isEmpty())
				payment = list.get(0);
		} catch (Exception e)
		{
			System.out.println("findWithItems falhou para id " + paymentId + ": " + e.getMessage());
		}

		if (payment == null)
		{
			payment = em.find(PaymentEntity.class, paymentId);
		}

		if (payment == null)
		{
			throw new jakarta.persistence.EntityNotFoundException(
					"Pagamento não encontrado: " + paymentId + ". Verifique: SELECT * FROM payments WHERE id = " + paymentId);
		}

		// 2. Atualiza método e referência
		if (paymentMethod != null)
			payment.setMethod(paymentMethod);
		if (ref != null)
			payment.setReference(ref);

		// 3. Recalcula total
		BigDecimal total = cart.stream().map(i ->
		{
			if (i.getSubtotal() != null)
				return i.getSubtotal();
			if (i.getValor() != null && i.getQuantidade() != null)
				return i.getValor().multiply(i.getQuantidade());
			if (i.getValor() != null)
				return i.getValor();
			return BigDecimal.ZERO;
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		payment.setTotalAmount(total);

		// 4. Limpa items antigos
		payment.getItems().clear();
		em.flush(); // importante para orphanRemoval

		// 5. Adiciona novos items com FeeItem buscado por descricao (tua Entity usa
		// descricao)
		for (PaymentItemEntity it : cart)
		{
			PaymentItemEntity newItem = new PaymentItemEntity();
			newItem.setDescricao(it.getDescricao());
			newItem.setValor(it.getValor());
			newItem.setQuantidade(it.getQuantidade() != null ? it.getQuantidade() : BigDecimal.ONE);
			newItem.setSubtotal(it.getSubtotal());
			if (newItem.getSubtotal() == null)
				newItem.calc();
			newItem.setPayment(payment);

			FeeItemEntity feeItem = null;
			if (it.getFeeItem() != null && it.getFeeItem().getId() != null)
			{
				feeItem = em.find(FeeItemEntity.class, it.getFeeItem().getId());
			}
			if (feeItem == null && it.getDescricao() != null)
			{
				List<FeeItemEntity> list = em
						.createQuery("SELECT f FROM FeeItemEntity f WHERE LOWER(f.descricao) LIKE LOWER(:d) ORDER BY f.id", FeeItemEntity.class)
						.setParameter("d", "%" + it.getDescricao().trim() + "%").setMaxResults(1).getResultList();
				if (!list.isEmpty())
					feeItem = list.get(0);
			}
			// fallback final IDs que tu mostraste: 1=INSCRIÇÃO, 2=MATRICULA
			if (feeItem == null && it.getDescricao() != null)
			{
				String u = it.getDescricao().toUpperCase();
				if (u.contains("INSCRI"))
					feeItem = em.find(FeeItemEntity.class, 1L);
				else if (u.contains("MATRIC"))
					feeItem = em.find(FeeItemEntity.class, 2L);
			}

			if (feeItem == null)
			{
				throw new RuntimeException("FeeItem não encontrado para item: " + it.getDescricao());
			}

			newItem.setFeeItem(feeItem);
			payment.getItems().add(newItem);
		}

		payment = em.merge(payment);
		em.flush();

		handleMatriculaStatus(payment);
		return payment;
	}

}