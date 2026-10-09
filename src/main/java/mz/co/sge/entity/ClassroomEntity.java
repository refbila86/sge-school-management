package mz.co.sge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "classrooms", uniqueConstraints = @UniqueConstraint(name = "uk_classroom_year_shift", columnNames = {"name", "academic_year_id", "shift"}))
@NamedQueries(
{ @NamedQuery(name = "Classroom.findAll", query = "SELECT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.responsibleTeacher LEFT JOIN FETCH c.academicYear ORDER BY c.name"),
		@NamedQuery(name = "Classroom.findByAcademicYear", query = "SELECT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.responsibleTeacher LEFT JOIN FETCH c.academicYear WHERE c.academicYear.id = :yearId ORDER BY c.name"),
		@NamedQuery(name = "Classroom.search", query = "SELECT c FROM ClassroomEntity c WHERE (LOWER(c.name) LIKE :term OR LOWER(c.classLevel.name) LIKE :term) AND c.academicYear.id = :yearId"),
		@NamedQuery(name = "Classroom.countByAcademicYear", query = "SELECT COUNT(c) FROM ClassroomEntity c WHERE c.academicYear.id = :yearId") })
public class ClassroomEntity implements java.io.Serializable
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false)
	private int capacity;

	@Column(name = "current_students", nullable = false)
	private int currentStudents = 0;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Shift shift;

	@Column(nullable = false)
	private boolean active = true;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "responsible_teacher_id")
	private TeacherEntity responsibleTeacher;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "class_level_id", nullable = false)
	private ClassLevelEntity classLevel;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYearEntity academicYear;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	public enum Shift
	{
		MORNING, AFTERNOON, NIGHT
	}

	@PrePersist
	public void prePersist()
	{
		if (createdAt == null)
			createdAt = LocalDateTime.now();
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

	public Shift getShift()
	{
		return shift;
	}

	public void setShift(Shift shift)
	{
		this.shift = shift;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
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

	public LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public int getPercentOcupacao()
	{
		if (capacity <= 0)
			return 0;
		return (int) Math.round((currentStudents * 100.0) / capacity);
	}

	@Transient
	public String getProfessorNome()
	{
		return responsibleTeacher != null ? responsibleTeacher.getFirstName() + " " + responsibleTeacher.getLastName() : null;
	}

	@Transient
	public String getClasseNome()
	{
		return classLevel != null ? classLevel.getName() : null;
	}
}