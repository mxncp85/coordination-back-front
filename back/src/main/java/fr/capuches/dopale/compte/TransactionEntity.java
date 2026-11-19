package fr.capuches.dopale.compte;

import java.time.OffsetDateTime;

import fr.capuches.dopale.api.model.TypeTransaction;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Une transaction ne se modifie jamais : pas de setters, une correction est une nouvelle transaction.
@Entity
@Table(name = "transaction_compte")
public class TransactionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private OffsetDateTime dateTransaction;
	private long montantBronze;
	private String libelle;

	@Enumerated(EnumType.STRING)
	private TypeTransaction type;

	private Long requeteId;
	private Long aventurierId;
	private Long equipementId;

	protected TransactionEntity() {
	}

	private TransactionEntity(long montantBronze, String libelle, TypeTransaction type) {
		this.dateTransaction = OffsetDateTime.now();
		this.montantBronze = montantBronze;
		this.libelle = libelle;
		this.type = type;
	}

	public static TransactionEntity credit(long montantBronze, String libelle, TypeTransaction type) {
		verifierPositif(montantBronze);
		return new TransactionEntity(montantBronze, libelle, type);
	}

	public static TransactionEntity debit(long montantBronze, String libelle, TypeTransaction type) {
		verifierPositif(montantBronze);
		return new TransactionEntity(-montantBronze, libelle, type);
	}

	private static void verifierPositif(long montantBronze) {
		if (montantBronze <= 0) {
			throw new IllegalArgumentException("Le montant doit être strictement positif : " + montantBronze);
		}
	}

	public TransactionEntity pourRequete(Long requeteId) {
		this.requeteId = requeteId;
		return this;
	}

	public TransactionEntity pourAventurier(Long aventurierId) {
		this.aventurierId = aventurierId;
		return this;
	}

	public TransactionEntity pourEquipement(Long equipementId) {
		this.equipementId = equipementId;
		return this;
	}

	public Long getId() {
		return id;
	}

	public OffsetDateTime getDateTransaction() {
		return dateTransaction;
	}

	public long getMontantBronze() {
		return montantBronze;
	}

	public String getLibelle() {
		return libelle;
	}

	public TypeTransaction getType() {
		return type;
	}

	public Long getRequeteId() {
		return requeteId;
	}

	public Long getAventurierId() {
		return aventurierId;
	}

	public Long getEquipementId() {
		return equipementId;
	}
}
