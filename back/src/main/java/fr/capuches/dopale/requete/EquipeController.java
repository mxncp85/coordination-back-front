package fr.capuches.dopale.requete;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fr.capuches.dopale.api.EquipesApi;
import fr.capuches.dopale.api.model.Equipe;
import fr.capuches.dopale.api.model.SetEquipeRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EquipeController implements EquipesApi {

    private final EquipeService service;

    @Override
    public ResponseEntity<Equipe> getEquipe(Long id) {
        return ResponseEntity.ok(service.consulter(id));
    }

    @Override
    public ResponseEntity<Equipe> setEquipe(Long id, SetEquipeRequest setEquipeRequest) {
        return ResponseEntity.ok(service.composer(id, setEquipeRequest.getAventurierIds(), setEquipeRequest.getEquipementIds()));
    }

    @Override
    public ResponseEntity<Equipe> validerEquipe(Long id) {
        return ResponseEntity.ok(service.valider(id));
    }

    @Override
    public ResponseEntity<Equipe> suggererEquipe(Long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Suggestion d'équipe : sprint 2");
    }
}
