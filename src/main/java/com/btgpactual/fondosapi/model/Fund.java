package com.btgpactual.fondosapi.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class Fund {
    private String fundId;
    private String name;
    private Long minAmount;
    private String category;
    private Boolean active;

    @DynamoDbPartitionKey
    public String getFundId() { 
        return fundId; 
    }

    public void setFundId(String fundId) { 
        this.fundId = fundId; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public Long getMinAmount() { 
        return minAmount; 
    }
    public void setMinAmount(Long minAmount) { 
        this.minAmount = minAmount; 
    }

    public String getCategory() { 
        return category; 
    }
    public void setCategory(String category) { 
        this.category = category; 
    }

    public Boolean getActive() { 
        return active; 
    }
    public void setActive(Boolean active) { 
        this.active = active; 
    }
}
