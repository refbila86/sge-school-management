package mz.co.sge.controller;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class DashboardBean implements Serializable
{

	private boolean sidebarCollapsed = false;
	private String currentPage = "/pages/welcome.xhtml";
	private String activeMenu = "dashboard";
	private String pageTitle = "Dashboard";

	public void toggleSidebar()
	{
		sidebarCollapsed = !sidebarCollapsed;
	}

	public void navigate(String page, String menu)
	{
		// page vem sem .xhtml
		this.currentPage = "/pages/" + page + ".xhtml";
		this.activeMenu = menu;
		this.pageTitle = toTitle(menu);
	}

	private String toTitle(String menu)
	{
		if (menu == null)
			return "Dashboard";
		return switch (menu)
		{
		case "alunos" -> "Alunos";
		case "professores" -> "Professores";
		case "turmas" -> "Turmas";
		case "classes" -> "Classes";
		case "disciplinas" -> "Disciplinas";
		case "notas" -> "Lançamento de Notas";
		case "pautas" -> "Pautas";
		case "mensalidades" -> "Mensalidades";
		case "pagamentos" -> "Pagamentos";
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

	// GETTERS
	public boolean isSidebarCollapsed()
	{
		return sidebarCollapsed;
	}

	public String getCurrentPage()
	{
		return currentPage;
	}

	public String getActiveMenu()
	{
		return activeMenu;
	}

	public String getPageTitle()
	{
		return pageTitle;
	}
}