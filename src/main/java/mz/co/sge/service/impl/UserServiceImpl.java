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
		System.out.println("=== DEPURANDO LOGIN ===");
		System.out.println("Username: [" + username + "]");
		System.out.println("SchoolId: [" + schoolId + "]");
		System.out.println("RawPassword length: " + (rawPassword != null ? rawPassword.length() : "null"));
		System.out.println("RawPassword bytes: [" + rawPassword + "]");

		Optional<UserEntity> found = userRepository.findByUsernameAndSchoolId(username, schoolId);
		if (found.isEmpty())
		{
			System.out.println("--> FALHA: Utilizador não encontrado");
			return null;
		}
		UserEntity user = found.get();
		System.out.println("Hash no BD: [" + user.getPassword() + "] len=" + user.getPassword().length());

		if (!Boolean.TRUE.equals(user.getActive()))
			return null;

		// Teste importante
		boolean passwordMatches = passwordEncoder.matches(rawPassword.trim(), user.getPassword());
		System.out.println("Matches: " + passwordMatches);
		if (!passwordMatches)
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
	public Optional<UserEntity> findById(Long id)
	{
		return userRepository.findById(id);
	}

	@Transactional(readOnly = true)
	public List<UserEntity> findAllBySchool(Long schoolId)
	{
		return userRepository.findBySchoolIdOrderByNameAsc(schoolId);
	}

	@Transactional(readOnly = true)
	public boolean usernameExists(String username, Long schoolId)
	{
		return userRepository.existsByUsernameAndSchoolId(username, schoolId);
	}

	@Override
	public UserEntity save(UserEntity user)
	{
		if (user.getCreatedAt() == null)
			user.setCreatedAt(LocalDateTime.now());
		if (user.getActive() == null)
			user.setActive(true);

		// CORRIGIDO: detecta qualquer hash BCrypt válido
		String pwd = user.getPassword();
		if (pwd != null && !pwd.matches("^\\$2[aby]?\\$\\d+\\$.*"))
		{
			System.out.println("Codificando senha nova para o user: " + user.getUsername());
			user.setPassword(passwordEncoder.encode(pwd));
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
}