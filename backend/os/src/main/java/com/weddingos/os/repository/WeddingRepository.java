package com.weddingos.os.repository;

import com.weddingos.os.domain.Wedding;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class WeddingRepository {

    private final JdbcClient jdbcClient;

    public WeddingRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Wedding> findByIdAndOwnerUserId(UUID id, String ownerUserId) {
        return jdbcClient.sql("""
                select id, owner_user_id, name
                from weddings
                where id = :id and owner_user_id = :ownerUserId
                """)
                .param("id", id)
                .param("ownerUserId", ownerUserId)
                .query((rs, rowNum) -> {
                    Wedding wedding = new Wedding();
                    wedding.setId(rs.getObject("id", UUID.class));
                    wedding.setOwnerUserId(rs.getString("owner_user_id"));
                    wedding.setName(rs.getString("name"));
                    return wedding;
                })
                .optional();
    }
}
