package mz.co.sge.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.entity.UserEntity;
import mz.co.sge.service.SchoolsService;
import mz.co.sge.service.UsersService;

@Component("loginBean")
@SessionScope
public class LoginBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final UsersService userService;
	private final SchoolsService schoolService;

	private Long schoolId;
	private String username;
	private String password;
	private boolean rememberMe;
	private String selectedProfile = "ADMIN"; // ADMIN, TEACHER, GUARDIAN

	private List<SchoolEntity> schools;

	// DADOS DO LOGADO
	private UserEntity loggedUser;
	private SchoolEntity loggedSchool;
	private String fullname;
	private String role;
	private boolean logged;

	public LoginBean(UsersService userService, SchoolsService schoolService)
	{
		this.userService = userService;
		this.schoolService = schoolService;
	}

	@PostConstruct
	public void init()
	{
		schools = new ArrayList<>();
		schoolList();
	}

	public void schoolList()
	{
		try
		{
			this.schools = schoolService.findAllActive();
		} catch (Exception e)
		{
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Não foi possível carregar a lista de escolas.");
		}
	}

	public String login()
	{
		try
		{
			if (schoolId == null)
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Selecione a escola.");
				return null;
			}
			if (username == null || username.trim().isEmpty())
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Informe o utilizador.");
				return null;
			}
			if (password == null || password.trim().isEmpty())
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Informe a senha.");
				return null;
			}
			if (selectedProfile == null)
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Selecione o perfil de acesso.");
				return null;
			}

			var schoolOpt = schoolService.findById(schoolId);
			if (schoolOpt.isEmpty())
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Escola não encontrada.");
				return null;
			}
			SchoolEntity school = schoolOpt.get();
			if (school.getActive() == null || !school.getActive())
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Escola inativa.");
				return null;
			}

			UserEntity user = userService.authenticate(username.trim(), password.trim(), schoolId);

			if (user == null)
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Utilizador, senha inválidos ou conta inativa.");
				return null;
			}

			// VALIDA SE O PERFIL ESCOLHIDO CORRESPONDE AO ROLE
			// ADMIN pode entrar em qualquer aba (facilita testes)
			String userRole = user.getRole() != null ? user.getRole().name() : "";
			if (!userRole.equals("ADMIN") && !userRole.equals(selectedProfile))
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Acesso negado!",
						"Este utilizador é " + userRole + " e não tem acesso à aba " + getProfileLabel(selectedProfile) + ".");
				return null;
			}

			var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + userRole));
			var authToken = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
			SecurityContextHolder.getContext().setAuthentication(authToken);

			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			HttpSession session = (HttpSession) externalContext.getSession(true);
			session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
			session.setAttribute("usuarioLogado", user);
			session.setAttribute("escolaLogada", school);
			session.setAttribute("schoolId", schoolId);
			session.setAttribute("schoolName", school.getName());
			session.setAttribute("selectedProfile", selectedProfile);

			this.loggedUser = user;
			this.loggedSchool = school;
			this.fullname = user.getName();
			this.role = userRole;
			this.password = null;
			this.logged = true;

			return "/pages/dashboard.xhtml?faces-redirect=true";

		} catch (Exception e)
		{
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Erro ao autenticar: " + e.getMessage());
			return null;
		}
	}

	public String logout()
	{ /* ... mantém igual ao teu ... */
		try
		{
			SecurityContextHolder.clearContext();
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			HttpSession session = (HttpSession) externalContext.getSession(false);
			if (session != null)
				session.invalidate();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			this.loggedUser = null;
			this.loggedSchool = null;
			this.logged = false;
			this.username = null;
			this.password = null;
			this.fullname = null;
			this.role = null;
			this.schoolId = null;
			this.selectedProfile = "ADMIN";
		}
		return "/login.xhtml?faces-redirect=true";
	}

	private void addMessage(FacesMessage.Severity severity, String summary, String detail)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
	}

	public String getProfileLabel(String profile)
	{
		if (profile == null)
			return "";
		switch (profile)
		{
		case "ADMIN":
			return "Administração";
		case "TEACHER":
			return "Professor";
		case "GUARDIAN":
			return "Encarregado";
		default:
			return profile;
		}
	}

	// HELPERS PARA O MENU
	public boolean isAdmin()
	{
		return "ADMIN".equals(role);
	}

	public boolean isTeacher()
	{
		return "TEACHER".equals(role);
	}

	public boolean isGuardian()
	{
		return "GUARDIAN".equals(role);
	}

	// GETTERS E SETTERS
	public Long getSchoolId()
	{
		return schoolId;
	}

	public void setSchoolId(Long schoolId)
	{
		this.schoolId = schoolId;
	}

	public List<SchoolEntity> getSchools()
	{
		if (this.schools == null || this.schools.isEmpty())
			schoolList();
		return schools;
	}

	public void setSchools(List<SchoolEntity> schools)
	{
		this.schools = schools;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public UserEntity getLoggedUser()
	{
		return loggedUser;
	}

	public void setLoggedUser(UserEntity loggedUser)
	{
		this.loggedUser = loggedUser;
	}

	public SchoolEntity getLoggedSchool()
	{
		return loggedSchool;
	}

	public void setLoggedSchool(SchoolEntity loggedSchool)
	{
		this.loggedSchool = loggedSchool;
	}

	public String getSchoolName()
	{
		return loggedSchool != null ? loggedSchool.getName() : "";
	}

	public String getSchoolCode()
	{
		return loggedSchool != null ? loggedSchool.getCode() : "";
	}

	public String getFullname()
	{
		return fullname;
	}

	public void setFullname(String fullname)
	{
		this.fullname = fullname;
	}

	public String getRole()
	{
		return role;
	}

	public void setRole(String role)
	{
		this.role = role;
	}

	public boolean isLogged()
	{
		return logged;
	}

	public void setLogged(boolean logged)
	{
		this.logged = logged;
	}

	public boolean isRememberMe()
	{
		return rememberMe;
	}

	public void setRememberMe(boolean rememberMe)
	{
		this.rememberMe = rememberMe;
	}

	public String getSelectedProfile()
	{
		return selectedProfile;
	}

	public void setSelectedProfile(String selectedProfile)
	{
		this.selectedProfile = selectedProfile;
	}

	public String getProfileLabel()
	{
		return getProfileLabel(this.selectedProfile);
	}

	public String getRoleLabel()
	{
		return getProfileLabel(this.role);
	}
}