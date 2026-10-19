package mz.co.sge.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mz.co.sge.entity.PaymentEntity;
import mz.co.sge.entity.PaymentItemEntity;
import mz.co.sge.enumes.EnrollmentType;

@Service
public class PaymentValidationService
{

	@PersistenceContext
	private EntityManager em;

	public PaymentStatusDTO checkStatus(Long studentId, Long academicYearId, EnrollmentType type)
	{
		PaymentStatusDTO dto = new PaymentStatusDTO();
		dto.type = type;

		List<PaymentEntity> payments = em
				.createQuery("SELECT DISTINCT p FROM PaymentEntity p LEFT JOIN FETCH p.items pi LEFT JOIN FETCH pi.feeItem "
						+ "WHERE p.student.id = :sid AND p.academicYear.id = :yearId ORDER BY p.paymentDate DESC", PaymentEntity.class)
				.setParameter("sid", studentId).setParameter("yearId", academicYearId).getResultList();

		if (payments.isEmpty())
		{
			payments = em.createQuery("SELECT DISTINCT p FROM PaymentEntity p LEFT JOIN FETCH p.items pi LEFT JOIN FETCH pi.feeItem "
					+ "WHERE p.student.id = :sid ORDER BY p.paymentDate DESC", PaymentEntity.class).setParameter("sid", studentId).getResultList();
		}

		for (PaymentEntity p : payments)
		{
			if (p.getItems() == null)
				continue;
			for (PaymentItemEntity pi : p.getItems())
			{
				String desc = pi.getFeeItem() != null ? pi.getFeeItem().getDescricao() : pi.getDescricao();
				if (desc == null)
					continue;
				String u = desc.toUpperCase();
				if (u.contains("INSCRI"))
					dto.hasPaidEnrollment = true;
				if (u.contains("MATRIC"))
					dto.hasPaidRegistration = true;
				if (u.contains("RENOV"))
					dto.hasPaidRenewal = true;
			}
		}

		if (type == EnrollmentType.FIRST_ENROLLMENT)
		{
			dto.readyForEnrollment = dto.hasPaidEnrollment && dto.hasPaidRegistration;
		} else
		{
			dto.readyForEnrollment = dto.hasPaidRenewal;
		}
		return dto;
	}

	public static class PaymentStatusDTO
	{
		public boolean hasPaidEnrollment = false;
		public boolean hasPaidRegistration = false;
		public boolean hasPaidRenewal = false;
		public boolean readyForEnrollment = false;
		public EnrollmentType type;
	}
}