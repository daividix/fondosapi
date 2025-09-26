package com.btgpactual.fondosapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubscriptionRequest {
    @NotBlank
    private String accountId;
    @NotBlank
    private String fundId;
    @NotNull @Min(1)
    private Long amount;
    private String notifyVia; // EMAIL | SMS

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
    public Long getAmount() { 
        return amount; 
    }
    public void setAmount(Long amount) { 
        this.amount = amount; 
    }
    public String getNotifyVia() { 
        return notifyVia; 
    }
    public void setNotifyVia(String notifyVia) { 
        this.notifyVia = notifyVia; 
    }
}
