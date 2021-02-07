package com.userfront.common;

public enum TransferError {

    SAME_ACCOUNT_TRANSFER("Transaction Failed :Cannot transfer to same account type"),
    LOW_BALANCE("Transaction Failed : Your account does not have enough balance."),
    INVALID_RECIPIENT("Transaction Failed : No recipient found"),
    INVALID_TRANSFER("Transaction Failed : Invalid data provided"),
    ;

    private String errorMessage;

    TransferError(String errorMessage){
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
