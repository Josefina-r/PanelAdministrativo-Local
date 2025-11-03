package com.parkeaya.local_paneladmi.model.dto;

import java.time.LocalDateTime;

public class ParkingRegistrationResponse {
    private Boolean success;
    private String message;
    private String requestId;
    private String status;
    private LocalDateTime submittedAt;
    private String estimatedReviewTime;
    private ParkingRegistrationRequest parkingData;

    // Constructores
    public ParkingRegistrationResponse() {
        this.submittedAt = LocalDateTime.now();
    }

    public ParkingRegistrationResponse(Boolean success, String message, String requestId) {
        this();
        this.success = success;
        this.message = message;
        this.requestId = requestId;
        this.status = "PENDING";
        this.estimatedReviewTime = "2-3 días hábiles";
    }

    // Getters y Setters
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getEstimatedReviewTime() { return estimatedReviewTime; }
    public void setEstimatedReviewTime(String estimatedReviewTime) { this.estimatedReviewTime = estimatedReviewTime; }

    public ParkingRegistrationRequest getParkingData() { return parkingData; }
    public void setParkingData(ParkingRegistrationRequest parkingData) { this.parkingData = parkingData; }

    @Override
    public String toString() {
        return "ParkingRegistrationResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", requestId='" + requestId + '\'' +
                ", status='" + status + '\'' +
                ", submittedAt=" + submittedAt +
                ", estimatedReviewTime='" + estimatedReviewTime + '\'' +
                ", parkingData=" + parkingData +
                '}';
    }
}