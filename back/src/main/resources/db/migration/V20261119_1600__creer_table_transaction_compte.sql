CREATE TABLE transaction_compte (
    id               BIGSERIAL PRIMARY KEY,
    date_transaction TIMESTAMPTZ NOT NULL DEFAULT now(),
    montant_bronze   BIGINT NOT NULL CHECK (montant_bronze <> 0),
    libelle          VARCHAR(255) NOT NULL,
    type             VARCHAR(30) NOT NULL
                     CHECK (type IN ('ACOMPTE', 'SOLDE', 'PAIEMENT_AVENTURIER', 'ACHAT', 'MANUEL')),
    requete_id       BIGINT REFERENCES requete (id),
    aventurier_id    BIGINT REFERENCES aventurier (id),
    equipement_id    BIGINT REFERENCES equipement (id)
);

CREATE INDEX idx_transaction_date ON transaction_compte (date_transaction DESC);
CREATE INDEX idx_transaction_type ON transaction_compte (type);
CREATE INDEX idx_transaction_requete ON transaction_compte (requete_id);

-- un seul acompte et un seul solde par requête
CREATE UNIQUE INDEX uq_acompte_par_requete ON transaction_compte (requete_id) WHERE type = 'ACOMPTE';
CREATE UNIQUE INDEX uq_solde_par_requete ON transaction_compte (requete_id) WHERE type = 'SOLDE';
