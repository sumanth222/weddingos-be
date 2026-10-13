package com.weddingos.os.repository;

import com.weddingos.os.domain.Family;
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
public class FamilyRepository {

    private final JdbcClient jdbcClient;

    public FamilyRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Family> findByIdAndWeddingId(UUID id, UUID weddingId) {
        return jdbcClient.sql("""
                select id, wedding_id, family_name, origin_city, default_arrival_at, default_departure_at
                from families
                where id = :id and wedding_id = :weddingId
                """)
                .param("id", id)
                .param("weddingId", weddingId)
                .query((rs, rowNum) -> mapFamily(rs))
                .optional();
    }

    public List<Family> findAllByIdInAndWeddingId(Collection<UUID> ids, UUID weddingId) {
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Family> families = new ArrayList<>();
        for (UUID id : ids) {
            findByIdAndWeddingId(id, weddingId).ifPresent(families::add);
        }
        return families;
    }

    public Family save(Family family) {
        int updated = jdbcClient.sql("""
                update families
                set family_name = :familyName,
                    origin_city = :originCity,
                    default_arrival_at = :defaultArrivalAt,
                    default_departure_at = :defaultDepartureAt
                where id = :id and wedding_id = :weddingId
                """)
                .param("id", family.getId())
                .param("weddingId", family.getWeddingId())
                .param("familyName", family.getFamilyName())
                .param("originCity", family.getOriginCity())
                .param("defaultArrivalAt", toTimestamp(family.getDefaultArrivalAt()))
                .param("defaultDepartureAt", toTimestamp(family.getDefaultDepartureAt()))
                .update();

        if (updated == 0) {
            jdbcClient.sql("""
                    insert into families(id, wedding_id, family_name, origin_city, default_arrival_at, default_departure_at)
                    values (:id, :weddingId, :familyName, :originCity, :defaultArrivalAt, :defaultDepartureAt)
                    """)
                    .param("id", family.getId())
                    .param("weddingId", family.getWeddingId())
                    .param("familyName", family.getFamilyName())
                    .param("originCity", family.getOriginCity())
                    .param("defaultArrivalAt", toTimestamp(family.getDefaultArrivalAt()))
                    .param("defaultDepartureAt", toTimestamp(family.getDefaultDepartureAt()))
                    .update();
        }
        return family;
    }

    private Family mapFamily(java.sql.ResultSet rs) throws java.sql.SQLException {
        Family family = new Family();
        family.setId(rs.getObject("id", UUID.class));
        family.setWeddingId(rs.getObject("wedding_id", UUID.class));
        family.setFamilyName(rs.getString("family_name"));
        family.setOriginCity(rs.getString("origin_city"));
        family.setDefaultArrivalAt(toOffsetDateTime(rs.getTimestamp("default_arrival_at")));
        family.setDefaultDepartureAt(toOffsetDateTime(rs.getTimestamp("default_departure_at")));
        return family;
    }

    private Timestamp toTimestamp(OffsetDateTime value) {
        return value == null ? null : Timestamp.from(value.toInstant());
    }

    private OffsetDateTime toOffsetDateTime(Timestamp value) {
        return value == null ? null : value.toInstant().atOffset(ZoneOffset.UTC);
    }
}
