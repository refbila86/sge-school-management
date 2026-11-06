package mz.co.sge.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import jakarta.persistence.*;
import mz.co.sge.enumes.MunicipalDistrict;
import mz.co.sge.enumes.Province;
import mz.co.sge.enumes.StudentStatus;

@Entity
@Table(name = "students", uniqueConstraints =
{ @UniqueConstraint(name = "uk_student_code", columnNames = "code"), @UniqueConstraint(name = "uk_student_document", columnNames = "document_number"),
		@UniqueConstraint(name = "uk_student_bi", columnNames = "bi_number") })
@NamedQueries(
{ @NamedQuery(name = "Student.findAll", query = "SELECT s FROM StudentEntity s ORDER BY s.firstName"),
		@NamedQuery(name = "StudentEntity.findByCode", query = "SELECT s FROM StudentEntity s WHERE s.code = :code"),
		@NamedQuery(name = "StudentEntity.findByDocument", query = "SELECT s FROM StudentEntity s WHERE s.biNumber = :doc"),
		@NamedQuery(name = "StudentEntity.findByBi", query = "SELECT s FROM StudentEntity s WHERE s.biNumber = :bi"),
		@NamedQuery(name = "StudentEntity.findByStatus", query = "SELECT s FROM StudentEntity s WHERE s.status = :status"),
		@NamedQuery(name = "StudentEntity.findByGuardian", query = "SELECT s FROM StudentEntity s WHERE s.guardian.id = :guardianId ORDER BY s.firstName"),
		@NamedQuery(name = "StudentEntity.countAll", query = "SELECT COUNT(s) FROM StudentEntity s"),
		@NamedQuery(name = "StudentEntity.countByGuardian", query = "SELECT COUNT(s) FROM StudentEntity s WHERE s.guardian.id = :guardianId"),
		@NamedQuery(name = "StudentEntity.findNewStudentIds", query = "SELECT s.id FROM StudentEntity s WHERE s.id NOT IN (SELECT e.student.id FROM EnrollmentEntity e)"),
		@NamedQuery(name = "StudentEntity.findByIds", query = "SELECT s FROM StudentEntity s WHERE s.id IN :ids"),
		@NamedQuery(name = "StudentEntity.findByIdWithDetails", query = "SELECT DISTINCT s FROM StudentEntity s LEFT JOIN FETCH s.guardian LEFT JOIN FETCH s.enrollments e LEFT JOIN FETCH e.school LEFT JOIN FETCH e.academicYear LEFT JOIN FETCH e.classroom WHERE s.id = :id"),
		@NamedQuery(name = "StudentEntity.findByClassroom", query = "SELECT DISTINCT s FROM StudentEntity s JOIN s.enrollments e WHERE e.classroom.id = :classroomId AND e.academicYear.id = :yearId ORDER BY s.firstName, s.lastName"),
		@NamedQuery(name = "StudentEntity.findByClassroomId", query = "SELECT DISTINCT s FROM StudentEntity s JOIN s.enrollments e WHERE e.classroom.id = :classroomId ORDER BY s.firstName, s.lastName"),
		@NamedQuery(name = "StudentEntity.findBySchool", query = "SELECT DISTINCT s FROM StudentEntity s JOIN s.enrollments e WHERE e.school.id = :schoolId ORDER BY s.firstName"),
		@NamedQuery(name = "StudentEntity.findBySchoolAndYear", query = "SELECT DISTINCT s FROM StudentEntity s JOIN s.enrollments e WHERE e.school.id = :schoolId AND e.academicYear.id = :academicYearId ORDER BY s.firstName"),
		@NamedQuery(name = "StudentEntity.countBySchoolAndYear", query = "SELECT COUNT(DISTINCT s) FROM StudentEntity s JOIN s.enrollments e WHERE e.school.id = :schoolId AND e.academicYear.id = :academicYearId") })
