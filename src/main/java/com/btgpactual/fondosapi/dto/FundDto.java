package com.btgpactual.fondosapi.dto;

public class FundDto {
    private String fundId;
    private String name;
    private Long minAmount;
    private String category;
    private Boolean active;

    public String getFundId() {
        return this.fundId;
    }
    public void setFundId(String fundId) {
        this.fundId = fundId;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Long getMinAmount() {
        return this.minAmount;
    }
    public void setMinAmount(Long minAmount) {
        this.minAmount = minAmount;
    }

    public String getCategory() {
        return this.category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getActive() {
        return this.active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
}
