package mz.co.sge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "prices", uniqueConstraints = @UniqueConstraint(columnNames =
{ "academic_year_id", "class_level_id" }))
@NamedQueries(
{ @NamedQuery(name = "Price.findAll", query = "SELECT p FROM PriceEntity p"),
		@NamedQuery(name = "Price.findByYearAndClass", query = "SELECT p FROM PriceEntity p WHERE p.academicYear.id = :yearId AND p.classLevel.id = :classId AND p.ativo = true"),
		@NamedQuery(name = "Price.findByYear", query = "SELECT p FROM PriceEntity p WHERE p.academicYear.id = :yearId") })
public class PriceEntity implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "academic_year_id", nullable = false)
	private AcademicYearEntity academicYear;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "class_level_id", nullable = false)
	private ClassLevelEntity classLevel;
	
	@Column(nullable = false)
	private java.math.BigDecimal precoMatricula;
	
	@Column(nullable = false)
	private java.math.BigDecimal precoMensalidade;
	
	private boolean ativo = true;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public AcademicYearEntity getAcademicYear()
	{
		return academicYear;
	}

	public void setAcademicYear(AcademicYearEntity academicYear)
	{
		this.academicYear = academicYear;
	}

	public ClassLevelEntity getClassLevel()
	{
		return classLevel;
	}

	public void setClassLevel(ClassLevelEntity classLevel)
	{
		this.classLevel = classLevel;
	}

	public java.math.BigDecimal getPrecoMatricula()
	{
		return precoMatricula;
	}

	public void setPrecoMatricula(java.math.BigDecimal precoMatricula)
	{
		this.precoMatricula = precoMatricula;
	}

	public java.math.BigDecimal getPrecoMensalidade()
	{
		return precoMensalidade;
	}

	public void setPrecoMensalidade(java.math.BigDecimal precoMensalidade)
	{
		this.precoMensalidade = precoMensalidade;
	}

	public boolean isAtivo()
	{
		return ativo;
	}

	public void setAtivo(boolean ativo)
	{
		this.ativo = ativo;
	}
}