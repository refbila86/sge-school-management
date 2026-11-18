package mz.co.sge.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.*;
import mz.co.sge.enumes.PaymentMethod;

@Entity
@Table(name = "payment_items")
@NamedQueries(
{ @NamedQuery(name = "PaymentItem.findByPayment", query = "SELECT pi FROM PaymentItemEntity pi WHERE pi.payment.id = :paymentId") })
public class PaymentItemEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private PaymentEntity payment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fee_item_id")
	private FeeItemEntity feeItem;

	@Column(nullable = false)
	private String descricao;

	@Column(precision = 12, scale = 2, nullable = false)
	private BigDecimal valor;

	@Column(precision = 12, scale = 2)
	private BigDecimal quantidade = BigDecimal.ONE;

	@Column(precision = 12, scale = 2)
	private BigDecimal subtotal;

	// NOVO: método + referência DESTE item, independentes dos outros itens.
	// Mesmo estilo da PaymentEntity: @Enumerated STRING + String simples, sem
	// @Column inventado.
	// Valores possíveis (do teu enum): NUMERARIO, TRANSFERENCIA_BANCARIA, MPESA,
	// EMOLA, MKESH.
	@Enumerated(EnumType.STRING)
	private PaymentMethod method;

	private String reference;

	@PrePersist
	@PreUpdate
	public void calc()
	{
		if (quantidade == null)
			quantidade = BigDecimal.ONE;
		if (valor != null)
			subtotal = valor.multiply(quantidade);
	}

	// getters/setters
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public PaymentEntity getPayment()
	{
		return payment;
	}

	public void setPayment(PaymentEntity p)
	{
		this.payment = p;
	}

	public FeeItemEntity getFeeItem()
	{
		return feeItem;
	}

	public void setFeeItem(FeeItemEntity f)
	{
		this.feeItem = f;
	}

	public String getDescricao()
	{
		return descricao;
	}

	public void setDescricao(String d)
	{
		this.descricao = d;
	}

	public BigDecimal getValor()
	{
		return valor;
	}

	public void setValor(BigDecimal v)
	{
		this.valor = v;
		calc();
	}

	public BigDecimal getQuantidade()
	{
		return quantidade;
	}

	public void setQuantidade(BigDecimal q)
	{
		this.quantidade = q;
		calc();
	}

	public BigDecimal getSubtotal()
	{
		return subtotal;
	}

	public void setSubtotal(BigDecimal s)
	{
		this.subtotal = s;
	}

	public PaymentMethod getMethod()
	{
		return method;
	}

	public void setMethod(PaymentMethod m)
	{
		this.method = m;
	}

	public String getReference()
	{
		return reference;
	}

	public void setReference(String r)
	{
		this.reference = r;
	}
}
