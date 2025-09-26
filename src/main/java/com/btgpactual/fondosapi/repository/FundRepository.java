package com.btgpactual.fondosapi.repository;

import com.btgpactual.fondosapi.model.Fund;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FundRepository {
    private final DynamoDbTable<Fund> table;

    public FundRepository(DynamoDbEnhancedClient enhancedClient,
                          @Value("${aws.funds-table}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(Fund.class));
    }

    public Optional<Fund> findById(String fundId) {
        Fund f = table.getItem(r -> r.key(k -> k.partitionValue(fundId)));
        return Optional.ofNullable(f);
    }

    public List<Fund> findAll() {
        List<Fund> out = new ArrayList<>();
        table.scan().items().forEach(out::add);
        return out;
    }

    public void save(Fund fund) {
        table.putItem(fund);
    }
}
