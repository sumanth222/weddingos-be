package com.weddingos.os.repository;

import com.weddingos.os.domain.AttendanceStatus;
import com.weddingos.os.domain.EventGuest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class EventGuestRepository {

    private final JdbcClient jdbcClient;

    public EventGuestRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<EventGuest> findAllByEventIdAndWeddingId(UUID eventId, UUID weddingId) {
        return jdbcClient.sql("""
                select eg.id, eg.event_id, eg.guest_id, eg.attendance_status
                from event_guests eg
                join events e on e.id = eg.event_id
                join guests g on g.id = eg.guest_id
                where eg.event_id = :eventId and e.wedding_id = :weddingId and g.wedding_id = :weddingId
                """)
                .param("eventId", eventId)
                .param("weddingId", weddingId)
                .query((rs, rowNum) -> mapEventGuest(rs))
                .list();
    }

    public Optional<EventGuest> findByEventIdAndGuestId(UUID eventId, UUID guestId) {
        return jdbcClient.sql("""
                select id, event_id, guest_id, attendance_status
                from event_guests
                where event_id = :eventId and guest_id = :guestId
                """)
                .param("eventId", eventId)
                .param("guestId", guestId)
                .query((rs, rowNum) -> mapEventGuest(rs))
                .optional();
    }

    public EventGuest save(EventGuest eventGuest) {
        int updated = jdbcClient.sql("""
                update event_guests
                set attendance_status = :attendanceStatus
                where event_id = :eventId and guest_id = :guestId
                """)
                .param("eventId", eventGuest.getEventId())
                .param("guestId", eventGuest.getGuestId())
                .param("attendanceStatus", eventGuest.getAttendanceStatus().name())
                .update();

        if (updated == 0) {
            jdbcClient.sql("""
                    insert into event_guests(id, event_id, guest_id, attendance_status)
                    values (:id, :eventId, :guestId, :attendanceStatus)
                    """)
                    .param("id", eventGuest.getId())
                    .param("eventId", eventGuest.getEventId())
                    .param("guestId", eventGuest.getGuestId())
                    .param("attendanceStatus", eventGuest.getAttendanceStatus().name())
                    .update();
        }
        return eventGuest;
    }

    public void deleteAllByEventIdAndGuestIdIn(UUID eventId, Collection<UUID> guestIds) {
        if (guestIds.isEmpty()) {
            return;
        }
        for (UUID guestId : guestIds) {
            jdbcClient.sql("delete from event_guests where event_id = :eventId and guest_id = :guestId")
                    .param("eventId", eventId)
                    .param("guestId", guestId)
                    .update();
        }
    }

    private EventGuest mapEventGuest(java.sql.ResultSet rs) throws java.sql.SQLException {
        EventGuest eventGuest = new EventGuest();
        eventGuest.setId(rs.getObject("id", UUID.class));
        eventGuest.setEventId(rs.getObject("event_id", UUID.class));
        eventGuest.setGuestId(rs.getObject("guest_id", UUID.class));
        eventGuest.setAttendanceStatus(AttendanceStatus.valueOf(rs.getString("attendance_status")));
        return eventGuest;
    }
}
