package com.btgpactual.fondosapi.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;


@DynamoDbBean
public class TransactionRecord {
    private String transactionId;
    private String createdAt;
    private String accountId;
    private String fundId;
    private String type; // SUBSCRIPTION | CANCELLATION
    private Long amount;
    private String status;
    private String metadata; // JSON string for simplicity

    @DynamoDbPartitionKey
    public String getTransactionId() { 
        return transactionId; 
    }
    public void setTransactionId(String transactionId) { 
        this.transactionId = transactionId; 
    }

    @DynamoDbSecondarySortKey(indexNames = "accountId-index")
    public String getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(String createdAt) { 
        this.createdAt = createdAt; 
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "accountId-index")
    public String getAccountId() { 
        return accountId; 
    }
    public void setAccountId(String accountId) { 
        this.accountId = accountId; 
    }

    public String getFundId() { 
        return fundId; 
    }
    public void setFundId(String fundId) { 
        this.fundId = fundId; 
    }

    public String getType() { 
        return type; 
    }
    public void setType(String type) { 
        this.type = type; 
    }

    public Long getAmount() { 
        return amount; 
    }
    public void setAmount(Long amount) { 
        this.amount = amount; 
    }

    public String getStatus() { 
        return status; 
    }
    public void setStatus(String status) { 
        this.status = status; 
    }

    public String getMetadata() { 
        return metadata; 
    }
    public void setMetadata(String metadata) { 
        this.metadata = metadata; 
    }
}
