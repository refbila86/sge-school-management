package mz.co.sge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mz.co.sge.entity.UserEntity;
import mz.co.sge.repository.UserRepository;

@Service
@Transactional
public class UsersService
{

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UsersService(UserRepository userRepository, PasswordEncoder passwordEncoder)
	{
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public UserEntity save(UserEntity user)
	{
		if (user.getCreatedAt() == null)
		{
			user.setCreatedAt(LocalDateTime.now());
		}
		if (user.getActive() == null)
		{
			user.setActive(true);
		}
		if (user.getPassword() != null && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$"))
		{
			user.setPassword(passwordEncoder.encode(user.getPassword().trim()));
		}
		return userRepository.save(user);
	}

	// MÉTODO CORRETO - SEM schoolId
	public UserEntity authenticate(String username, String rawPassword, Long schoolId)
	{
		System.out.println("=== DEPURANDO LOGIN ===");
		System.out.println("Username: [" + username + "] schoolId ignorado: [" + schoolId + "]");

		Optional<UserEntity> found = userRepository.findByUsername(username);

		if (found.isEmpty())
		{
			System.out.println("--> FALHA: Utilizador não encontrado: " + username);
			return null;
		}

		UserEntity user = found.get();
		System.out.println("Encontrado: ID=" + user.getId() + " Active=" + user.getActive());

		if (!Boolean.TRUE.equals(user.getActive()))
		{
			System.out.println("--> FALHA: Inativo");
			return null;
		}

		boolean matches = passwordEncoder.matches(rawPassword.trim(), user.getPassword());
		System.out.println("Senha OK: " + matches);

		if (!matches)
		{
			System.out.println("--> FALHA: Senha incorreta");
			return null;
		}

		user.setLastLogin(LocalDateTime.now());
		return userRepository.save(user);
	}

	public UserEntity authenticates(String username, String rawPassword, Long schoolId)
	{
		return authenticate(username, rawPassword, schoolId);
	}

	@Transactional(readOnly = true)
	public Optional<UserEntity> findById(Long id)
	{
		return userRepository.findById(id);
	}

	@Transactional(readOnly = true)
	public List<UserEntity> findAllBySchool(Long schoolId)
	{
		// retorna todos, escola ignorada
		return userRepository.findAllByOrderByNameAsc();
	}

	@Transactional(readOnly = true)
	public boolean usernameExists(String username, Long schoolId)
	{
		return userRepository.existsByUsername(username);
	}

	public void delete(Long id)
	{
		userRepository.deleteById(id);
	}
}