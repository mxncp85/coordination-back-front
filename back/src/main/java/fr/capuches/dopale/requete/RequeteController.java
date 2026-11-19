package fr.capuches.dopale.requete;

import fr.capuches.dopale.api.RequetesApi;
import fr.capuches.dopale.api.model.ListRequetes200Response;
import fr.capuches.dopale.api.model.Requete;
import fr.capuches.dopale.api.model.RequeteInput;
import fr.capuches.dopale.api.model.StatutRequete;
import fr.capuches.dopale.commun.pagination.PaginationUtils;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RequeteController implements RequetesApi {

    private final RequeteService service;
    private final RequeteMapper mapper;

    @Override
    public ResponseEntity<ListRequetes200Response> listRequetes(
            StatutRequete statut, Long primeMin, Long primeMax, Integer niveau,
            LocalDate echeanceAvant, String commanditaire, String tri, String ordre,
            Integer page, Integer size) {
        FiltreRequete filtre = new FiltreRequete(statut, primeMin, primeMax, niveau, echeanceAvant, commanditaire);
        Page<RequeteEntity> resultat = service.lister(filtre, tri, ordre, page, size);
        return ResponseEntity.ok(
                PaginationUtils.versReponse(resultat, mapper::versDto, ListRequetes200Response::new));
    }

    @Override
    public ResponseEntity<Requete> createRequete(RequeteInput requeteInput) {
        Requete creee = mapper.versDto(service.creer(requeteInput));
        return ResponseEntity.status(HttpStatus.CREATED).body(creee);
    }

    @Override
    public ResponseEntity<Requete> getRequete(Long id) {
        return ResponseEntity.ok(mapper.versDto(service.trouver(id)));
    }

    @Override
    public ResponseEntity<Requete> updateRequete(Long id, RequeteInput requeteInput) {
        return ResponseEntity.ok(mapper.versDto(service.modifier(id, requeteInput)));
    }
}
