package mz.co.sge.enumes;

public enum EnrollmentType
{
	FIRST_ENROLLMENT, // Paga INSCRICAO + MATRICULA
	RENEWAL; // Paga só RENOVACAO

	public static EnrollmentType fromTotalPrevious(long total)
	{
		return total == 0 ? FIRST_ENROLLMENT : RENEWAL;
	}
}