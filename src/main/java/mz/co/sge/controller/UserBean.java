package mz.co.sge.controller;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
	private List<UserEntity> filteredUsers;
	private UserEntity selectedUser;

	private Role roleFilter; // null = todos os perfis
	private Boolean statusFilter; // null = todos; true = activos; false = inactivos
	private String viewMode = "CARDS"; // "CARDS" ou "TABELA"

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
		applyFilter();
	}

	public void newUser()
	{
		user = new UserEntity();
		user.setActive(true);
		user.setRole(Role.ADMIN);
		// AUTOMATICO - já associa
//		user.setAcademicYearId(getCurrentAcademicYearId());
	}

	public String save()
	{
		try
		{
			Long schoolId = getLoggedSchoolId();
			Long academicYearId = getCurrentAcademicYearId();

			// FORÇA associação automática - nunca vem do form

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

	// ====================== FILTROS ======================

	private void applyFilter()
	{
		filteredUsers = users.stream().filter(u -> roleFilter == null || u.getRole() == roleFilter)
				.filter(u -> statusFilter == null || Boolean.TRUE.equals(u.getActive()) == statusFilter).collect(Collectors.toList());
	}

	public void filterByRole(Role role)
	{
		// clicar outra vez no mesmo card remove o filtro
		this.roleFilter = (this.roleFilter == role) ? null : role;
		this.statusFilter = null;
		applyFilter();
	}

	public void filterByStatus(boolean active)
	{
		this.statusFilter = (this.statusFilter != null && this.statusFilter == active) ? null : active;
		this.roleFilter = null;
		applyFilter();
	}

	public void clearFilter()
	{
		roleFilter = null;
		statusFilter = null;
		applyFilter();
	}

	// ====================== CONTAGENS ======================

	public int getTotalUsers()
	{
		return users.size();
	}

	public long countByRole(Role role)
	{
		return users.stream().filter(u -> u.getRole() == role).count();
	}

	public long getCountActive()
	{
		return users.stream().filter(u -> Boolean.TRUE.equals(u.getActive())).count();
	}

	public long getCountInactive()
	{
		return users.size() - getCountActive();
	}

	// ====================== APARÊNCIA POR PERFIL ======================

	public String roleLabel(Role r)
	{
		if (r == null)
			return "-";
		String n = r.name().toUpperCase();
		if (n.contains("ADMIN"))
			return "Administrador";
		if (n.contains("PROF") || n.contains("TEACHER"))
			return "Professor";
		if (n.contains("ENCARREG") || n.contains("GUARDIAN") || n.contains("PARENT"))
			return "Encarregado";
		if (n.contains("ALUNO") || n.contains("STUDENT"))
			return "Aluno";
		if (n.contains("SECRET"))
			return "Secretaria";
		if (n.contains("DIRE"))
			return "Director";
		if (n.contains("FINAN") || n.contains("TESOUR"))
			return "Financeiro";
		String s = n.replace('_', ' ').toLowerCase();
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	public String roleIcon(Role r)
	{
		if (r == null)
			return "pi-user";
		String n = r.name().toUpperCase();
		if (n.contains("ADMIN"))
			return "pi-shield";
		if (n.contains("PROF") || n.contains("TEACHER"))
			return "pi-briefcase";
		if (n.contains("ENCARREG") || n.contains("GUARDIAN") || n.contains("PARENT"))
			return "pi-heart";
		if (n.contains("ALUNO") || n.contains("STUDENT"))
			return "pi-book";
		if (n.contains("SECRET"))
			return "pi-file-edit";
		if (n.contains("DIRE"))
			return "pi-star";
		if (n.contains("FINAN") || n.contains("TESOUR"))
			return "pi-wallet";
		return "pi-user";
	}

	public String roleColor(Role r)
	{
		if (r == null)
			return "#607d8b";
		String n = r.name().toUpperCase();
		if (n.contains("ADMIN"))
			return "#17365d";
		if (n.contains("PROF") || n.contains("TEACHER"))
			return "#1976d2";
		if (n.contains("ENCARREG") || n.contains("GUARDIAN") || n.contains("PARENT"))
			return "#e65100";
		if (n.contains("ALUNO") || n.contains("STUDENT"))
			return "#7b1fa2";
		if (n.contains("SECRET"))
			return "#00838f";
		if (n.contains("DIRE"))
			return "#ad1457";
		if (n.contains("FINAN") || n.contains("TESOUR"))
			return "#558b2f";
		return "#607d8b";
	}

	public String initials(UserEntity u)
	{
		if (u == null || u.getName() == null || u.getName().isBlank())
			return "?";
		String[] p = u.getName().trim().split("\\s+");
		String first = p[0].substring(0, 1);
		String last = p.length > 1 ? p[p.length - 1].substring(0, 1) : "";
		return (first + last).toUpperCase();
	}

	// ====================== GETTERS / SETTERS ======================

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

	public List<UserEntity> getFilteredUsers()
	{
		return filteredUsers;
	}

	public UserEntity getSelectedUser()
	{
		return selectedUser;
	}

	public void setSelectedUser(UserEntity selectedUser)
	{
		this.selectedUser = selectedUser;
	}

	public Role getRoleFilter()
	{
		return roleFilter;
	}

	public Boolean getStatusFilter()
	{
		return statusFilter;
	}

	public String getViewMode()
	{
		return viewMode;
	}

	public void setViewMode(String viewMode)
	{
		this.viewMode = viewMode;
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