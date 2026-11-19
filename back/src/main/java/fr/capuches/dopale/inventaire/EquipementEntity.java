package fr.capuches.dopale.inventaire;

import fr.capuches.dopale.api.model.StatutEquipement;
import fr.capuches.dopale.commun.exception.ConflitMetierException;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "equipement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquipementEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nom;
	private String type;
	private int durabiliteMax;
	private int durabiliteActuelle;
	private long prixAchatBronze;

	@Enumerated(EnumType.STRING)
	private StatutEquipement statut;

	@Version
	private long version;

	public EquipementEntity(String nom, String type, int durabiliteMax, long prixAchatBronze) {
		this.nom = nom;
		this.type = type;
		this.durabiliteMax = durabiliteMax;
		this.durabiliteActuelle = durabiliteMax;
		this.prixAchatBronze = prixAchatBronze;
		this.statut = StatutEquipement.DISPONIBLE;
	}
	public void emprunter() {
		if (statut != StatutEquipement.DISPONIBLE) {
			throw new ConflitMetierException("Equipement " + id + " non disponible (" + statut + ")");
		}
		statut = StatutEquipement.EMPRUNTE;
	}

	public void reparer() {
		if (statut == StatutEquipement.EMPRUNTE) {
			throw new ConflitMetierException("Equipement " + id + " en mission, réparation impossible");
		}
		durabiliteActuelle = durabiliteMax;
		statut = StatutEquipement.DISPONIBLE;
	}

}
