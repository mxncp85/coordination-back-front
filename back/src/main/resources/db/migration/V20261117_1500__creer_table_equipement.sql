CREATE TABLE equipement (
    id                  BIGSERIAL PRIMARY KEY,
    nom                 VARCHAR(200) NOT NULL,
    type                VARCHAR(50) NOT NULL,
    durabilite_max      INT NOT NULL CHECK (durabilite_max >= 1),
    durabilite_actuelle INT NOT NULL,
    prix_achat_bronze   BIGINT NOT NULL CHECK (prix_achat_bronze >= 0),
    statut              VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE'
                        CHECK (statut IN ('DISPONIBLE', 'EMPRUNTE', 'EN_REPARATION')),
    version             BIGINT NOT NULL DEFAULT 0,
    CHECK (durabilite_actuelle BETWEEN 0 AND durabilite_max)
);

CREATE INDEX idx_equipement_statut ON equipement (statut);
