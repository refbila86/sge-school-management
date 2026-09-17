package mz.co.sge.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.entity.UserEntity;
import mz.co.sge.service.SchoolsService;
import mz.co.sge.service.UsersService;

@Component("loginBean")
@RequestScoped
public class LoginBean implements Serializable
{

	private static final long serialVersionUID = 1L;

	private final UsersService userService;
	private final SchoolsService schoolService; 

	private Long schoolId;
	private String username;
	private String password;
	private boolean rememberMe;

	private List<SchoolEntity> schools;

	private UserEntity loggedUser;
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

	/**
	 * Autentica o utilizador considerando a escola selecionada.
	 */
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

			UserEntity user = userService.authenticate(username.trim(), password, schoolId);

			if (user == null)
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Utilizador, senha inválidos ou conta inativa.");
				return null;
			}

			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			jakarta.servlet.http.HttpServletRequest request = (jakarta.servlet.http.HttpServletRequest) externalContext.getRequest();

			request.changeSessionId();

			HttpSession session = (HttpSession) externalContext.getSession(true);
			session.setAttribute("usuarioLogado", user);
			session.setAttribute("schoolId", schoolId);

			this.loggedUser = user;
			this.fullname = user.getName();
			this.role = (user.getRole() != null) ? user.getRole().name() : "";
			this.password = null;
			this.logged = true;

			externalContext.getFlash().setKeepMessages(true);
			addMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Bem-vindo(a), " + this.fullname + "!");

			return "/pages/dashboard.xhtml?faces-redirect=true";

		} catch (Exception e)
		{
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Erro ao autenticar: " + e.getMessage());
			return null;
		}
	}

	public String loginWrong()
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

			// Autentica validando Username, Senha com BCrypt e SchoolId na NamedQuery
			UserEntity user = userService.authenticate(username.trim(), password, schoolId);

			if (user == null)
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Utilizador, senha inválidos ou conta inativa.");
				return null;
			}

			// Invalidação de sessão antiga para prevenir Session Fixation
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			HttpSession oldSession = (HttpSession) externalContext.getSession(false);
			if (oldSession != null)
			{
				oldSession.invalidate();
			}

			HttpSession session = (HttpSession) externalContext.getSession(true);
			session.setAttribute("usuarioLogado", user);
			session.setAttribute("schoolId", schoolId);

			this.loggedUser = user;
			this.fullname = user.getName();
			this.role = (user.getRole() != null) ? user.getRole().name() : "";
			this.password = null;
			this.logged = true;

			externalContext.getFlash().setKeepMessages(true);
			addMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Bem-vindo(a), " + this.fullname + "!");

			return "/pages/dashboard.xhtml?faces-redirect=true";

		} catch (Exception e)
		{
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Erro ao autenticar: " + e.getMessage());
			return null;
		}
	}

	public String logout()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		HttpSession session = (HttpSession) externalContext.getSession(false);

		if (session != null)
		{
			session.invalidate();
		}

		this.loggedUser = null;
		this.logged = false;
		this.username = null;
		this.password = null;
		this.fullname = null;
		this.role = null;
		this.schoolId = null;

		return "/login.xhtml?faces-redirect=true";
	}

	private void addMessage(FacesMessage.Severity severity, String summary, String detail)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
	}

	// =========================================================
	// GETTERS E SETTERS
	// =========================================================

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
		{
			schoolList();
		}
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

}