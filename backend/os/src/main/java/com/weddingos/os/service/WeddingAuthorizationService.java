package com.weddingos.os.service;

import com.weddingos.os.repository.WeddingRepository;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class WeddingAuthorizationService {

    private final WeddingRepository weddingRepository;

    public WeddingAuthorizationService(WeddingRepository weddingRepository) {
        this.weddingRepository = weddingRepository;
    }

    public void assertCanModifyWedding(String weddingId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Authentication is required");
        }
        weddingRepository.findByIdAndOwnerUserId(weddingId, authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("You are not allowed to modify this wedding"));
    }
}

