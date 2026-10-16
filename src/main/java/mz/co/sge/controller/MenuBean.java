package mz.co.sge.controller;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import mz.co.sge.service.IGuardianService;

@Component("menuBean")
@Scope("session")
public class MenuBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Autowired
	private transient IGuardianService guardianService;

	private long guardianCount = 0;

	@PostConstruct
	public void init()
	{
		refreshCounts();
	}

	public void refreshCounts()
	{
		try
		{
			this.guardianCount = guardianService != null ? guardianService.countGuardians() : 0;
		} catch (Exception e)
		{
			this.guardianCount = 0;
		}
	}

	public long getGuardianCount()
	{
		return guardianCount;
	}

	public String getGuardianLabel()
	{
		return "Encarregados (" + guardianCount + ")";
	}
}