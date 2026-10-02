package mz.co.sge.entity;

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
import jakarta.persistence.Table;
import mz.co.sge.enumes.AllocationStatus;

@Entity
@Table(name = "classroom_students")
@NamedQueries(
{ @NamedQuery(name = "ClassroomStudent.findByClassroom", query = "SELECT cs FROM ClassroomStudentEntity cs WHERE cs.classroom.id = :classroomId AND cs.status = mz.co.sge.enumes.AllocationStatus.ATIVO"),
		@NamedQuery(name = "ClassroomStudent.findByStudent", query = "SELECT cs FROM ClassroomStudentEntity cs WHERE cs.student.id = :studentId AND cs.academicYear.id = :yearId"),
		@NamedQuery(name = "ClassroomStudent.countByClassroom", query = "SELECT COUNT(cs) FROM ClassroomStudentEntity cs WHERE cs.classroom.id = :classroomId AND cs.status = mz.co.sge.enumes.AllocationStatus.ATIVO") })
public class ClassroomStudentEntity implements java.io.Serializable
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(optional = false)
	@JoinColumn(name = "student_id")
	private StudentEntity student;
	@ManyToOne(optional = false)
	@JoinColumn(name = "classroom_id")
	private ClassroomEntity classroom;
	@ManyToOne(optional = false)
	@JoinColumn(name = "academic_year_id")
	private AcademicYearEntity academicYear;
	@Enumerated(EnumType.STRING)
	private AllocationStatus status = AllocationStatus.ATIVO;
	private java.time.LocalDateTime allocationDate = java.time.LocalDateTime.now();

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

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity academicYear)
	{
		this.academicYear = academicYear;
	}

	public AllocationStatus getStatus()
	{
		return status;
	}

	public void setStatus(AllocationStatus status)
	{
		this.status = status;
	}

	public java.time.LocalDateTime getAllocationDate()
	{
		return allocationDate;
	}

	public void setAllocationDate(java.time.LocalDateTime allocationDate)
	{
		this.allocationDate = allocationDate;
	}
}