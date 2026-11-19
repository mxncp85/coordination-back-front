package fr.capuches.dopale.compte;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import fr.capuches.dopale.api.ComptesApi;
import fr.capuches.dopale.api.model.CreateTransactionRequest;
import fr.capuches.dopale.api.model.GetSolde200Response;
import fr.capuches.dopale.api.model.ListTransactions200Response;
import fr.capuches.dopale.api.model.Transaction;
import fr.capuches.dopale.api.model.TypeTransaction;
import fr.capuches.dopale.commun.pagination.PaginationUtils;

@RestController
public class CompteController implements ComptesApi {

	private static final Sort PLUS_RECENTES_D_ABORD = Sort.by(Sort.Direction.DESC, "dateTransaction", "id");

	private final CompteService compteService;
	private final TransactionMapper transactionMapper;

	public CompteController(CompteService compteService, TransactionMapper transactionMapper) {
		this.compteService = compteService;
		this.transactionMapper = transactionMapper;
	}

	@Override
	public ResponseEntity<GetSolde200Response> getSolde() {
		return ResponseEntity.ok(new GetSolde200Response(compteService.solde()));
	}

	@Override
	public ResponseEntity<ListTransactions200Response> listTransactions(TypeTransaction type, LocalDate from,
			LocalDate to, Long requeteId, Integer page, Integer size) {
		Page<TransactionEntity> transactions = compteService.lister(type, from, to, requeteId,
				PaginationUtils.pageable(page, size, PLUS_RECENTES_D_ABORD));
		return ResponseEntity.ok(
				PaginationUtils.versReponse(transactions, transactionMapper::versDto, ListTransactions200Response::new));
	}

	@Override
	public ResponseEntity<Transaction> createTransaction(CreateTransactionRequest request) {
		boolean credit = request.getSens() == CreateTransactionRequest.SensEnum.CREDIT;
		TransactionEntity transaction = compteService.saisirManuellement(request.getMontantBronze(), credit,
				request.getLibelle());
		return ResponseEntity.status(HttpStatus.CREATED).body(transactionMapper.versDto(transaction));
	}
}
