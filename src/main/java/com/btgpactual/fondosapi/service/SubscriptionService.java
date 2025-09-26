package com.btgpactual.fondosapi.service;

import com.btgpactual.fondosapi.dto.SubscriptionRequest;
import com.btgpactual.fondosapi.exception.InsufficientFundsException;
import com.btgpactual.fondosapi.model.Account;
import com.btgpactual.fondosapi.model.Fund;
import com.btgpactual.fondosapi.model.TransactionRecord;
import com.btgpactual.fondosapi.model.Subscription;
import com.btgpactual.fondosapi.repository.AccountRepository;
import com.btgpactual.fondosapi.repository.FundRepository;
import com.btgpactual.fondosapi.repository.TransactionRepository;
import com.btgpactual.fondosapi.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import org.springframework.beans.factory.annotation.Value;


import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import java.util.concurrent.CompletableFuture;

@Service
public class SubscriptionService {

    private final AccountRepository accountRepo;
    private final FundRepository fundRepo;
    private final TransactionRepository txRepo;
    private final SubscriptionRepository subsRepo;
    private final NotificationService notificationService;
    private final DynamoDbClient dynamoDbClient;
    private final String accountsTable;
    private final String transactionsTable;
    private final String subscriptionsTable;

    @Value("${spring.config.activate.on-profile}")
    private String env;

    public SubscriptionService(AccountRepository accountRepo,
                               FundRepository fundRepo,
                               TransactionRepository txRepo,
                               SubscriptionRepository subsRepo,
                               NotificationService notificationService,
                               DynamoDbClient dynamoDbClient,
                               @Value("${aws.accounts-table}") String accountsTable,
                               @Value("${aws.transactions-table}") String transactionsTable,
                               @Value("${aws.subscriptions-table}") String subscriptionsTable) {
        this.accountRepo = accountRepo;
        this.fundRepo = fundRepo;
        this.txRepo = txRepo;
        this.subsRepo = subsRepo;
        this.notificationService = notificationService;
        this.dynamoDbClient = dynamoDbClient;
        this.accountsTable = accountsTable;
        this.transactionsTable = transactionsTable;
        this.subscriptionsTable = subscriptionsTable;
    }

    public String subscribe(SubscriptionRequest req) {
        Fund fund = fundRepo.findById(req.getFundId())
                .orElseThrow(() -> new IllegalArgumentException("Fondo no encontrado"));
        Account account = accountRepo.findById(req.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));

        if (!Boolean.TRUE.equals(fund.getActive())) {
            throw new IllegalArgumentException("Fondo inactivo");
        }
        if (req.getAmount() < fund.getMinAmount()) {
            throw new IllegalArgumentException("Monto inferior al mínimo requerido por el fondo");
        }
        if (account.getBalance() < req.getAmount()) {
            // Mensaje exacto requerido por la prueba
            throw new InsufficientFundsException("No tiene saldo disponible para vincularse al fondo " + fund.getName());
        }

        String txId = UUID.randomUUID().toString();
        long newBalance = account.getBalance() - req.getAmount();
        String now = Instant.now().toString();

        // Construir TransactWriteItemsRequest para escritura atómica: update account & put transaction
        TransactWriteItem updateAccount = TransactWriteItem.builder()
                .update(Update.builder()
                        .tableName(accountsTable)
                        .key(Map.of("accountId", AttributeValue.builder().s(account.getAccountId()).build()))
                        // Condition expression to ensure balance is still >= amount (optimistic)
                        .updateExpression("SET balance = :newBalance")
                        .conditionExpression("balance >= :amount")
                        .expressionAttributeValues(Map.of(
                                ":newBalance", AttributeValue.builder().n(String.valueOf(newBalance)).build(),
                                ":amount", AttributeValue.builder().n(String.valueOf(req.getAmount())).build()
                        ))
                        .build())
                .build();

