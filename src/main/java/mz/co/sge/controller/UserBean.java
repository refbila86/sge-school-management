package mz.co.sge.controller;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import mz.co.sge.entity.UserEntity;
import mz.co.sge.enumes.Role;
import mz.co.sge.service.UsersService;

@Component("userBean")
@ViewScoped
public class UserBean implements Serializable
{

	private static final long serialVersionUID = 1L;

	private final UsersService userService;

	private UserEntity user;

	private List<UserEntity> users;

	private UserEntity selectedUser;

	public UserBean(UsersService userService)
	{
		this.userService = userService;
		newUser();
		loadUsers();
	}

	/**
	 * Loads all users of the school.
	 *
	 * We are temporarily using school_id = 1. Later this will come from the
	 * session/login.
	 */
	public void loadUsers()
	{
		Long schoolId = 1L;
		users = userService.findAllBySchool(schoolId);
	}

	/**
	 * Prepares a new user.
	 */
	public void newUser()
	{
		user = new UserEntity();
		user.setActive(true);
		user.setRole(Role.ADMIN);
	}

	public String save()
	{
		try
		{
			Long schoolId = 1L;
			user.setSchoolId(schoolId);
			user.setCreatedAt(LocalDateTime.now());

			if (user.getName() == null || user.getName().trim().isEmpty())
			{
				errorMessage("Please enter the user's name.");
				return null;
			}

			if (user.getUsername() == null || user.getUsername().trim().isEmpty())
			{
				errorMessage("Please enter the username.");
				return null;
			}

			if (user.getPassword() == null || user.getPassword().trim().isEmpty())
			{
				errorMessage("Please enter the password.");
				return null;
			}

			if (user.getRole() == null)
			{
				errorMessage("Please select the user's role.");
				return null;
			}

			if (userService.usernameExists(user.getUsername(), schoolId))
			{
				errorMessage("The username '" + user.getUsername() + "' already exists in this school.");
				return null;
			}

			userService.save(user);

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "User registered successfully."));

			newUser();

			return "/pages/dashboard/dashboard.xhtml?faces-redirect=true";

		} catch (Exception e)
		{
			e.printStackTrace();
			errorMessage("Error registering user: " + e.getMessage());
			return null;
		}
	}

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
			errorMessage("Erro ao eliminar utilizador: " + e.getMessage());
		}
	}

	public UserEntity getSelectedUser()
	{
		return selectedUser;
	}

	private void errorMessage(String message)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
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

	public Role[] getRoles()
	{
		return Role.values();
	}
}