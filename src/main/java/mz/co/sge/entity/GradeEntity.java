package mz.co.sge.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import mz.co.sge.enumes.Trimester;

@Entity
@Table(name = "grade", uniqueConstraints = @UniqueConstraint(columnNames =
{ "student_id", "subject_id", "academic_year_id", "trimester" }))
@NamedQueries(
{ @NamedQuery(name = "GradeEntity.findAll", query = "SELECT g FROM GradeEntity g ORDER BY g.student.firstName, g.student.lastName"),
		@NamedQuery(name = "GradeEntity.findByFilters", query = "SELECT g FROM GradeEntity g WHERE g.classroom.id = :classroomId AND g.subject.id = :subjectId AND g.academicYear.id = :yearId AND g.trimester = :trimester ORDER BY g.student.firstName"),
		@NamedQuery(name = "GradeEntity.findByStudentAndSubject", query = "SELECT g FROM GradeEntity g WHERE g.student.id = :studentId AND g.subject.id = :subjectId AND g.academicYear.id = :yearId ORDER BY g.trimester"),
		@NamedQuery(name = "GradeEntity.findByYearAndTrimester", query = "SELECT g FROM GradeEntity g WHERE g.academicYear.id = :yearId AND g.trimester = :trimester") })
public class GradeEntity implements Serializable
{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "student_id")
	private StudentEntity student;

	@ManyToOne
	@JoinColumn(name = "classroom_id")
	private ClassroomEntity classroom;

	@ManyToOne
	@JoinColumn(name = "subject_id")
	private SubjectEntity subject;

	@ManyToOne
	@JoinColumn(name = "academic_year_id")
	private AcademicYearEntity academicYear;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Trimester trimester;

	@Column(name = "acs1")
	private Double acs1 = 0.0;

	@Column(name = "acs2")
	private Double acs2 = 0.0;

	@Column(name = "ap")
	private Double ap = 0.0;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	public void prePersist()
	{
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	public void preUpdate()
	{
		updatedAt = LocalDateTime.now();
	}

	// === CALCULADOS - NÃO SALVA NO BANCO ===
	@Transient
	public Double getMacs()
	{
		if (acs1 == null && acs2 == null)
			return null;
		double a1 = acs1 != null ? acs1 : 0;
		double a2 = acs2 != null ? acs2 : 0;
		double macs = (a1 + a2) / 2.0;
		return Math.round(macs * 10.0) / 10.0;

	}

	@Transient
	public Double getMediaFinal()
	{
		Double macs = getMacs();
		if (macs == null || ap == null)
			return null;
		double media = (macs + ap) / 2.0;
		return Math.round(media * 10.0) / 10.0;
	}

	@Transient
	public String getObservacao()
	{
		double m = getMediaFinal();
		if (m >= 16)
			return "Quadro Honra";
		if (m >= 10)
			return "Aprovado";
		if (m >= 8)
			return "Recuperação";
		return "Reprovado";
	}

	// Getters/Setters
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public StudentEntity getStudent()
	{
		return student;
	}

	public void setStudent(StudentEntity student)
	{
		this.student = student;
	}

	public ClassroomEntity getClassroom()
	{
		return classroom;
	}

	public void setClassroom(ClassroomEntity classroom)
	{
		this.classroom = classroom;
	}

	public SubjectEntity getSubject()
	{
		return subject;
	}

	public void setSubject(SubjectEntity subject)
	{
		this.subject = subject;
	}

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity academicYear)
	{
		this.academicYear = academicYear;
	}

	public Trimester getTrimester()
	{
		return trimester;
	}

	public void setTrimester(Trimester trimester)
	{
		this.trimester = trimester;
	}

	public Double getAcs1()
	{
		return acs1;
	}

	public void setAcs1(Double acs1)
	{
		this.acs1 = acs1;
	}

	public Double getAcs2()
	{
		return acs2;
	}

	public void setAcs2(Double acs2)
	{
		this.acs2 = acs2;
	}

	public Double getAp()
	{
		return ap;
	}

	public void setAp(Double ap)
	{
		this.ap = ap;
	}

	public LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public LocalDateTime getUpdatedAt()
	{
		return updatedAt;
	}
}