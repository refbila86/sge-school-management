package mz.co.sge.enumes;

public enum Province
{
	MAPUTO_CIDADE("Maputo City"), MAPUTO_PROVINCIA("Maputo Province"), GAZA("Gaza"), INHAMBANE("Inhambane"), SOFALA("Sofala"), MANICA("Manica"), TETE("Tete"),
	ZAMBEZIA("Zambezia"), NAMPULA("Nampula"), NIASSA("Niassa"), CABO_DELGADO("Cabo Delgado");

	private final String label;

	Province(String label)
	{
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}
}