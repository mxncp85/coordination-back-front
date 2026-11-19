-- Équipements réservés pour l'équipe d'une requête
CREATE TABLE equipe_equipement (
                                   requete_id    BIGINT NOT NULL REFERENCES requete (id) ON DELETE CASCADE,
                                   equipement_id BIGINT NOT NULL REFERENCES equipement (id),
                                   PRIMARY KEY (requete_id, equipement_id)
);

CREATE INDEX idx_equipe_equipement_equipement ON equipe_equipement (equipement_id);
