package mz.co.sge.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import mz.co.sge.entity.AcademicYearEntity;
import mz.co.sge.entity.LicenseEntity;
import mz.co.sge.entity.SchoolEntity;
import mz.co.sge.entity.UserEntity;
import mz.co.sge.service.IAcademicYearService;
import mz.co.sge.service.ILicenseService;
import mz.co.sge.service.SchoolsService;
import mz.co.sge.service.UsersService;

@Component("loginBean")
@SessionScope
public class LoginBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final UsersService userService;
	private final SchoolsService schoolService;
	private final IAcademicYearService academicYearService;

	private Long schoolId;
	private String username;
	private String password;
	private boolean rememberMe;
	private String selectedProfile = "ADMIN";

	private List<SchoolEntity> schools;
	private List<AcademicYearEntity> academicYears = new ArrayList<>();
	private Long academicYearId;

	private UserEntity loggedUser;
	private SchoolEntity loggedSchool;
	private AcademicYearEntity loggedAcademicYear;
	private String fullname;
	private String role;
	private boolean yearStep = false;

	@Autowired
	private ILicenseService licenseService;

	private LicenseEntity loggedLicense;

	public LoginBean(UsersService userService, SchoolsService schoolService, IAcademicYearService academicYearService)
	{
		this.userService = userService;
		this.schoolService = schoolService;
		this.academicYearService = academicYearService;
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
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Não foi possível carregar escolas.");
		}
	}

	public String goToYearStep()
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
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Utilizador obrigatório.");
				return null;
			}
			if (password == null || password.trim().isEmpty())
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Senha obrigatória.");
				return null;
			}

			UserEntity user = userService.authenticate(username.trim(), password.trim(), schoolId);
			if (user == null)
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Utilizador/senha inválidos.");
				return null;
			}

			String userRole = user.getRole() != null ? user.getRole().name() : "";
			if (!userRole.equals("ADMIN") && !userRole.equals(selectedProfile))
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Acesso negado!", "É " + userRole + " e não tem acesso à aba " + getProfileLabel(selectedProfile));
				return null;
			}

			this.academicYears = academicYearService.getAllAcademicYears();
			if (academicYears == null || academicYears.isEmpty())
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Nenhum ano académico.");
				return null;
			}

			Optional<AcademicYearEntity> active = academicYearService.getActiveAcademicYear();
			if (active.isPresent())
				academicYearId = active.get().getId();

			this.yearStep = true;
			return null;
		} catch (Exception e)
		{
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", e.getMessage());
			return null;
		}
	}

	public String backToCredentials()
	{
		this.yearStep = false;
		return null;
	}

	public String login()
	{
		try
		{
			if (schoolId == null || academicYearId == null)
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Selecione escola e ano.");
				return null;
			}

			var schoolOpt = schoolService.findById(schoolId);
			if (schoolOpt.isEmpty())
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Escola não encontrada.");
				return null;
			}
			SchoolEntity school = schoolOpt.get();

			Optional<AcademicYearEntity> ayOpt = academicYearService.getAcademicYearById(academicYearId);
			if (ayOpt.isEmpty())
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Ano não encontrado.");
				return null;
			}
			AcademicYearEntity academicYear = ayOpt.get();

			UserEntity user = userService.authenticate(username.trim(), password.trim(), schoolId);
			if (user == null)
			{
				addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Utilizador/senha inválidos.");
				return null;
			}

			String userRole = user.getRole() != null ? user.getRole().name() : "";
			var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + userRole));
			var authToken = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
			SecurityContextHolder.getContext().setAuthentication(authToken);

			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
			HttpSession session = (HttpSession) ec.getSession(true);
			session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
			session.setAttribute("usuarioLogado", user);
			session.setAttribute("escolaLogada", school);
			session.setAttribute("schoolId", schoolId);
			session.setAttribute("academicYearId", academicYearId);

			this.loggedUser = user;
			this.loggedSchool = school;
			this.loggedAcademicYear = academicYear;
			this.fullname = user.getName();
			this.role = userRole;
			this.loggedLicense = null; // força recarregar
			this.password = null;

			return "/pages/dashboard.xhtml?faces-redirect=true";
		} catch (Exception e)
		{
			e.printStackTrace();
			addMessage(FacesMessage.SEVERITY_ERROR, "Erro!", e.getMessage());
			return null;
		}
	}

	public String logout()
	{
		try
		{
			SecurityContextHolder.clearContext();
			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
			HttpSession s = (HttpSession) ec.getSession(false);
			if (s != null)
				s.invalidate();
		} catch (Exception e)
		{
		} finally
		{
			loggedUser = null;
			loggedSchool = null;
			loggedAcademicYear = null;
			loggedLicense = null;
			username = null;
			password = null;
			fullname = null;
			role = null;
			schoolId = null;
			academicYearId = null;
			selectedProfile = "ADMIN";
			yearStep = false;
		}
		return "/login.xhtml?faces-redirect=true";
	}

	private void addMessage(FacesMessage.Severity s, String sum, String det)
	{
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(s, sum, det));
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

	// ===== LICENÇA - AGORA TRAZ OBJETO E NÃO ID =====
	public LicenseEntity getLoggedLicense()
	{
		if (loggedLicense == null && loggedSchool != null && loggedSchool.getLicenseId() != null)
		{
			try
			{
				loggedLicense = licenseService.getById(loggedSchool.getLicenseId()).orElse(null);
			} catch (Exception e)
			{
				loggedLicense = null;
			}
		}
		return loggedLicense;
	}

	public String getLicenseCode()
	{
		LicenseEntity lic = getLoggedLicense();
		return lic != null ? lic.getCode() : "LIC-" + (loggedSchool != null ? loggedSchool.getLicenseId() : "N/A");
	}

	public String getLicenseStatus()
	{
		LicenseEntity lic = getLoggedLicense();
		return lic != null && lic.getStatus() != null ? lic.getStatus().name() : "ATIVA";
	}

	public String getLicenseType()
	{
		LicenseEntity lic = getLoggedLicense();
		return lic != null && lic.getType() != null ? lic.getType().name() : "";
	}

	public String getLicenseExpiry()
	{
		LicenseEntity lic = getLoggedLicense();
		if (lic == null)
			return "";
		LocalDate exp = lic.getExpirationDate() != null ? lic.getExpirationDate() : lic.getEndDate();
		if (exp == null)
			return "";
		return exp.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}
	
 

	public long getLicenseDaysRemaining()
	{
		LicenseEntity lic = getLoggedLicense();
		return lic != null ? lic.getDaysRemaining() : 0;
	}

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

	public boolean isYearStep()
	{
		return yearStep;
	}

	public void setYearStep(boolean yearStep)
	{
		this.yearStep = yearStep;
	}

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

	public SchoolEntity getLoggedSchool()
	{
		return loggedSchool;
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

	public String getRole()
	{
		return role;
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

	public List<AcademicYearEntity> getAcademicYears()
	{
		return academicYears;
	}

	public Long getAcademicYearId()
	{
		return academicYearId;
	}

	public void setAcademicYearId(Long academicYearId)
	{
		this.academicYearId = academicYearId;
	}

	public AcademicYearEntity getLoggedAcademicYear()
	{
		return loggedAcademicYear;
	}

	public String getAcademicYearLabel()
	{
		return loggedAcademicYear != null ? loggedAcademicYear.getYear() : "";
	}

	public Long getLoggedUserId()
	{
		return loggedUser.getId();
	}
}