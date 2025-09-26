package com.btgpactual.fondosapi.repository;

import com.btgpactual.fondosapi.model.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

@Repository
public class AccountRepository {
    private final DynamoDbTable<Account> accountTable;

    public AccountRepository(DynamoDbEnhancedClient enhancedClient,
        @Value("${aws.accounts-table}") String tableName) {
        this.accountTable = enhancedClient.table(tableName, TableSchema.fromBean(Account.class));
    }

    public Optional<Account> findById(String accountId) {
        Account acct = accountTable.getItem(Key.builder().partitionValue(accountId).build());
        return Optional.ofNullable(acct);
    }

    public void save(Account account) {
        accountTable.putItem(account);
    }
}
