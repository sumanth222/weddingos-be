package com.weddingos.os.api.dto;

import java.util.List;
import java.util.UUID;

public record GuestListResponse(
        UUID weddingId,
        UUID eventId,
        List<FamilyResponse> families
) {
}

