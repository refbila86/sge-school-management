package mz.co.sge.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import java.io.Serializable;

@Component("dashboardBean")
@SessionScope
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String currentPage;
    private String activeMenu = "dashboard";
    private boolean sidebarCollapsed = false;

    @PostConstruct
    public void init() {
        this.currentPage = "/pages/welcome.xhtml"; // onde teu arquivo está
        this.activeMenu = "dashboard";
    }

    public void navigate(String page, String menuId) {
        this.currentPage = "/pages/" + page + ".xhtml";
        this.activeMenu = menuId;
    }
    
    public void navigate(String page) {
        // overload para compatibilidade
        this.currentPage = "/pages/" + page + ".xhtml";
        // extrai menu do path
        if(page.contains("/")) this.activeMenu = page.split("/")[0];
        else this.activeMenu = page;
    }

    public void toggleSidebar() {
        this.sidebarCollapsed = !this.sidebarCollapsed;
    }

    // GETTERS / SETTERS
    public String getCurrentPage() { return currentPage; }
    public void setCurrentPage(String currentPage) { this.currentPage = currentPage; }
    public String getActiveMenu() { return activeMenu; }
    public void setActiveMenu(String activeMenu) { this.activeMenu = activeMenu; }
    public boolean isSidebarCollapsed() { return sidebarCollapsed; }
    public void setSidebarCollapsed(boolean sidebarCollapsed) { this.sidebarCollapsed = sidebarCollapsed; }
    
    public boolean isActive(String menu) {
        return activeMenu != null && activeMenu.equals(menu);
    }
}
