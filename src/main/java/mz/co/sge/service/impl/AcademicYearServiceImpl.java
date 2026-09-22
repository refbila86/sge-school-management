package mz.co.sge.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.repository.IAcademicYearRepository;
import mz.co.sge.service.IAcademicYearService;

@Service
@Transactional
public class AcademicYearServiceImpl implements IAcademicYearService
{

	private final IAcademicYearRepository repository;

	public AcademicYearServiceImpl(IAcademicYearRepository repository)
	{
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<AcademicYearEntity> getAllAcademicYears()
	{
		return repository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<AcademicYearEntity> getActiveAcademicYear()
	{
		return repository.findActive();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<AcademicYearEntity> getAcademicYearById(Long id)
	{
		return repository.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<AcademicYearEntity> getAcademicYearByYear(String year)
	{
		return repository.findByYear(year);
	}

	@Override
	public AcademicYearEntity createAcademicYear(AcademicYearEntity academicYear)
	{
		if (academicYear.getYear() == null || academicYear.getYear().isBlank())
		{
			throw new IllegalArgumentException("O campo year é obrigatório");
		}

		if (repository.findByDescription(academicYear.getYear()).isPresent())
		{
			throw new RuntimeException("Ano acadêmico " + academicYear.getYear() + " já existe");
		}

		if (academicYear.getActive() == null)
		{
			academicYear.setActive(false);
		}

		return repository.save(academicYear);
	}

	@Override
	public AcademicYearEntity updateAcademicYear(AcademicYearEntity academicYear)
	{
		if (academicYear.getId() == null)
		{
			throw new IllegalArgumentException("ID é obrigatório para atualização");
		}
		return repository.save(academicYear);
	}

	@Override
	public void deleteAcademicYear(Long id)
	{
		AcademicYearEntity entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Ano não encontrado: " + id));

		if (Boolean.TRUE.equals(entity.getActive()))
		{
			throw new RuntimeException("Não pode remover o ano ativo. Ative outro antes.");
		}

		repository.delete(id);
	}

	@Override
	public AcademicYearEntity activateAcademicYear(Long id)
	{
		AcademicYearEntity toActive = repository.findById(id).orElseThrow(() -> new RuntimeException("Ano não encontrado: " + id));

		List<AcademicYearEntity> allActive = repository.findAll().stream().filter(a -> Boolean.TRUE.equals(a.getActive())).toList();

		for (AcademicYearEntity ay : allActive)
		{
			ay.setActive(false);
			repository.save(ay);
		}

		toActive.setActive(true);
		return repository.save(toActive);
	}

	@Override
	public List<AcademicYearEntity> findByYear(String year)
	{
		 return repository.findByYearContaining(year);
	}
}