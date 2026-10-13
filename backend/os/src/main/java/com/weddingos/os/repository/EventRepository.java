package com.weddingos.os.repository;

import com.weddingos.os.domain.Event;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepository {

    private final JdbcClient jdbcClient;

    public EventRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Event> findByIdAndWeddingId(UUID id, UUID weddingId) {
        return jdbcClient.sql("""
                select id, wedding_id, name
                from events
                where id = :id and wedding_id = :weddingId
                """)
                .param("id", id)
                .param("weddingId", weddingId)
                .query((rs, rowNum) -> {
                    Event event = new Event();
                    event.setId(rs.getObject("id", UUID.class));
                    event.setWeddingId(rs.getObject("wedding_id", UUID.class));
                    event.setName(rs.getString("name"));
                    return event;
                })
                .optional();
    }
}
