package com.btgpactual.fondosapi.repository;

import com.btgpactual.fondosapi.model.Subscription;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

@Repository
public class SubscriptionRepository {

    private final DynamoDbTable<Subscription> table;

    public SubscriptionRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("Subscriptions", TableSchema.fromBean(Subscription.class));
    }

    public void save(Subscription subscription) {
        table.putItem(subscription);
    }

    public Optional<Subscription> findById(String subscriptionId) {
        return Optional.ofNullable(table.getItem(
                Key.builder().partitionValue(subscriptionId).build()));
    }

    public List<Subscription> findByAccountId(String accountId, int limit) {
        DynamoDbIndex<Subscription> gsi = table.index("accountId-index");
        List<Subscription> result = new ArrayList<>();
        
        QueryEnhancedRequest.Builder builder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(accountId).build()))
                .scanIndexForward(false); // más recientes primero

        if (limit > 0) {
            builder.limit(limit);
        }

        gsi.query(builder.build()).forEach(page -> 
            result.addAll(page.items()
        ));
        //return result.stream().filter(sub -> sub.getStatus().equals("ACTIVE"));
        return result;
    }

    public void updateStatus(String subscriptionId, String newStatus) {
        Subscription sub = findById(subscriptionId).orElseThrow();
        sub.setStateName(newStatus);
        save(sub);
    }
}
