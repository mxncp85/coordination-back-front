-- Aventuriers (AVT) : fiche, spécialités, repos US-21 (repos_jusqu_au)
CREATE TABLE aventurier (
	id                      BIGSERIAL PRIMARY KEY,
	nom                     VARCHAR(255) NOT NULL,
	experience              INTEGER NOT NULL CHECK (experience >= 1),
	taux_base_bronze        BIGINT NOT NULL CHECK (taux_base_bronze >= 1),
	taux_journalier_bronze  BIGINT GENERATED ALWAYS AS (
		ROUND(taux_base_bronze * (1 + 0.5 * LN(experience)))::BIGINT
	) STORED,
	repos_jusqu_au          DATE,
	supprime_le             TIMESTAMPTZ
);

CREATE TABLE aventurier_specialite (
	aventurier_id BIGINT NOT NULL REFERENCES aventurier (id) ON DELETE CASCADE,
	specialite    VARCHAR(64) NOT NULL,
	PRIMARY KEY (aventurier_id, specialite)
);

CREATE INDEX idx_aventurier_nom ON aventurier (nom);
CREATE INDEX idx_aventurier_repos ON aventurier (repos_jusqu_au);
