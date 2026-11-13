package mz.co.sge.enumes;

public enum MunicipalDistrict
{
	KAMPFUMO("KaMpfumo", "Urban District No. 1", "Polana Cimento, Alto Mae, Sommerschield, Coop"),
	NLHAMANKULU("Nlhamankulu / Chamanculo", "Urban District No. 2", "Malhangalene, Chamanculo, Malanga"),
	KAMAXAKENI("KaMaxakeni / Maxaquene", "Urban District No. 3", "Mafalala, Polana-Canico, Maxaquene"),
	KAMAVOTA("KaMavota", "Urban District No. 4", "Albazine, 3 de Fevereiro, Costa do Sol, Magoanine"),
	KAMUBUKWANA("KaMubukwana", "Urban District No. 5", "Zimpeto, Malhazine, Jardim, Infulene"),
	KATEMBE("Katembe", "Municipal District No. 6", "Beyond Maputo Bay - Katembe, Incassane"),
	INHACA("Inhaca", "Municipal District No. 7", "Inhaca Island, Portuguese Island");

	private final String name;
	private final String category;
	private final String neighborhoods;

	MunicipalDistrict(String name, String category, String neighborhoods)
	{
		this.name = name;
		this.category = category;
		this.neighborhoods = neighborhoods;
	}

	public String getName()
	{
		return name;
	}

	public String getCategory()
	{
		return category;
	}

	public String getNeighborhoods()
	{
		return neighborhoods;
	}

	public String getLabel()
	{
		return name + " - " + category;
	}

	public String getFullDescription()
	{
		return name + " (" + category + ") - Includes: " + neighborhoods;
	}
}