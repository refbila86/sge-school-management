package mz.co.sge.util;

import java.util.Iterator;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;

public class HeadWarningFilter implements PhaseListener
{
	@Override
	public PhaseId getPhaseId()
	{
		return PhaseId.RENDER_RESPONSE;
	}

	@Override
	public void beforePhase(PhaseEvent e)
	{
		FacesContext ctx = e.getFacesContext();
		Iterator<FacesMessage> it = ctx.getMessages(null);
		while (it.hasNext())
		{
			FacesMessage m = it.next();
			if (m.getSummary() != null && m.getSummary().contains("target of 'head'"))
				it.remove();
		}
	}

	@Override
	public void afterPhase(PhaseEvent e)
	{
	}
}