        Subscription subs = new Subscription();
        subs.setSubscriptionId(UUID.randomUUID().toString());
        subs.setAccountId(account.getAccountId());
        subs.setFundId(fund.getFundId());
        subs.setAmount(req.getAmount());
        subs.setStateName("ACTIVE");
        subs.setCreatedAt(now);

        Put putSubs = Put.builder()
                .tableName(subscriptionsTable)
                .item(Map.of(
                        "subscriptionId", AttributeValue.builder().s(subs.getSubscriptionId()).build(),
                        "createdAt", AttributeValue.builder().s(subs.getCreatedAt()).build(),
                        "accountId", AttributeValue.builder().s(subs.getAccountId()).build(),
                        "fundId", AttributeValue.builder().s(subs.getFundId()).build(),
                        "amount", AttributeValue.builder().n(String.valueOf(subs.getAmount())).build(),
                        "stateName", AttributeValue.builder().s(subs.getStateName()).build()
                ))
                .build();

        TransactWriteItem putSubscriptionItem = TransactWriteItem.builder()
                .put(putSubs)
                .build();

        TransactionRecord tx = new TransactionRecord();
        tx.setTransactionId(txId);
        tx.setAccountId(account.getAccountId());
        tx.setFundId(fund.getFundId());
        tx.setAmount(req.getAmount());
        tx.setType("SUBSCRIPTION");
        tx.setCreatedAt(now);
        tx.setStatus("COMPLETED");
        tx.setMetadata("{}");

        Put putTx = Put.builder()
                .tableName(transactionsTable)
                .item(Map.of(
                        "transactionId", AttributeValue.builder().s(txId).build(),
                        "createdAt", AttributeValue.builder().s(now).build(),
                        "accountId", AttributeValue.builder().s(tx.getAccountId()).build(),
                        "fundId", AttributeValue.builder().s(tx.getFundId()).build(),
                        "type", AttributeValue.builder().s(tx.getType()).build(),
                        "amount", AttributeValue.builder().n(String.valueOf(tx.getAmount())).build(),
                        "status", AttributeValue.builder().s(tx.getStatus()).build(),
                        "metadata", AttributeValue.builder().s(tx.getMetadata()).build()
                ))
                .build();

        TransactWriteItem putTransactionItem = TransactWriteItem.builder()
                .put(putTx)
                .build();

        TransactWriteItemsRequest twr = TransactWriteItemsRequest.builder()
                .transactItems(updateAccount, putSubscriptionItem, putTransactionItem)
                .build();

        try {
            dynamoDbClient.transactWriteItems(twr);
            // Update local projection for immediate read (eventual consistency)
            account.setBalance(newBalance);
            accountRepo.save(account);
            txRepo.save(tx); // duplicate but ok - ensures repo view
        } catch (TransactionCanceledException tce) {
            throw new InsufficientFundsException("No tiene saldo disponible para vincularse al fondo " + fund.getName());
        }

        if (this.env.equals("prod")) {
            CompletableFuture.runAsync(() -> {
                // Send notification
                String notifyVia = req.getNotifyVia() != null ? req.getNotifyVia() : (account.getNotificationType() != null ? account.getNotificationType() : "EMAIL");
                String message = String.format("Suscripción %s al fondo %s por %d COP. Transacción: %s", tx.getType(), fund.getName(), tx.getAmount(), txId);
                if ("SMS".equalsIgnoreCase(notifyVia) && account.getPhone() != null) {
                    notificationService.sendSms(account.getPhone(), message);
                } else if (account.getEmail() != null) {
                    notificationService.sendEmail(account.getEmail(), "Suscripción realizada", "<p>" + message + "</p>");
                } else {
                    notificationService.publishToTopic(message);
                }
            });
        }

