package com.weddingos.os.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Guest {

    private UUID id;
    private UUID weddingId;
    private UUID familyId;
    private String fullName;
    private String phone;
    private AttendanceStatus attendanceStatus;
    private OffsetDateTime arrivalAt;
    private OffsetDateTime departureAt;
    private boolean needsAccommodation;
    private boolean needsTransport;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getWeddingId() {
        return weddingId;
    }

    public void setWeddingId(UUID weddingId) {
        this.weddingId = weddingId;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public void setFamilyId(UUID familyId) {
        this.familyId = familyId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public AttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(AttendanceStatus attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public OffsetDateTime getArrivalAt() {
        return arrivalAt;
    }

    public void setArrivalAt(OffsetDateTime arrivalAt) {
        this.arrivalAt = arrivalAt;
    }

    public OffsetDateTime getDepartureAt() {
        return departureAt;
    }

    public void setDepartureAt(OffsetDateTime departureAt) {
        this.departureAt = departureAt;
    }

    public boolean isNeedsAccommodation() {
        return needsAccommodation;
    }

    public void setNeedsAccommodation(boolean needsAccommodation) {
        this.needsAccommodation = needsAccommodation;
    }

    public boolean isNeedsTransport() {
        return needsTransport;
    }

    public void setNeedsTransport(boolean needsTransport) {
        this.needsTransport = needsTransport;
    }
}
