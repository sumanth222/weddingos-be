package com.weddingos.os.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Family {

    private UUID id;
    private UUID weddingId;
    private String familyName;
    private String originCity;
    private OffsetDateTime defaultArrivalAt;
    private OffsetDateTime defaultDepartureAt;

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

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getOriginCity() {
        return originCity;
    }

    public void setOriginCity(String originCity) {
        this.originCity = originCity;
    }

    public OffsetDateTime getDefaultArrivalAt() {
        return defaultArrivalAt;
    }

    public void setDefaultArrivalAt(OffsetDateTime defaultArrivalAt) {
        this.defaultArrivalAt = defaultArrivalAt;
    }

    public OffsetDateTime getDefaultDepartureAt() {
        return defaultDepartureAt;
    }

    public void setDefaultDepartureAt(OffsetDateTime defaultDepartureAt) {
        this.defaultDepartureAt = defaultDepartureAt;
    }
}
