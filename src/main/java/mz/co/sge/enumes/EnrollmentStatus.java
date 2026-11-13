package mz.co.sge.enumes;

public enum EnrollmentStatus
{
	PENDING, // Pré-matrícula criada, aguardando pagamento/validação
	CONFIRMED, // Pagamento confirmado, pronto para efetivar
	ENROLLED, // Matriculado - só entra aqui após confirmação final
	CANCELLED, TRANSFERRED, SUSPENDED;

	public boolean canTransitionTo(EnrollmentStatus next)
	{
		return switch (this)
		{
		case PENDING -> next == CONFIRMED || next == CANCELLED;
		case CONFIRMED -> next == ENROLLED || next == CANCELLED;
		case ENROLLED -> next == TRANSFERRED || next == SUSPENDED || next == CANCELLED;
		default -> false;
		};
	}
}
