package mz.co.sge.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import mz.co.sge.enumes.PaymentMethod;

@Entity
@Table(name = "payments")
@NamedQueries(
{
		@NamedQuery(name = "Payment.findWithItems", query = "SELECT DISTINCT p FROM PaymentEntity p LEFT JOIN FETCH p.items LEFT JOIN FETCH p.student LEFT JOIN FETCH p.createdBy " + "WHERE p.id = :id"),
		@NamedQuery(name = "Payment.findBySchoolAndYear", query = "SELECT p FROM PaymentEntity p WHERE p.school.id = :schoolId AND p.academicYear.id = :yearId ORDER BY p.createdAt DESC"),
		@NamedQuery(name = "Payment.findByStudent", query = "SELECT p FROM PaymentEntity p WHERE p.student.id = :studentId AND p.academicYear.id = :yearId ORDER BY p.createdAt DESC"),
		@NamedQuery(name = "Payment.findByReceipt", query = "SELECT p FROM PaymentEntity p WHERE p.school.id = :schoolId AND (LOWER(p.receiptNumber) LIKE LOWER(CONCAT('%', :term, '%'))) ORDER BY p.createdAt DESC"),
		@NamedQuery(name = "Payment.countBySchoolAndYear", query = "SELECT COUNT(p) FROM PaymentEntity p WHERE p.school.id = :schoolId AND p.academicYear.id = :yearId"),
		@NamedQuery(name = "Payment.sumBySchoolAndYear", query = "SELECT SUM(p.totalAmount) FROM PaymentEntity p WHERE p.school.id = :schoolId AND p.academicYear.id = :yearId") })
public class PaymentEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, length = 30)
	private String receiptNumber;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_id", nullable = false)
	private SchoolEntity school;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYearEntity academicYear;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private StudentEntity student;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserEntity createdBy;

	@Column(precision = 12, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;
	@Enumerated(EnumType.STRING)
	private PaymentMethod method;
	private String reference;
	private LocalDateTime createdAt = LocalDateTime.now();

	@OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<PaymentItemEntity> items = new ArrayList<>();

	@PrePersist
	public void prePersist()
	{
		if (receiptNumber == null)
			receiptNumber = "REC-" + (System.currentTimeMillis() % 10000000);
		createdAt = LocalDateTime.now();
	}

	// getters/setters
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getReceiptNumber()
	{
		return receiptNumber;
	}

	public void setReceiptNumber(String s)
	{
		this.receiptNumber = s;
	}

	public SchoolEntity getSchool()
	{
		return school;
	}

	public void setSchool(SchoolEntity s)
	{
		this.school = s;
	}

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity a)
	{
		this.academicYear = a;
	}

	public StudentEntity getStudent()
	{
		return student;
	}

	public void setStudent(StudentEntity s)
	{
		this.student = s;
	}

	public UserEntity getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(UserEntity u)
	{
		this.createdBy = u;
	}

	public BigDecimal getTotalAmount()
	{
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal b)
	{
		this.totalAmount = b;
	}

	public PaymentMethod getMethod()
	{
		return method;
	}

	public void setMethod(PaymentMethod m)
	{
		this.method = m;
	}

	public String getReference()
	{
		return reference;
	}

	public void setReference(String r)
	{
		this.reference = r;
	}

	public LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime l)
	{
		this.createdAt = l;
	}

	public List<PaymentItemEntity> getItems()
	{
		return items;
	}

	public void setItems(List<PaymentItemEntity> items)
	{
		this.items = items;
	}
}