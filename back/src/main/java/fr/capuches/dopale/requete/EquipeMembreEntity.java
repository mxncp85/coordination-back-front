package fr.capuches.dopale.requete;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "equipe_membre")
@IdClass(EquipeMembreEntity.Pk.class)
public class EquipeMembreEntity {

	@Id
	@Column(name = "requete_id")
	private Long requeteId;

	@Id
	@Column(name = "aventurier_id")
	private Long aventurierId;

	@Column(name = "taux_journalier_fige_bronze")
	private Long tauxJournalierFigeBronze;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "requete_id", insertable = false, updatable = false)
	private RequeteEntity requete;

	public Long getRequeteId() {
		return requeteId;
	}

	public void setRequeteId(Long requeteId) {
		this.requeteId = requeteId;
	}

	public Long getAventurierId() {
		return aventurierId;
	}

	public void setAventurierId(Long aventurierId) {
		this.aventurierId = aventurierId;
	}

	public Long getTauxJournalierFigeBronze() {
		return tauxJournalierFigeBronze;
	}

	public void setTauxJournalierFigeBronze(Long tauxJournalierFigeBronze) {
		this.tauxJournalierFigeBronze = tauxJournalierFigeBronze;
	}

	public RequeteEntity getRequete() {
		return requete;
	}

	public void setRequete(RequeteEntity requete) {
		this.requete = requete;
	}

	public static class Pk implements Serializable {
		private Long requeteId;
		private Long aventurierId;

		public Pk() {
		}

		public Pk(Long requeteId, Long aventurierId) {
			this.requeteId = requeteId;
			this.aventurierId = aventurierId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Pk pk)) {
				return false;
			}
			return Objects.equals(requeteId, pk.requeteId) && Objects.equals(aventurierId, pk.aventurierId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(requeteId, aventurierId);
		}
	}
}
