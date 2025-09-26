package com.btgpactual.fondosapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CancellationRequest {
    @NotBlank private String accountId;
    @NotBlank private String transactionId;
    
    public String getAccountId() { 
        return accountId; 
    }
    public void setAccountId(String accountId) { 
        this.accountId = accountId; 
    }

    public String getTransactionId() {
        return this.transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
