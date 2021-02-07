package com.userfront.common;

public enum AccountType {
    PRIMARY_ACCOUNT("Primary"),
    SAVING_ACCOUNT("Savings");

    private String accountType;

    AccountType(String accountType){
        this.accountType = accountType;
    }

    public String getAccountType() {
        return accountType;
    }
}
