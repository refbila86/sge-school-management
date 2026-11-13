package mz.co.sge.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import mz.co.sge.enumes.Trimester;

@Entity
@Table(name = "attendance", uniqueConstraints = @UniqueConstraint(columnNames =
{ "student_id", "subject_id", "attendance_date", "trimester" }))
@NamedQueries(
{ @NamedQuery(name = "AttendanceEntity.findByDateAndSubject", query = "SELECT a FROM AttendanceEntity a WHERE a.attendanceDate = :date AND a.subject.id = :subjectId AND a.classroom.id = :classroomId AND a.trimester = :trimester"),
		@NamedQuery(name = "AttendanceEntity.findByStudentAndSubjectAndTrimester", query = "SELECT a FROM AttendanceEntity a WHERE a.student.id = :studentId AND a.subject.id = :subjectId AND a.trimester = :trimester AND a.present = false"),
		@NamedQuery(name = "AttendanceEntity.countFaltasByStudentAndTrimester", query = "SELECT COUNT(a) FROM AttendanceEntity a WHERE a.student.id = :studentId AND a.subject.id = :subjectId AND a.trimester = :trimester AND a.present = false"),
		@NamedQuery(name = "AttendanceEntity.findByFilters", query = "SELECT a FROM AttendanceEntity a WHERE a.classroom.id = :classroomId AND a.subject.id = :subjectId AND a.academicYear.id = :yearId AND a.trimester = :trimester ORDER BY a.student.firstName"),
		@NamedQuery(name = "AttendanceEntity.findByFiltersNoDate", query = "SELECT a FROM AttendanceEntity a WHERE a.classroom.id = :classroomId AND a.subject.id = :subjectId AND a.academicYear.id = :yearId AND a.trimester = :trimester ORDER BY a.attendanceDate DESC, a.student.firstName"),
		@NamedQuery(name = "AttendanceEntity.findFaltasHoje", query = "SELECT COUNT(a) FROM AttendanceEntity a WHERE a.attendanceDate = :date AND a.subject.id = :subjectId AND a.present = false") })
public class AttendanceEntity implements Serializable
{

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

	@Column(name = "attendance_date", nullable = false)
	private LocalDate attendanceDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "trimester", nullable = false, length = 10)
	private Trimester trimester;

	@Column(nullable = false)
	private Boolean present = true;

	@Column
	private Boolean justified = false;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public AttendanceEntity()
	{
	}

	public AttendanceEntity(Long id)
	{
		this.id = id;
	}

	@PrePersist
	public void prePersist()
	{
		if (createdAt == null)
			createdAt = LocalDateTime.now();
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

	public void setStudent(StudentEntity s)
	{
		this.student = s;
	}

	public ClassroomEntity getClassroom()
	{
		return classroom;
	}

	public void setClassroom(ClassroomEntity c)
	{
		this.classroom = c;
	}

	public SubjectEntity getSubject()
	{
		return subject;
	}

	public void setSubject(SubjectEntity s)
	{
		this.subject = s;
	}

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity a)
	{
		this.academicYear = a;
	}

	public LocalDate getAttendanceDate()
	{
		return attendanceDate;
	}

	public void setAttendanceDate(LocalDate d)
	{
		this.attendanceDate = d;
	}

	public Trimester getTrimester()
	{
		return trimester;
	}

	public void setTrimester(Trimester t)
	{
		this.trimester = t;
	}

	// CORREÇÃO DO BUG JSF - present precisa dos 4 métodos
	public Boolean getPresent()
	{
		return present;
	}

	public void setPresent(Boolean p)
	{
		this.present = p;
	}

	public boolean isPresent()
	{
		return present != null && present;
	}

	public void setPresent(boolean present)
	{
		this.present = present;
	}

	// CORREÇÃO DO BUG JSF - justified também
	public Boolean getJustified()
	{
		return justified;
	}

	public void setJustified(Boolean j)
	{
		this.justified = j;
	}

	public boolean isJustified()
	{
		return justified != null && justified;
	}

	public void setJustified(boolean justified)
	{
		this.justified = justified;
	}

	public LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt)
	{
		this.createdAt = createdAt;
	}
}