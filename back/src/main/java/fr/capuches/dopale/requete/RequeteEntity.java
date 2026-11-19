package fr.capuches.dopale.requete;

import java.time.LocalDate;

import fr.capuches.dopale.api.model.StatutRequete;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "requete")
@Getter
@Setter
@NoArgsConstructor
public class RequeteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int dureeJours;

    @Column(nullable = false)
    private LocalDate datePeremption;

    @Column(nullable = false)
    private long primeBronze;

    @Column(nullable = false, length = 150)
    private String commanditaire;

    @Column(nullable = false)
    private int expRecommandee;

    @Column(nullable = false)
    private int xpGagnee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutRequete statut = StatutRequete.NOUVELLE;

    private LocalDate dateDepart;

    private LocalDate dateCloture;

    public boolean estActive() {
        return "EQUIPE_VALIDEE".equals(statut) || "EN_COURS".equals(statut);
    }
}

