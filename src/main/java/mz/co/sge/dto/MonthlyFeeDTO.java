package mz.co.sge.dto;

import java.math.BigDecimal;
import java.util.Date;

public class MonthlyFeeDTO
{

	private int month;
	private String monthLabel;
	private int year;
	private String reference;
	private BigDecimal amount;
	private Date dueDate;

	public int getMonth()
	{
		return month;
	}

	public void setMonth(int m)
	{
		month = m;
	}

	public String getMonthLabel()
	{
		return monthLabel;
	}

	public void setMonthLabel(String l)
	{
		monthLabel = l;
	}

	public int getYear()
	{
		return year;
	}

	public void setYear(int y)
	{
		year = y;
	}

	public String getReference()
	{
		return reference;
	}

	public void setReference(String r)
	{
		reference = r;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public void setAmount(BigDecimal a)
	{
		amount = a;
	}

	public Date getDueDate()
	{
		return dueDate;
	}

	public void setDueDate(Date d)
	{
		dueDate = d;
	}

}
