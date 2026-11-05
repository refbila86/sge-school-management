package mz.co.sge.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "classrooms")
@NamedQueries(
{ @NamedQuery(name = "Classroom.findAll", query = "SELECT DISTINCT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.academicYear LEFT JOIN FETCH c.responsibleTeacher ORDER BY c.name"),
		@NamedQuery(name = "Classroom.findByAcademicYear", query = "SELECT DISTINCT c FROM ClassroomEntity c LEFT JOIN FETCH c.classLevel LEFT JOIN FETCH c.academicYear LEFT JOIN FETCH c.responsibleTeacher WHERE c.academicYear.id = :yearId ORDER BY c.name"),
		@NamedQuery(name = "ClassroomEntity.findAllActive", query = "SELECT c FROM ClassroomEntity c WHERE c.active = true ORDER BY c.name") })
public class ClassroomEntity implements Serializable
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private Integer capacity;
	private Integer currentStudents = 0;
	private String shift;
	private Boolean active = true;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "responsible_teacher_id")
	private TeacherEntity responsibleTeacher;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "class_level_id")
	private ClassLevelEntity classLevel;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "academic_year_id")
	private AcademicYearEntity academicYear;

	public ClassroomEntity()
	{
	}

	public ClassroomEntity(Long id)
	{
		this.id = id;
	}

	// GETTERS E SETTERS BLINDADOS
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
		return name != null ? name : "";
	}

	public void setName(String n)
	{
		this.name = n;
	}

	public Integer getCapacity()
	{
		return capacity;
	}

	public void setCapacity(Integer c)
	{
		this.capacity = c;
	}

	public Integer getCurrentStudents()
	{
		return currentStudents != null ? currentStudents : 0;
	}

	public void setCurrentStudents(Integer c)
	{
		this.currentStudents = c;
	}

	public String getShift()
	{
		return shift;
	}

	public void setShift(String s)
	{
		this.shift = s;
	}

	public Boolean getActive()
	{
		return active != null ? active : true;
	}

	public void setActive(Boolean a)
	{
		this.active = a;
	}

	public TeacherEntity getResponsibleTeacher()
	{
		return responsibleTeacher;
	}

	public void setResponsibleTeacher(TeacherEntity t)
	{
		this.responsibleTeacher = t;
	}

	public ClassLevelEntity getClassLevel()
	{
		return classLevel;
	}

	public void setClassLevel(ClassLevelEntity c)
	{
		this.classLevel = c;
	}

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity a)
	{
		this.academicYear = a;
	}

	@Transient
	public String getClasseNome()
	{
		try
		{
			if (classLevel != null)
				return classLevel.getName();
		} catch (Exception e)
		{
		}
		return "-";
	}

	@Transient
	public String getProfessorNome()
	{
		try
		{
			if (responsibleTeacher != null)
			{
				if (responsibleTeacher.getFullName() != null)
					return responsibleTeacher.getFullName();
				return responsibleTeacher.getFirstName() + " " + responsibleTeacher.getLastName();
			}
		} catch (Exception e)
		{
		}
		return null;
	}

	@Transient
	public int getVacancy()
	{
		try
		{
			if (capacity == null)
				return 0;
			return capacity - getCurrentStudents();
		} catch (Exception e)
		{
			return 0;
		}
	}

	@Transient
	public int getOccupancyPercent()
	{
		try
		{
			if (capacity == null || capacity == 0)
				return 0;
			return (int) (getCurrentStudents() * 100.0 / capacity);
		} catch (Exception e)
		{
			return 0;
		}
	}
}