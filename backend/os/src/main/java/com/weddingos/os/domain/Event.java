package com.weddingos.os.domain;

import java.util.UUID;

public class Event {

    private UUID id;
    private UUID weddingId;
    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