        return txId;
    }

    public String cancel(String accountId, String subscriptionId) {
        Account account = accountRepo.findById(accountId).orElseThrow();
        Subscription subscription = subsRepo.findById(subscriptionId).orElseThrow();
        if(!account.getAccountId().equals(subscription.getAccountId())) {
            throw new IllegalArgumentException("La cuenta seleccionada no realizo esa suscripcion");
        }
        if(subscription.getStateName().equals("CANCELLED")) {
            throw new IllegalArgumentException("La subscripcion ya se encuentra cancelada");
        }
        Long amount = subscription.getAmount();
        String fundId = subscription.getFundId();
        String txId = UUID.randomUUID().toString();
        long newBalance = account.getBalance() + amount;
        String now = Instant.now().toString();

        // For simplicity, perform an Update and Put (no condition)
        UpdateItemRequest updateAccountReq = UpdateItemRequest.builder()
                .tableName(accountsTable)
                .key(Map.of("accountId", AttributeValue.builder().s(accountId).build()))
                .updateExpression("SET balance = :newBalance")
                .expressionAttributeValues(Map.of(":newBalance", AttributeValue.builder().n(String.valueOf(newBalance)).build()))
                .build();

        UpdateItemRequest updateSubscriptionReq = UpdateItemRequest.builder()
                .tableName(subscriptionsTable)
                .key(Map.of("subscriptionId", AttributeValue.builder().s(subscriptionId).build()))
                .updateExpression("SET stateName = :statusCancelled")
                .expressionAttributeValues(Map.of(":statusCancelled", AttributeValue.builder().s("CANCELLED").build()))
                .build();

        PutItemRequest putTransactionReq = PutItemRequest.builder()
                .tableName(transactionsTable)
                .item(Map.of(
                        "transactionId", AttributeValue.builder().s(txId).build(),
                        "createdAt", AttributeValue.builder().s(now).build(),
                        "accountId", AttributeValue.builder().s(accountId).build(),
                        "fundId", AttributeValue.builder().s(fundId).build(),
                        "type", AttributeValue.builder().s("CANCELLATION").build(),
                        "amount", AttributeValue.builder().n(String.valueOf(amount)).build(),
                        "status", AttributeValue.builder().s("COMPLETED").build(),
                        "metadata", AttributeValue.builder().s("{}").build()
                ))
                .build();

        TransactWriteItemsRequest twr = TransactWriteItemsRequest.builder()
                .transactItems(
                        TransactWriteItem.builder().update(Update.builder()
                                .tableName(updateAccountReq.tableName())
                                .key(updateAccountReq.key())
                                .updateExpression(updateAccountReq.updateExpression())
                                .expressionAttributeValues(updateAccountReq.expressionAttributeValues())
                                .build()).build(),
                        TransactWriteItem.builder().update(Update.builder()
                                .tableName(updateSubscriptionReq.tableName())
                                .key(updateSubscriptionReq.key())
                                .updateExpression(updateSubscriptionReq.updateExpression())
                                .expressionAttributeValues(updateSubscriptionReq.expressionAttributeValues())
                                .build()).build(),
                        TransactWriteItem.builder().put(Put.builder()
                                .tableName(putTransactionReq.tableName())
                                .item(putTransactionReq.item())
                                .build()).build()
                ).build();

        dynamoDbClient.transactWriteItems(twr);

        account.setBalance(newBalance);
        accountRepo.save(account);
        TransactionRecord tx = new TransactionRecord();
        tx.setTransactionId(txId);
        tx.setAccountId(accountId);
        tx.setFundId(fundId);
        tx.setType("CANCELLATION");
        tx.setAmount(amount);
        tx.setCreatedAt(now);
        tx.setStatus("COMPLETED");
        txRepo.save(tx);

        if (this.env.equals("prod")) {
            CompletableFuture.runAsync(() -> {
                // Notify
                String message = String.format("Cancelación del fondo %s por %d COP. Transacción: %s", fundId, amount, txId);
                if (account.getEmail() != null) {
                    notificationService.sendEmail(account.getEmail(), "Cancelación realizada", "<p>" + message + "</p>");
                } else {
                    notificationService.publishToTopic(message);
                }
            });
        }    
        return txId;
    }
}
