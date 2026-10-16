package mz.co.sge.dto;

import java.io.Serializable;
import java.util.Date;

public class PaymentDTO implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String reference;
	private String amount;
	private String status;
	private String description;
	private String method;
	private String monthLabel;
	private Date paymentDate;
	private boolean selected;

	public String getReference()
	{
		return reference;
	}

	public void setReference(String r)
	{
		reference = r;
	}

	public String getAmount()
	{
		return amount;
	}

	public void setAmount(String a)
	{
		amount = a;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String s)
	{
		status = s;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String d)
	{
		description = d;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String m)
	{
		method = m;
	}

	public String getMonthLabel()
	{
		return monthLabel;
	}

	public void setMonthLabel(String m)
	{
		monthLabel = m;
	}

	public Date getPaymentDate()
	{
		return paymentDate;
	}

	public void setPaymentDate(Date d)
	{
		paymentDate = d;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean s)
	{
		selected = s;
	}

	public boolean getSelected()
	{
		return selected;
	}
}
 