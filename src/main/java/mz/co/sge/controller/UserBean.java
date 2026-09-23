package mz.co.sge.controller;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import mz.co.sge.entity.UserEntity;
import mz.co.sge.enumes.Role;
import mz.co.sge.service.IUserService;

@Component("userBean")
@Scope("view")
public class UserBean implements Serializable
{

	private static final long serialVersionUID = 1L;

	private final IUserService userService;

	private UserEntity user;
	private List<UserEntity> users;
	private UserEntity selectedUser;

	private Long currentAcademicYearId; // vem do filtro

	public UserBean(IUserService userService)
	{
		this.userService = userService;
		newUser();
		loadUsers();
	}

	public void loadUsers()
	{
		Long schoolId = getLoggedSchoolId();
		if (currentAcademicYearId != null)
		{
			users = userService.findBySchoolIdAndAcademicYearId(schoolId, currentAcademicYearId);
		} else
		{
			users = userService.findBySchoolId(schoolId);
		}
	}

	public void newUser()
	{
		user = new UserEntity();
		user.setActive(true);
		user.setRole(Role.ADMIN);
		// AUTOMATICO - já associa
		user.setSchoolId(getLoggedSchoolId());
		user.setAcademicYearId(getCurrentAcademicYearId());
	}

	public String save()
	{
		try
		{
			Long schoolId = getLoggedSchoolId();
			Long academicYearId = getCurrentAcademicYearId();

			// FORÇA associação automática - nunca vem do form
			user.setSchoolId(schoolId);
			if (user.getAcademicYearId() == null)
			{
				user.setAcademicYearId(academicYearId);
			}
			user.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now());

			if (user.getName() == null || user.getName().trim().isEmpty())
			{
				errorMessage("Informe o nome.");
				return null;
			}
			if (user.getUsername() == null || user.getUsername().trim().isEmpty())
			{
				errorMessage("Informe o username.");
				return null;
			}
			if (user.getId() == null && (user.getPassword() == null || user.getPassword().trim().isEmpty()))
			{
				errorMessage("Informe a senha.");
				return null;
			}
			if (user.getRole() == null)
			{
				errorMessage("Selecione o perfil.");
				return null;
			}

			// Verifica duplicado ignorando ele mesmo na edição
			boolean exists;
			if (user.getId() == null)
			{
				exists = userService.existsByUsernameAndSchoolId(user.getUsername(), schoolId);
			} else
			{
				exists = userService.existsByUsernameAndSchoolIdAndIdNot(user.getUsername(), schoolId, user.getId());
			}

			if (exists)
			{
				errorMessage("Username '" + user.getUsername() + "' já existe nesta escola.");
				return null;
			}

			userService.save(user);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Utilizador gravado com sucesso."));

			newUser();
			loadUsers();
			return null; // fica na mesma página

		} catch (Exception e)
		{
			e.printStackTrace();
			errorMessage("Erro: " + e.getMessage());
			return null;
		}
	}

	// Pega school_id da sessão logada - NUNCA hardcoded 1L
	private Long getLoggedSchoolId()
	{
		try
		{
			Object loginBean = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loginBean");
			if (loginBean != null)
			{
				// via reflection para não depender da classe
				return (Long) loginBean.getClass().getMethod("getLoggedSchoolId").invoke(loginBean);
			}
			// fallback se ainda usa loggedSchool
			Object school = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("loggedSchool");
			if (school != null)
			{
				return (Long) school.getClass().getMethod("getId").invoke(school);
			}
		} catch (Exception ex)
		{
			System.out.println("Falha ao pegar schoolId da sessão, usando 1L fallback: " + ex.getMessage());
		}
		return 1L;
	}

//	private Long getCurrentAcademicYearId()
//	{
//		try
//		{
//			Object academicYearBean = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("academicYearBean");
//			// tenta pegar ano ativo - implementa no seu AcademicYearBean um
//			// getActiveYearId()
//		} catch (Exception e)
//		{
//		}
//		return currentAcademicYearId;
//	}

	public void selectForEdit(UserEntity u)
	{
		this.user = u;
	}

	public void selectForDelete(UserEntity u)
	{
		this.selectedUser = u;
	}

	public void delete()
	{
		try
		{
			userService.delete(selectedUser.getId());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Utilizador eliminado."));
			loadUsers();
		} catch (Exception e)
		{
			errorMessage("Erro ao eliminar: " + e.getMessage());
		}
	}

	private void errorMessage(String message)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", message));
	}

	public UserEntity getUser()
	{
		return user;
	}

	public void setUser(UserEntity user)
	{
		this.user = user;
	}

	public List<UserEntity> getUsers()
	{
		return users;
	}

	public UserEntity getSelectedUser()
	{
		return selectedUser;
	}

	public void setSelectedUser(UserEntity selectedUser)
	{
		this.selectedUser = selectedUser;
	}

	public Long getCurrentAcademicYearId()
	{
		return currentAcademicYearId;
	}

	public void setCurrentAcademicYearId(Long currentAcademicYearId)
	{
		this.currentAcademicYearId = currentAcademicYearId;
		loadUsers();
	}

	public Role[] getRoles()
	{
		return Role.values();
	}
}