package com.weddingos.os.repository;

import com.weddingos.os.domain.AttendanceStatus;
import com.weddingos.os.domain.Guest;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class GuestRepository {

    private final JdbcClient jdbcClient;

    public GuestRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Guest> findByIdAndWeddingId(UUID id, UUID weddingId) {
        return jdbcClient.sql("""
                select id, wedding_id, family_id, full_name, phone, attendance_status, arrival_at, departure_at,
                       needs_accommodation, needs_transport
                from guests
                where id = :id and wedding_id = :weddingId
                """)
                .param("id", id)
                .param("weddingId", weddingId)
                .query((rs, rowNum) -> mapGuest(rs))
                .optional();
    }

    public List<Guest> findAllByIdInAndWeddingId(Collection<UUID> ids, UUID weddingId) {
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Guest> guests = new ArrayList<>();
        for (UUID id : ids) {
            findByIdAndWeddingId(id, weddingId).ifPresent(guests::add);
        }
        return guests;
    }

    public Guest save(Guest guest) {
        int updated = jdbcClient.sql("""
                update guests
                set family_id = :familyId,
                    full_name = :fullName,
                    phone = :phone,
                    attendance_status = :attendanceStatus,
                    arrival_at = :arrivalAt,
                    departure_at = :departureAt,
                    needs_accommodation = :needsAccommodation,
                    needs_transport = :needsTransport
                where id = :id and wedding_id = :weddingId
                """)
                .param("id", guest.getId())
                .param("weddingId", guest.getWeddingId())
                .param("familyId", guest.getFamilyId())
                .param("fullName", guest.getFullName())
                .param("phone", guest.getPhone())
                .param("attendanceStatus", guest.getAttendanceStatus().name())
                .param("arrivalAt", toTimestamp(guest.getArrivalAt()))
                .param("departureAt", toTimestamp(guest.getDepartureAt()))
                .param("needsAccommodation", guest.isNeedsAccommodation())
                .param("needsTransport", guest.isNeedsTransport())
                .update();

        if (updated == 0) {
            jdbcClient.sql("""
                    insert into guests(id, wedding_id, family_id, full_name, phone, attendance_status, arrival_at, departure_at,
                                       needs_accommodation, needs_transport)
                    values (:id, :weddingId, :familyId, :fullName, :phone, :attendanceStatus, :arrivalAt, :departureAt,
                            :needsAccommodation, :needsTransport)
                    """)
                    .param("id", guest.getId())
                    .param("weddingId", guest.getWeddingId())
                    .param("familyId", guest.getFamilyId())
                    .param("fullName", guest.getFullName())
                    .param("phone", guest.getPhone())
                    .param("attendanceStatus", guest.getAttendanceStatus().name())
                    .param("arrivalAt", toTimestamp(guest.getArrivalAt()))
                    .param("departureAt", toTimestamp(guest.getDepartureAt()))
                    .param("needsAccommodation", guest.isNeedsAccommodation())
                    .param("needsTransport", guest.isNeedsTransport())
                    .update();
        }
        return guest;
    }

    private Guest mapGuest(java.sql.ResultSet rs) throws java.sql.SQLException {
        Guest guest = new Guest();
        guest.setId(rs.getObject("id", UUID.class));
        guest.setWeddingId(rs.getObject("wedding_id", UUID.class));
        guest.setFamilyId(rs.getObject("family_id", UUID.class));
        guest.setFullName(rs.getString("full_name"));
        guest.setPhone(rs.getString("phone"));
        guest.setAttendanceStatus(AttendanceStatus.valueOf(rs.getString("attendance_status")));
        guest.setArrivalAt(toOffsetDateTime(rs.getTimestamp("arrival_at")));
        guest.setDepartureAt(toOffsetDateTime(rs.getTimestamp("departure_at")));
        guest.setNeedsAccommodation(rs.getBoolean("needs_accommodation"));
        guest.setNeedsTransport(rs.getBoolean("needs_transport"));
        return guest;
    }

    private Timestamp toTimestamp(OffsetDateTime value) {
        return value == null ? null : Timestamp.from(value.toInstant());
    }

    private OffsetDateTime toOffsetDateTime(Timestamp value) {
        return value == null ? null : value.toInstant().atOffset(ZoneOffset.UTC);
    }
}
