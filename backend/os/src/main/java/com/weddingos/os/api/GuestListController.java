package com.weddingos.os.api;

import com.weddingos.os.api.dto.GuestListResponse;
import com.weddingos.os.api.dto.GuestListUpsertRequest;
import com.weddingos.os.service.GuestListService;
import com.weddingos.os.service.WeddingAuthorizationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weddings/{weddingId}/events/{eventId}/guest-list")
public class GuestListController {

    private final GuestListService guestListService;
    private final WeddingAuthorizationService weddingAuthorizationService;

    public GuestListController(GuestListService guestListService, WeddingAuthorizationService weddingAuthorizationService) {
        this.guestListService = guestListService;
        this.weddingAuthorizationService = weddingAuthorizationService;
    }

    @PutMapping
    public ResponseEntity<GuestListResponse> saveGuestList(
            @PathVariable String weddingId,
            @PathVariable String eventId,
            @Valid @RequestBody GuestListUpsertRequest request,
            Authentication authentication
    ) {
        weddingAuthorizationService.assertCanModifyWedding(weddingId, authentication);
        return ResponseEntity.ok(guestListService.saveGuestList(weddingId, eventId, request));
    }
}

