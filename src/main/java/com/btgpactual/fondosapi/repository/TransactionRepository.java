package com.btgpactual.fondosapi.repository;

import com.btgpactual.fondosapi.model.TransactionRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

@Repository
public class TransactionRepository {
    private final DynamoDbTable<TransactionRecord> table;
    private final String transactionsTable;

    public TransactionRepository(DynamoDbEnhancedClient enhancedClient,
                                 @Value("${aws.transactions-table}") String tableName) {
        this.transactionsTable = tableName;
        this.table = enhancedClient.table(this.transactionsTable, TableSchema.fromBean(TransactionRecord.class));
    }

    public void save(TransactionRecord tx) {
        table.putItem(tx);
    }

    public List<TransactionRecord> findByAccountId(String accountId, int limit) {
        List<TransactionRecord> result = new ArrayList<>();
        DynamoDbIndex<TransactionRecord> gsi = table.index("accountId-index");

        QueryEnhancedRequest.Builder builder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(accountId).build()))
                .scanIndexForward(false); // más recientes primero

        if (limit > 0) {
            builder.limit(limit);
        }

        gsi.query(builder.build()).forEach(page -> result.addAll(page.items()));
        return result;
    }

    public Optional<TransactionRecord> findById(String transactionId) {
        TransactionRecord transaction = table.getItem(Key.builder().partitionValue(transactionId).build());
        return Optional.ofNullable(transaction);
    }
}