public class StudentEntity implements java.io.Serializable
{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 20)
	private String code;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	private LocalDate birthDate;
	private String gender;
	private String phone;
	private String email;
	private String address;

	@Column(name = "bi_number", length = 13, unique = true)
	private String biNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "province", length = 50)
	private Province province;

	@Enumerated(EnumType.STRING)
	@Column(name = "district", length = 50)
	private MunicipalDistrict district;

	@Column(name = "naturalidade", length = 150)
	private String placeOfBirth;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StudentStatus status;

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "guardian_id", nullable = true)
	private GuardianEntity guardian;

	@OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@OrderBy("enrollmentDate DESC")
	private List<EnrollmentEntity> enrollments = new ArrayList<>();

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();
	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "photo", columnDefinition = "LONGBLOB")
	private byte[] photo;

	@Column(name = "photo_content_type", length = 100)
	private String photoContentType;

	@Transient
	private String fullName;

	@PrePersist
	public void prePersist()
	{
		if (this.status == null)
			this.status = StudentStatus.REGISTERED;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		validateAndNormalizeBi();
	}

	@PreUpdate
	public void preUpdate()
	{
		this.updatedAt = LocalDateTime.now();
		validateAndNormalizeBi();
	}

	private void validateAndNormalizeBi()
	{
		if (biNumber == null || biNumber.isBlank())
			return;
		String bi = biNumber.trim().toUpperCase().replaceAll("\\s+", "");
		if (bi.length() != 13)
			throw new IllegalArgumentException("BI deve ter 13 caracteres. Ex: 110101123456J");
		if (!bi.substring(0, 12).matches("\\d{12}"))
			throw new IllegalArgumentException("Os 12 primeiros do BI devem ser números");
		if (!Character.isLetter(bi.charAt(12)))
			throw new IllegalArgumentException("O último caractere do BI deve ser letra");
		this.biNumber = bi;
	}

	public String getBiFormatted()
	{
		if (biNumber == null || biNumber.length() != 13)
			return biNumber;
		return biNumber.substring(0, 4) + " " + biNumber.substring(4, 8) + " " + biNumber.substring(8, 12) + " " + biNumber.substring(12);
	}

	public boolean isFirstEnrollment()
	{
		return enrollments == null || enrollments.isEmpty();
	}

	public String getFullName()
	{
		return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
	}

	public String getPhotoBase64()
	{
		if (photo == null || photo.length == 0)
			return null;
		String type = photoContentType != null ? photoContentType : "image/jpeg";
		return "data:" + type + ";base64," + Base64.getEncoder().encodeToString(photo);
	}

	// GETTERS E SETTERS
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public LocalDate getBirthDate()
	{
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate)
	{
		this.birthDate = birthDate;
	}

	public String getGender()
	{
		return gender;
	}

	public void setGender(String gender)
	{
		this.gender = gender;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getBiNumber()
	{
		return biNumber;
	}

	public void setBiNumber(String biNumber)
	{
		this.biNumber = biNumber;
	}

	public Province getProvince()
	{
		return province;
	}

	public void setProvince(Province province)
	{
		this.province = province;
	}

	public MunicipalDistrict getDistrict()
	{
		return district;
	}

	public void setDistrict(MunicipalDistrict district)
	{
		this.district = district;
	}

	public String getPlaceOfBirth()
	{
		return placeOfBirth;
	}

	public void setPlaceOfBirth(String placeOfBirth)
	{
		this.placeOfBirth = placeOfBirth;
	}

	public StudentStatus getStatus()
	{
		return status;
	}

	public void setStatus(StudentStatus status)
	{
		this.status = status;
	}

	public GuardianEntity getGuardian()
	{
		return guardian;
	}

	public void setGuardian(GuardianEntity guardian)
	{
		this.guardian = guardian;
	}

	public List<EnrollmentEntity> getEnrollments()
	{
		return enrollments;
	}

	public void setEnrollments(List<EnrollmentEntity> enrollments)
	{
		this.enrollments = enrollments;
	}

	public LocalDateTime getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt)
	{
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt()
	{
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt)
	{
		this.updatedAt = updatedAt;
	}

	public String getPhotoContentType()
	{
		return photoContentType;
	}

	public void setPhotoContentType(String photoContentType)
	{
		this.photoContentType = photoContentType;
	}

	public byte[] getPhoto()
	{
		return photo;
	}

	public void setPhoto(byte[] photo)
	{
		this.photo = photo;
	}

	public String getFullNameTransient()
	{
		return fullName;
	}

	public void setFullName(String fullName)
	{
		this.fullName = fullName;
	}
}