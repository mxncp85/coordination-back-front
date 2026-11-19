-- Requêtes / missions (cycle de vie ; dates de départ et clôture pour l'emploi du temps)
CREATE TABLE requete (
	id               BIGSERIAL PRIMARY KEY,
	nom              VARCHAR(255) NOT NULL,
	description      TEXT NOT NULL,
	duree_jours      INTEGER NOT NULL CHECK (duree_jours >= 1),
	date_peremption  DATE NOT NULL,
	prime_bronze     BIGINT NOT NULL CHECK (prime_bronze >= 1),
	commanditaire    VARCHAR(255) NOT NULL,
	exp_recommandee  INTEGER NOT NULL DEFAULT 0 CHECK (exp_recommandee >= 0),
	xp_gagnee        INTEGER NOT NULL DEFAULT 0 CHECK (xp_gagnee >= 0),
	statut           VARCHAR(32) NOT NULL,
	date_depart      DATE,
	date_cloture     DATE
);

CREATE INDEX idx_requete_statut ON requete (statut);
