package com.userfront.common;

public enum AppointmentError {

    INVALID_DATE("Valid date must be provided"),
    INVALID_LOCATION("Location must be provided");

    private String errorMessage;

    AppointmentError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
