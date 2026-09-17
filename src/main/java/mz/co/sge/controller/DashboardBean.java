package mz.co.sge.controller;

import java.io.Serializable;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.faces.context.FacesContext;


@Component("dashboardBean") // Expõe o bean para o JSF como #{loginBean}
@Scope("session")       // Mantém o bean vivo durante toda a sessão do utilizador
public class DashboardBean implements Serializable
{

	private static final long serialVersionUID = 1L;

	// Página inicial padrão (Welcome)
	private String currentPage = "/pages/dashboard/welcome.xhtml";
	
	private boolean sidebarVisible = true;

	// Injeção do LoginBean para obter dados do utilizador se necessário
	@Autowired // Substitui o @Inject
	private LoginBean loginBean;

	// =========================================================
	// NAVEGAÇÃO GENÉRICA
	// =========================================================

	/**
	 * Método genérico para carregar qualquer página pelo nome. Ex:
	 * loadPage("students") carrega "/pages/dashboard/students.xhtml"
	 */
	
	public Date getCurrentDate() {
	    return new Date();
	}
	public void loadPage(String page)
	{
		this.currentPage = "/pages/dashboard/" + page + ".xhtml";
	}

	public void goHome()
	{
		this.currentPage = "/pages/dashboard/welcome.xhtml";
	}

	public void toggleSidebar()
	{
		this.sidebarVisible = !this.sidebarVisible;
	}

	public String logout()
	{
		// Delega o logout ao LoginBean para garantir que a sessão seja limpa
		// corretamente
		return loginBean.logout();
	}


	// **************PRODUTO*************************************************************
//	public void loadProduct() {
//		FacesContext context = FacesContext.getCurrentInstance();
//		ProductBean productBean = context.getApplication().evaluateExpressionGet(context, "#{productBean}",
//				ProductBean.class);
//		productBean.getListProduct();
//		this.setCurrentPage("/pages/product.xhtml");
//	}
	
	// =========================================================
	// ALUNOS
	// =========================================================
	public void loadStudents()
	{
		FacesContext context = FacesContext.getCurrentInstance();
		this.currentPage = "/pages/dashboard/students.xhtml";
	}

	public void loadStudentInclude()
	{
		this.currentPage = "/pages/dashboard/student-include.xhtml";
	}

	public void backStudentInclude()
	{
		this.currentPage = "/pages/dashboard/students.xhtml";
	}

	public void cancelStudentInclude()
	{
		this.currentPage = "/pages/dashboard/students.xhtml";
	}

	// =========================================================
	// PROFESSORES
	// =========================================================
	public void loadTeachers()
	{
		this.currentPage = "/pages/dashboard/teachers.xhtml";
	}

	public void loadTeacherInclude()
	{
		this.currentPage = "/pages/dashboard/teacher-include.xhtml";
	}

	public void backTeacherInclude()
	{
		this.currentPage = "/pages/dashboard/teachers.xhtml";
	}

	public void cancelTeacherInclude()
	{
		this.currentPage = "/pages/dashboard/teachers.xhtml";
	}

	// =========================================================
	// TURMAS E DISCIPLINAS
	// =========================================================
	public void loadClasses()
	{
		this.currentPage = "/pages/dashboard/classes.xhtml";
	}

	public void loadClassInclude()
	{
		this.currentPage = "/pages/dashboard/class-include.xhtml";
	}

	public void backClassInclude()
	{
		this.currentPage = "/pages/dashboard/classes.xhtml";
	}

	// =========================================================
	// FINANCEIRO / MENSALIDADES
	// =========================================================
	public void loadFees()
	{
		this.currentPage = "/pages/dashboard/fees.xhtml";
	}

	public void loadFeeInclude()
	{
		this.currentPage = "/pages/dashboard/fee-include.xhtml";
	}

	public void backFeeInclude()
	{
		this.currentPage = "/pages/dashboard/fees.xhtml";
	}

	public void cancelFeeInclude()
	{
		this.currentPage = "/pages/dashboard/fees.xhtml";
	}

	// =========================================================
	// MATRÍCULAS
	// =========================================================
	public void loadEnrollments()
	{
		this.currentPage = "/pages/dashboard/enrollments.xhtml";
	}

	public void loadEnrollmentInclude()
	{
		this.currentPage = "/pages/dashboard/enrollment-include.xhtml";
	}

	public void backEnrollmentInclude()
	{
		this.currentPage = "/pages/dashboard/enrollments.xhtml";
	}

	// =========================================================
	// UTILIZADORES DO SISTEMA
	// =========================================================
	public void loadUsers()
	{
		this.currentPage = "/pages/dashboard/users.xhtml";
	}

	public void loadUserInclude()
	{
		this.currentPage = "/pages/dashboard/user-include.xhtml";
	}

	public void backUserInclude()
	{
		this.currentPage = "/pages/dashboard/users.xhtml";
	}

	// =========================================================
	// GETTERS E SETTERS
	// =========================================================

	public String getCurrentPage()
	{
		return currentPage;
	}

	public void setCurrentPage(String currentPage)
	{
		this.currentPage = currentPage;
	}

	public boolean isSidebarVisible()
	{
		return sidebarVisible;
	}

	public void setSidebarVisible(boolean sidebarVisible)
	{
		this.sidebarVisible = sidebarVisible;
	}

	public LoginBean getLoginBean()
	{
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean)
	{
		this.loginBean = loginBean;
	}
}