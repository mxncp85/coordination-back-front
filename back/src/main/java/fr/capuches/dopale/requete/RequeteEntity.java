package fr.capuches.dopale.requete;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "requete")
public class RequeteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nom;

	@Column(nullable = false)
	private String description;

	@Column(name = "duree_jours", nullable = false)
	private int dureeJours;

	@Column(name = "date_peremption", nullable = false)
	private LocalDate datePeremption;

	@Column(name = "prime_bronze", nullable = false)
	private long primeBronze;

	@Column(nullable = false)
	private String commanditaire;

	@Column(name = "exp_recommandee", nullable = false)
	private int expRecommandee;

	@Column(name = "xp_gagnee", nullable = false)
	private int xpGagnee;

	@Column(nullable = false)
	private String statut;

	@Column(name = "date_depart")
	private LocalDate dateDepart;

	@Column(name = "date_cloture")
	private LocalDate dateCloture;

	public Long getId() {
		return id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDureeJours() {
		return dureeJours;
	}

	public void setDureeJours(int dureeJours) {
		this.dureeJours = dureeJours;
	}

	public LocalDate getDatePeremption() {
		return datePeremption;
	}

	public void setDatePeremption(LocalDate datePeremption) {
		this.datePeremption = datePeremption;
	}

	public long getPrimeBronze() {
		return primeBronze;
	}

	public void setPrimeBronze(long primeBronze) {
		this.primeBronze = primeBronze;
	}

	public String getCommanditaire() {
		return commanditaire;
	}

	public void setCommanditaire(String commanditaire) {
		this.commanditaire = commanditaire;
	}

	public int getExpRecommandee() {
		return expRecommandee;
	}

	public void setExpRecommandee(int expRecommandee) {
		this.expRecommandee = expRecommandee;
	}

	public int getXpGagnee() {
		return xpGagnee;
	}

	public void setXpGagnee(int xpGagnee) {
		this.xpGagnee = xpGagnee;
	}

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}

	public LocalDate getDateDepart() {
		return dateDepart;
	}

	public void setDateDepart(LocalDate dateDepart) {
		this.dateDepart = dateDepart;
	}

	public LocalDate getDateCloture() {
		return dateCloture;
	}

	public void setDateCloture(LocalDate dateCloture) {
		this.dateCloture = dateCloture;
	}

	public boolean estActive() {
		return "EQUIPE_VALIDEE".equals(statut) || "EN_COURS".equals(statut);
	}
}
