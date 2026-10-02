package mz.co.sge.entity;

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
import mz.co.sge.enumes.Turno;

@Entity
@Table(name = "classrooms", uniqueConstraints = @UniqueConstraint(columnNames =
{ "name", "academic_year_id" }))
@NamedQueries(
{ @NamedQuery(name = "Classroom.findAll", query = "SELECT c FROM ClassroomEntity c ORDER BY c.name ASC"),
		@NamedQuery(name = "Classroom.findByAcademicYear", query = "SELECT c FROM ClassroomEntity c WHERE c.academicYear.id = :yearId"),
		@NamedQuery(name = "Classroom.findByShift", query = "SELECT c FROM ClassroomEntity c WHERE c.shift = :shift AND c.academicYear.id = :yearId"),
		@NamedQuery(name = "Classroom.findByResponsibleTeacher", query = "SELECT c FROM ClassroomEntity c WHERE c.responsibleTeacher.id = :teacherId AND c.academicYear.id = :yearId") })
public class ClassroomEntity implements java.io.Serializable
{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;  

	@Column(nullable = false)
	private int capacity = 35;

	@Column(nullable = false)
	private int currentStudents = 0;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Turno shift; // MORNING, AFTERNOON, NIGHT

	@ManyToOne
	@JoinColumn(name = "responsible_teacher_id")
	private TeacherEntity responsibleTeacher; // Tutor of the classroom

	@ManyToOne(optional = false)
	@JoinColumn(name = "class_level_id", nullable = false)
	private ClassLevelEntity classLevel; // 8th Grade, 10th Grade

	@ManyToOne(optional = false)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYearEntity academicYear;

	@Column(nullable = false)
	private boolean active = true;

	private LocalDateTime createdAt = LocalDateTime.now();

	@PrePersist
	public void prePersist()
	{
		if (this.createdAt == null)
		{
			this.createdAt = LocalDateTime.now();
		}
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getCapacity()
	{
		return capacity;
	}

	public void setCapacity(int capacity)
	{
		this.capacity = capacity;
	}

	public int getCurrentStudents()
	{
		return currentStudents;
	}

	public void setCurrentStudents(int currentStudents)
	{
		this.currentStudents = currentStudents;
	}

	public Turno getShift()
	{
		return shift;
	}

	public void setShift(Turno shift)
	{
		this.shift = shift;
	}

	public TeacherEntity getResponsibleTeacher()
	{
		return responsibleTeacher;
	}

	public void setResponsibleTeacher(TeacherEntity responsibleTeacher)
	{
		this.responsibleTeacher = responsibleTeacher;
	}

	public ClassLevelEntity getClassLevel()
	{
		return classLevel;
	}

	public void setClassLevel(ClassLevelEntity classLevel)
	{
		this.classLevel = classLevel;
	}

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity academicYear)
	{
		this.academicYear = academicYear;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
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