package mz.co.sge.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.repository.SchoolRepository;
import mz.co.sge.service.ISchoolService;

@Service
@Transactional
public class SchoolServiceImpl implements ISchoolService
{

    private final SchoolRepository schoolRepository;

    public SchoolServiceImpl(SchoolRepository schoolRepository)
    {
        this.schoolRepository = schoolRepository;
    }

    @Override
    public SchoolEntity save(SchoolEntity school)
    {
        return schoolRepository.save(school);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchoolEntity> findById(Long id)
    {
        return schoolRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchoolEntity> findByCode(String code)
    {
        return schoolRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchoolEntity> findByLicenseId(Long licenseId)
    {
        return schoolRepository.findByLicenseId(licenseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolEntity> findAll()
    {
        return schoolRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolEntity> findAllActive()
    {
        return schoolRepository.findAllActiveOrderByNameAsc();
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<SchoolEntity> search(String term)
//    {
//        if (term == null || term.trim().isEmpty())
//        {
//            return findAll();
//        }
//        return schoolRepository.searchByTerm(term.trim());
//    }

    @Override
    @Transactional(readOnly = true)
    public boolean codeExists(String code)
    {
        return schoolRepository.existsByCode(code);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public boolean licenseExists(Long licenseId)
//    {
//        return schoolRepository.existsByLicenseId(licenseId);
//    }

    @Override
    public void delete(Long id)
    {
        schoolRepository.deleteById(id);
    }

    @Override
    public void toggleActive(Long id)
    {
        var opt = schoolRepository.findById(id);
        if (opt.isPresent())
        {
            SchoolEntity school = opt.get();
            school.setActive(!school.getActive());
            schoolRepository.save(school);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolEntity> search(String term)
    {
        if (term == null || term.trim().isEmpty())
        {
            return findAll();
        }
        return schoolRepository.searchByTerm(term.trim());
    }
	@Override
	public boolean licenseExists(Long licenseId)
	{
		// TODO Auto-generated method stub
		return false;
	}
}