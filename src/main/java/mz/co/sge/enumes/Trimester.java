package mz.co.sge.enumes;

public enum Trimester
{
	T1(1, "1º Trimestre"), T2(2, "2º Trimestre"), T3(3, "3º Trimestre"), T4(4, "4º Trimestre");

	private int code;
	private String label;

	Trimester(int code, String label)
	{
		this.code = code;
		this.label = label;
	}

	public int getCode()
	{
		return code;
	}

	public String getLabel()
	{
		return label;
	}
}