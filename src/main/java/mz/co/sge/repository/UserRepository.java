package mz.co.sge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mz.co.sge.entity.UserEntity;
import mz.co.sge.enumes.Role;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>
{

	Optional<UserEntity> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByUsernameAndIdNot(String username, Long id);
	
	List<UserEntity> findAllByOrderByNameAsc();

	Optional<UserEntity> findByEmail(String email);

	List<UserEntity> findAllByActiveTrueOrderByNameAsc();

	List<UserEntity> findByRoleOrderByNameAsc(Role role);
}