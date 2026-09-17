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
		// LOG 1: Verificar os parâmetros que chegaram
		System.out.println("=== DEPURANDO LOGIN ===");
		System.out.println("Username recebido: [" + username + "]");
		System.out.println("SchoolId recebido: [" + schoolId + "]");

		Optional<UserEntity> found = userRepository.findByUsernameAndSchoolId(username, schoolId);

		if (found.isEmpty())
		{
			System.out.println("--> FALHA: Utilizador não encontrado para este Username e SchoolId no BD.");
			return null;
		}

		UserEntity user = found.get();
		System.out.println("Utilizador encontrado no BD: ID=" + user.getId() + ", Active=" + user.getActive());

		if (!Boolean.TRUE.equals(user.getActive()))
		{
			System.out.println("--> FALHA: O campo 'active' está como FALSE ou NULL no BD.");
			return null;
		}

		boolean passwordMatches = passwordEncoder.matches(rawPassword, user.getPassword());
		System.out.println("Validação da Senha BCrypt: " + passwordMatches);

		if (!passwordMatches)
		{
			System.out.println("--> FALHA: A senha fornecida não corresponde à Hash BCrypt do BD.");
			System.out.println("Hash no BD: " + user.getPassword());
			return null;
		}

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
		// Se for um novo usuário e a senha estiver em texto puro, criptografa com
		// BCrypt
		if (user.getCreatedAt() == null)
		{
			user.setCreatedAt(LocalDateTime.now());
		}
		if (user.getActive() == null)
		{
			user.setActive(true);
		}

		if (user.getId() == null || !user.getPassword().startsWith("$2a$"))
		{
			user.setPassword(passwordEncoder.encode(user.getPassword()));
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