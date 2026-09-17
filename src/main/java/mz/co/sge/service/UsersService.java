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

	// Injeção de dependência via construtor (prática recomendada pelo Spring)
	public UsersService(UserRepository userRepository, PasswordEncoder passwordEncoder)
	{
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Salva ou atualiza um utilizador. Encripta a senha apenas se ela ainda não
	 * estiver encriptada (evita re-encriptar ao editar).
	 */
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

		// Verifica se a senha precisa ser encriptada
		if (user.getPassword() != null && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$"))
		{
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}

		return userRepository.save(user);
	}

	/**
	 * Autentica o utilizador verificando username, senha e escola. Este é o método
	 * exato que o LoginBean irá chamar.
	 */
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

	public UserEntity authenticates(String username, String rawPassword, Long schoolId)
	{

		Optional<UserEntity> found = userRepository.findByUsernameAndSchoolId(username, schoolId);

		if (found.isEmpty())
		{
			return null;
		}

		UserEntity user = found.get();

		if (!Boolean.TRUE.equals(user.getActive()))
		{
			return null;
		}

		if (!passwordEncoder.matches(rawPassword, user.getPassword()))
		{
			return null;
		}

		user.setLastLogin(LocalDateTime.now());
		return userRepository.save(user);
	}

	// =========================================================
	// MÉTODOS AUXILIARES (Utilizados pelo UserBean para Gestão)
	// =========================================================

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

	public void delete(Long id)
	{
		userRepository.deleteById(id);
	}
}