package fr.capuches.dopale.aventurier;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "aventurier")
public class AventurierEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nom;

	@Column(nullable = false)
	private int experience;

	@Column(name = "taux_base_bronze", nullable = false)
	private long tauxBaseBronze;

	@Column(name = "taux_journalier_bronze", insertable = false, updatable = false)
	private Long tauxJournalierBronze;

	@Column(name = "repos_jusqu_au")
	private LocalDate reposJusquAu;

	@Column(name = "supprime_le")
	private Instant supprimeLe;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "aventurier_specialite", joinColumns = @JoinColumn(name = "aventurier_id"))
	@Column(name = "specialite", nullable = false)
	private Set<String> specialites = new LinkedHashSet<>();

	public Long getId() {
		return id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public int getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}

	public long getTauxBaseBronze() {
		return tauxBaseBronze;
	}

	public void setTauxBaseBronze(long tauxBaseBronze) {
		this.tauxBaseBronze = tauxBaseBronze;
	}

	public Long getTauxJournalierBronze() {
		return tauxJournalierBronze;
	}

	public LocalDate getReposJusquAu() {
		return reposJusquAu;
	}

	public void setReposJusquAu(LocalDate reposJusquAu) {
		this.reposJusquAu = reposJusquAu;
	}

	public Instant getSupprimeLe() {
		return supprimeLe;
	}

	public void setSupprimeLe(Instant supprimeLe) {
		this.supprimeLe = supprimeLe;
	}

	public Set<String> getSpecialites() {
		return specialites;
	}

	public void setSpecialites(Set<String> specialites) {
		this.specialites = specialites;
	}

	public boolean estSupprime() {
		return supprimeLe != null;
	}

	public boolean estEnRepos(LocalDate jour) {
		return reposJusquAu != null && !jour.isAfter(reposJusquAu);
	}
}
