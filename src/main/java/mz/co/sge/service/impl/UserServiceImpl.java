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

	// NOVO - sem schoolId
	@Override
	public UserEntity authenticate(String username, String rawPassword)
	{
		Optional<UserEntity> found = userRepository.findByUsername(username);
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

	// LEGADO - mantido pra não quebrar LoginBean que ainda passa schoolId
	@Override
	public UserEntity authenticate(String username, String rawPassword, Long schoolId)
	{
		return authenticate(username, rawPassword);
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
		// agora retorna todos, escola é global
		return userRepository.findAllByOrderByNameAsc();
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserEntity> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId)
	{
		return userRepository.findAllByOrderByNameAsc();
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
		return userRepository.existsByUsername(username);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByUsernameAndSchoolIdAndIdNot(String username, Long schoolId, Long id)
	{
		return userRepository.existsByUsernameAndIdNot(username, id);
	}

	// NOVOS métodos limpos
	@Transactional(readOnly = true)
	public boolean existsByUsername(String username)
	{
		return userRepository.existsByUsername(username);
	}
}