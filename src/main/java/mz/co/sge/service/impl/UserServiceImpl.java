package mz.co.sge.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mz.co.sge.entity.UserEntity;
import mz.co.sge.repository.UserRepository;
import mz.co.sge.service.IUserService;

@Service
@Transactional
public class UserServiceImpl implements IUserService
{

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder)
	{
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserEntity authenticate(String username, String rawPassword, Long schoolId)
	{
		Optional<UserEntity> found = userRepository.findByUsernameAndSchoolId(username, schoolId);
		if (found.isEmpty())
			return null;

		UserEntity user = found.get();
		if (!Boolean.TRUE.equals(user.getActive()))
			return null;
		if (!passwordEncoder.matches(rawPassword.trim(), user.getPassword()))
			return null;

		user.setLastLogin(LocalDateTime.now());
		return userRepository.save(user);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserEntity> findAll()
	{
		return userRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserEntity> findBySchoolId(Long schoolId)
	{
		return userRepository.findBySchoolIdOrderByNameAsc(schoolId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId)
	{
		if (academicYearId == null)
			return findBySchoolId(schoolId);
		return userRepository.findBySchoolIdAndAcademicYearIdOrderByNameAsc(schoolId, academicYearId);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserEntity> findById(Long id)
	{
		return userRepository.findById(id);
	}

	@Override
	public UserEntity save(UserEntity user)
	{
		if (user.getCreatedAt() == null)
			user.setCreatedAt(LocalDateTime.now());
		if (user.getActive() == null)
			user.setActive(true);

		// Só codifica se ainda não for BCrypt
		String pwd = user.getPassword();
		if (pwd != null && !pwd.matches("^\\$2[aby]?\\$\\d+\\$.*"))
		{
			user.setPassword(passwordEncoder.encode(pwd.trim()));
		}
		return userRepository.save(user);
	}

	@Override
	public void delete(Long id)
	{
		userRepository.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByUsernameAndSchoolId(String username, Long schoolId)
	{
		return userRepository.existsByUsernameAndSchoolId(username, schoolId);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByUsernameAndSchoolIdAndIdNot(String username, Long schoolId, Long id)
	{
		return userRepository.existsByUsernameAndSchoolIdAndIdNot(username, schoolId, id);
	}
}