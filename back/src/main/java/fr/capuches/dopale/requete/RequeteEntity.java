package fr.capuches.dopale.requete;

import java.time.LocalDate;

import fr.capuches.dopale.api.model.StatutRequete;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

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

    @ElementCollection
    @CollectionTable(name = "equipe_equipement", joinColumns = @JoinColumn(name = "requete_id"))
    @Column(name = "equipement_id")
    private Set<Long> equipementIds = new LinkedHashSet<>();
    public boolean estActive() {
        return statut == StatutRequete.EQUIPE_VALIDEE || statut == StatutRequete.EN_COURS;
    }
}

