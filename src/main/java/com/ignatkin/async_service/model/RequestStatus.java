package com.ignatkin.async_service.model;

public enum RequestStatus {
    RECEIVED,
    VALIDATING,
    PROCESSING,
    FINALIZING,
    DONE,
    ERROR
}