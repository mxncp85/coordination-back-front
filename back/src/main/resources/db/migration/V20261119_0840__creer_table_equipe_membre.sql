-- Composition d'équipe : un aventurier ne peut être que dans une équipe active à la fois
CREATE TABLE equipe_membre (
	requete_id                   BIGINT NOT NULL REFERENCES requete (id) ON DELETE CASCADE,
	aventurier_id                BIGINT NOT NULL REFERENCES aventurier (id),
	taux_journalier_fige_bronze  BIGINT,
	PRIMARY KEY (requete_id, aventurier_id)
);

CREATE INDEX idx_equipe_membre_aventurier ON equipe_membre (aventurier_id);
