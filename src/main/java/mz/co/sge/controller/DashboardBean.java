package mz.co.sge.controller;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component("dashboardBean")
@Scope("session")
public class DashboardBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	private boolean sidebarCollapsed = false;
	private String currentPage = "/pages/welcome.xhtml";
	private String activeMenu = "dashboard";
	private String pageTitle = "Dashboard";

	@Autowired
	private LoginBean loginBean;

	@Autowired
	private mz.co.sge.service.IStudentService studentService;
	
	@Autowired
	private MenuBean menuBean;

	@PostConstruct
	public void init()
	{
		try
		{
			menuBean.refreshCounts();
		} catch (Exception e)
		{
		}
	}

	public void toggleSidebar()
	{
		sidebarCollapsed = !sidebarCollapsed;
	}

	public void navigate(String page, String menu)
	{
		if (page == null || page.isBlank())
			return;
		String normalized = page.trim();
		if (normalized.startsWith("/pages/"))
			normalized = normalized.substring(7);
		if (normalized.endsWith(".xhtml"))
			normalized = normalized.substring(0, normalized.length() - 6);
		if (normalized.isBlank())
			return;

		this.currentPage = "/pages/" + normalized + ".xhtml";
		this.activeMenu = menu != null ? menu : "dashboard";
		this.pageTitle = toTitle(menu);

		// ATUALIZA CONTADORES SEMPRE QUE NAVEGA
		try
		{
			menuBean.refreshCounts();
		} catch (Exception e)
		{
		}
	}

	public void goToStudents()
	{
		navigate("student/student-list", "alunos");
	}

	public void goToGuardians()
	{
		navigate("guardians/guardian-list", "encarregados");
	}

	public void goToSchools()
	{
		navigate("schools/school-list", "escolas");
	}

	public void goToDashboard()
	{
		navigate("welcome", "dashboard");
	}

	private String toTitle(String menu)
	{
		if (menu == null)
			return "Dashboard";
		return switch (menu)
		{
		case "alunos" -> "Gestão de Alunos";
		case "estudantes" -> "Gestão de Alunos";
		case "encarregados" -> "Gestão de Encarregados";
		case "professores" -> "Professores";
		case "pagamentos" -> "Pagamentos";
		case "feeitems" -> "Items de Pagamento - " + loginBean.getLoggedAcademicYear().getYear() + " - " + loginBean.getLoggedSchool().getName();
		case "turmas" -> "Turmas";
		case "classes" -> "Classes";
		case "disciplinas" -> "Disciplinas";
		case "notas" -> "Lançamento de Notas";
		case "pautas" -> "Pautas";
		case "mensalidades" -> "Mensalidades";
		// case "pagamentos" -> "Pagamentos";
		case "users" -> "Utilizadores";
		case "backups" -> "Backups";
		case "licencas" -> "Licenças";
		case "escolas" -> "Escolas";
		case "ano" -> "Ano Académico";
		case "sms" -> "SMS / Emails";
		default -> "Dashboard";
		};
	}

	public boolean isActive(String menu)
	{
		return activeMenu.equals(menu);
	}

	private Long getCurrentSchoolId()
	{
		if (loginBean == null)
			return null;
		try
		{
			if (loginBean.getLoggedSchool() != null && loginBean.getLoggedSchool().getId() != null)
				return loginBean.getLoggedSchool().getId();
			return loginBean.getSchoolId();
		} catch (Exception e)
		{
			return null;
		}
	}

	private Long getCurrentAcademicYearId()
	{
		if (loginBean == null)
			return null;
		try
		{
			return loginBean.getAcademicYearId();
		} catch (Exception e)
		{
			return null;
		}
	}

	public int getTotalAlunos()
	{
		try
		{
			Long schoolId = getCurrentSchoolId();
			Long yearId = getCurrentAcademicYearId();
			if (schoolId != null && yearId != null && studentService != null)
				return (int) studentService.countBySchoolIdAndAcademicYearId(schoolId, yearId);
			if (schoolId != null && studentService != null)
				return (int) studentService.countBySchoolId(schoolId);
			if (yearId != null && studentService != null)
				return (int) studentService.countByAcademicYearId(yearId);
			if (studentService != null)
				return (int) studentService.countStudents();
		} catch (Exception e)
		{
		}
		return 0;
	}

	public String getTotalAlunosFormatted()
	{
		int total = getTotalAlunos();
		if (total >= 1000)
			return String.format("%.1fk", total / 1000.0).replace(",", ".");
		return String.valueOf(total);
	}

	public int getTotalAlunosCount()
	{
		return getTotalAlunos();
	}

	public long getTotalStudentsInCurrentYear()
	{
		return getTotalAlunos();
	}

	public String getTotalStudentsInCurrentYearFormatted()
	{
		return getTotalAlunosFormatted();
	}

	public long getTotalStudents()
	{
		return getTotalAlunos();
	}

	public String getTotalStudentsFormatted()
	{
		return getTotalAlunosFormatted();
	}

	public boolean isSidebarCollapsed()
	{
		return sidebarCollapsed;
	}

	public boolean getSidebarCollapsed()
	{
		return sidebarCollapsed;
	}

	public void setSidebarCollapsed(boolean b)
	{
		this.sidebarCollapsed = b;
	}

	public String getCurrentPage()
	{
		return currentPage;
	}

	public void setCurrentPage(String s)
	{
		this.currentPage = s;
	}

	public String getActiveMenu()
	{
		return activeMenu;
	}

	public void setActiveMenu(String s)
	{
		this.activeMenu = s;
	}

	public String getPageTitle()
	{
		return pageTitle;
	}

	public void setPageTitle(String s)
	{
		this.pageTitle = s;
	}
}