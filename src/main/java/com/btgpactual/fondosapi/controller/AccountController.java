package com.btgpactual.fondosapi.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import com.btgpactual.fondosapi.model.Account;
import com.btgpactual.fondosapi.model.TransactionRecord;
import com.btgpactual.fondosapi.model.Subscription;
import com.btgpactual.fondosapi.repository.AccountRepository;
import com.btgpactual.fondosapi.repository.TransactionRepository;
import com.btgpactual.fondosapi.repository.SubscriptionRepository;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin(origins = "*")
public class AccountController {
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final SubscriptionRepository subsRepo;
    
    public AccountController(
        AccountRepository accountRepo, 
        TransactionRepository txRepo,
        SubscriptionRepository subsRepo
    ) {
        this.accountRepo = accountRepo;
        this.txRepo = txRepo;
        this.subsRepo = subsRepo;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountId) {
        return accountRepo.findById(accountId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<TransactionRecord>> getTransactions(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "20") int limit) {
        List<TransactionRecord> txs = txRepo.findByAccountId(accountId, limit);
        if (txs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(txs);
    }

    @GetMapping("/{accountId}/subscriptions")
    public ResponseEntity<List<Subscription>> getSubscriptions(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "20") int limit) {
        List<Subscription> subs = subsRepo.findByAccountId(accountId, limit);
        if (subs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(subs);
    }
}
