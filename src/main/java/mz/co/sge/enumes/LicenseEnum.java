package mz.co.sge.enumes;

public final class LicenseEnum
{

	private LicenseEnum()
	{
	}

	public enum LicenseType
	{
		MONTHLY, ANNUAL, TRIENNIAL, LIFETIME, TRIAL
	}

	public enum LicenseStatus
	{
		AVAILABLE, ACTIVE, EXPIRED, CANCELLED
	}
}