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
import mz.co.sge.enumes.Turno;

@Entity
@Table(name = "teaching_assignments")
@NamedQueries(
{ @NamedQuery(name = "TeachingAssignment.findByTeacher", query = "SELECT ta FROM TeachingAssignmentEntity ta WHERE ta.teacher.id = :teacherId AND ta.academicYear.id = :yearId"),
		@NamedQuery(name = "TeachingAssignment.findByTeacherUser", query = "SELECT ta FROM TeachingAssignmentEntity ta WHERE ta.teacher.user.id = :userId AND ta.academicYear.id = :yearId"),
		@NamedQuery(name = "TeachingAssignment.findClassroomsByTeacher", query = "SELECT DISTINCT ta.classroom FROM TeachingAssignmentEntity ta WHERE ta.teacher.user.id = :userId AND ta.academicYear.id = :yearId"),
		@NamedQuery(name = "TeachingAssignment.findByClassroom", query = "SELECT ta FROM TeachingAssignmentEntity ta WHERE ta.classroom.id = :classroomId") })
public class TeachingAssignmentEntity implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(optional = false)
	private TeacherEntity teacher;
	
	@ManyToOne(optional = false)
	private ClassroomEntity classroom;
	
	@ManyToOne(optional = false)
	private SubjectEntity subject;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "academic_year_id")
	private AcademicYearEntity academicYear;
	@Enumerated(EnumType.STRING)
	private  Turno shift;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public TeacherEntity getTeacher()
	{
		return teacher;
	}

	public void setTeacher(TeacherEntity teacher)
	{
		this.teacher = teacher;
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

	public Turno getShift()
	{
		return shift;
	}

	public void setShift(Turno shift)
	{
		this.shift = shift;
	}
}