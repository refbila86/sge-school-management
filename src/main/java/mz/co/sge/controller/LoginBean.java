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

	private List<SchoolEntity> schools;

	// DADOS DO LOGADO - AGORA COM ESCOLA COMPLETA
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

	/**
	 * Autentica o utilizador considerando a escola selecionada. AGORA BUSCA A
	 * ESCOLA NA TABELA school E GUARDA NA SESSÃO
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

			// 1. Busca a escola primeiro para validar e exibir no welcome
			var schoolOpt = schoolService.findById(schoolId);
			if (schoolOpt.isEmpty())
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Escola selecionada não encontrada.");
				return null;
			}
			SchoolEntity school = schoolOpt.get();
			if (school.getActive() == null || !school.getActive())
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Escola inativa.");
				return null;
			}

			// 2. Autentica o utilizador
			UserEntity user = userService.authenticate(username.trim(), password.trim(), schoolId);

			if (user == null)
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Utilizador, senha inválidos ou conta inativa.");
				return null;
			}

			// 3. Spring Security
			var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + (user.getRole() != null ? user.getRole().name() : "USER")));
			var authToken = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
			SecurityContextHolder.getContext().setAuthentication(authToken);

			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			HttpSession session = (HttpSession) externalContext.getSession(true);
			session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
			session.setAttribute("usuarioLogado", user);
			session.setAttribute("escolaLogada", school); // NOVO: guarda objeto completo
			session.setAttribute("schoolId", schoolId);
			session.setAttribute("schoolName", school.getName());

			// 4. Guarda no Bean de sessão para usar no welcome.xhtml
			this.loggedUser = user;
			this.loggedSchool = school;
			this.fullname = user.getName();
			this.role = (user.getRole() != null) ? user.getRole().name() : "";
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
	{
		try
		{
			SecurityContextHolder.clearContext();
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			HttpSession session = (HttpSession) externalContext.getSession(false);
			if (session != null)
			{
				session.invalidate();
			}
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
		}
		return "/login.xhtml?faces-redirect=true";
	}

	private void addMessage(FacesMessage.Severity severity, String summary, String detail)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
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

	// NOVOS GETTERS PARA A ESCOLA
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
}
