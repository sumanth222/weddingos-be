package com.weddingos.os.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GuestListControllerIntegrationTest {

    private static final UUID WEDDING_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_WEDDING_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID EVENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SECOND_EVENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID OTHER_EVENT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID FAMILY_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID GUEST_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("delete from event_guests");
        jdbcTemplate.update("delete from guests");
        jdbcTemplate.update("delete from families");
        jdbcTemplate.update("delete from events");
        jdbcTemplate.update("delete from weddings");

        jdbcTemplate.update("insert into weddings(id, owner_user_id, name) values (?, ?, ?)", WEDDING_ID, "test-user", "Main Wedding");
        jdbcTemplate.update("insert into weddings(id, owner_user_id, name) values (?, ?, ?)", OTHER_WEDDING_ID, "other-user", "Other Wedding");
        jdbcTemplate.update("insert into events(id, wedding_id, name) values (?, ?, ?)", EVENT_ID, WEDDING_ID, "Reception");
        jdbcTemplate.update("insert into events(id, wedding_id, name) values (?, ?, ?)", SECOND_EVENT_ID, WEDDING_ID, "Haldi");
        jdbcTemplate.update("insert into events(id, wedding_id, name) values (?, ?, ?)", OTHER_EVENT_ID, OTHER_WEDDING_ID, "Other Event");
    }

    @Test
    void createsNewFamilyWithMultipleGuests() {
        String body = """
                {
                  "families": [
                    {
                      "familyId": null,
                      "familyName": "Reddy Family",
                      "originCity": "Warangal",
                      "defaultArrivalAt": "2026-11-20T10:00:00Z",
                      "defaultDepartureAt": "2026-11-22T18:00:00Z",
                      "guests": [
                        {
                          "guestId": null,
                          "fullName": "Ramesh Reddy",
                          "phone": "9876543210",
                          "attendanceStatus": "CONFIRMED",
                          "arrivalAt": null,
                          "departureAt": null,
                          "needsAccommodation": true,
                          "needsTransport": true
                        },
                        {
                          "guestId": null,
                          "fullName": "Sita Reddy",
                          "phone": "9876543211",
                          "attendanceStatus": "PENDING",
                          "arrivalAt": "2026-11-20T12:00:00Z",
                          "departureAt": null,
                          "needsAccommodation": false,
                          "needsTransport": true
                        }
                      ]
                    }
                  ]
                }
                """;

        ResponseEntity<String> response = exchange(EVENT_ID, body, "test-user", "test-password");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Reddy Family");
        assertThat(response.getBody()).contains("\"effectiveArrivalAt\":\"2026-11-20T10:00:00Z\"");
    }

    @Test
    void updatesExistingFamilyAndGuest() {
        jdbcTemplate.update("insert into families(id, wedding_id, family_name, origin_city) values (?, ?, ?, ?)", FAMILY_ID, WEDDING_ID, "Reddy Family", "Warangal");
        jdbcTemplate.update("insert into guests(id, wedding_id, family_id, full_name, phone, attendance_status, needs_accommodation, needs_transport) values (?, ?, ?, ?, ?, ?, ?, ?)", GUEST_ID, WEDDING_ID, FAMILY_ID, "Ramesh Reddy", "9876543210", "PENDING", true, false);
        jdbcTemplate.update("insert into event_guests(id, event_id, guest_id, attendance_status) values (?, ?, ?, ?)", UUID.randomUUID(), EVENT_ID, GUEST_ID, "PENDING");

        String body = """
                {
                  "families": [
                    {
                      "familyId": "66666666-6666-6666-6666-666666666666",
                      "familyName": "Reddy Family Updated",
                      "originCity": "Hyderabad",
                      "defaultArrivalAt": null,
                      "defaultDepartureAt": null,
                      "guests": [
                        {
                          "guestId": "77777777-7777-7777-7777-777777777777",
                          "fullName": "Ramesh Reddy Updated",
                          "phone": "9999999999",
                          "attendanceStatus": "CONFIRMED",
                          "arrivalAt": null,
                          "departureAt": null,
                          "needsAccommodation": false,
                          "needsTransport": true
                        }
                      ]
                    }
                  ]
                }
                """;

        ResponseEntity<String> response = exchange(EVENT_ID, body, "test-user", "test-password");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Reddy Family Updated");
        assertThat(response.getBody()).contains("Ramesh Reddy Updated");
        assertThat(response.getBody()).contains("CONFIRMED");
    }

    @Test
    void linksOneGuestToMultipleEventsWithoutDuplication() {
        jdbcTemplate.update("insert into families(id, wedding_id, family_name) values (?, ?, ?)", FAMILY_ID, WEDDING_ID, "Reddy Family");
        jdbcTemplate.update("insert into guests(id, wedding_id, family_id, full_name, attendance_status, needs_accommodation, needs_transport) values (?, ?, ?, ?, ?, ?, ?)", GUEST_ID, WEDDING_ID, FAMILY_ID, "Ramesh Reddy", "CONFIRMED", true, true);
        jdbcTemplate.update("insert into event_guests(id, event_id, guest_id, attendance_status) values (?, ?, ?, ?)", UUID.randomUUID(), SECOND_EVENT_ID, GUEST_ID, "CONFIRMED");

        String body = """
                {
                  "families": [
                    {
                      "familyId": "66666666-6666-6666-6666-666666666666",
                      "familyName": "Reddy Family",
                      "originCity": null,
                      "defaultArrivalAt": null,
                      "defaultDepartureAt": null,
                      "guests": [
                        {
                          "guestId": "77777777-7777-7777-7777-777777777777",
                          "fullName": "Ramesh Reddy",
                          "phone": null,
                          "attendanceStatus": "CONFIRMED",
                          "arrivalAt": null,
                          "departureAt": null,
                          "needsAccommodation": true,
                          "needsTransport": true
                        }
                      ]
                    }
                  ]
                }
                """;

        ResponseEntity<String> response = exchange(EVENT_ID, body, "test-user", "test-password");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Integer count = jdbcTemplate.queryForObject("select count(*) from event_guests where guest_id = ?", Integer.class, GUEST_ID);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void removesGuestFromOneEventWithoutDeletingGuest() {
        jdbcTemplate.update("insert into families(id, wedding_id, family_name) values (?, ?, ?)", FAMILY_ID, WEDDING_ID, "Reddy Family");
        jdbcTemplate.update("insert into guests(id, wedding_id, family_id, full_name, attendance_status, needs_accommodation, needs_transport) values (?, ?, ?, ?, ?, ?, ?)", GUEST_ID, WEDDING_ID, FAMILY_ID, "Ramesh Reddy", "CONFIRMED", true, true);
        jdbcTemplate.update("insert into event_guests(id, event_id, guest_id, attendance_status) values (?, ?, ?, ?)", UUID.randomUUID(), EVENT_ID, GUEST_ID, "CONFIRMED");
        jdbcTemplate.update("insert into event_guests(id, event_id, guest_id, attendance_status) values (?, ?, ?, ?)", UUID.randomUUID(), SECOND_EVENT_ID, GUEST_ID, "CONFIRMED");

        String body = """
                {
                  "families": [
                    {
                      "familyId": "66666666-6666-6666-6666-666666666666",
                      "familyName": "Reddy Family",
                      "originCity": null,
                      "defaultArrivalAt": null,
                      "defaultDepartureAt": null,
                      "guests": [
                      ]
                    }
                  ]
                }
                """;

        ResponseEntity<String> response = exchange(EVENT_ID, body, "test-user", "test-password");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Integer eventGuestCount = jdbcTemplate.queryForObject("select count(*) from event_guests where event_id = ? and guest_id = ?", Integer.class, EVENT_ID, GUEST_ID);
        Integer guestCount = jdbcTemplate.queryForObject("select count(*) from guests where id = ?", Integer.class, GUEST_ID);
        Integer secondEventCount = jdbcTemplate.queryForObject("select count(*) from event_guests where event_id = ? and guest_id = ?", Integer.class, SECOND_EVENT_ID, GUEST_ID);
        assertThat(eventGuestCount).isZero();
        assertThat(guestCount).isEqualTo(1);
        assertThat(secondEventCount).isEqualTo(1);
    }

    @Test
    void rejectsEventThatBelongsToAnotherWedding() {
        String body = minimalValidBody();
        ResponseEntity<String> response = exchange(OTHER_EVENT_ID, body, "test-user", "test-password");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void rejectsGuestOrFamilyIdsBelongingToAnotherWedding() {
        UUID foreignFamilyId = UUID.fromString("88888888-8888-8888-8888-888888888888");
        UUID foreignGuestId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        jdbcTemplate.update("insert into families(id, wedding_id, family_name) values (?, ?, ?)", foreignFamilyId, OTHER_WEDDING_ID, "Foreign Family");
        jdbcTemplate.update("insert into guests(id, wedding_id, family_id, full_name, attendance_status, needs_accommodation, needs_transport) values (?, ?, ?, ?, ?, ?, ?)", foreignGuestId, OTHER_WEDDING_ID, foreignFamilyId, "Foreign Guest", "CONFIRMED", true, true);

        String body = """
                {
                  "families": [
                    {
                      "familyId": "88888888-8888-8888-8888-888888888888",
                      "familyName": "Foreign Family",
                      "originCity": null,
                      "defaultArrivalAt": null,
                      "defaultDepartureAt": null,
                      "guests": [
                        {
                          "guestId": "99999999-9999-9999-9999-999999999999",
                          "fullName": "Foreign Guest",
                          "phone": null,
                          "attendanceStatus": "CONFIRMED",
                          "arrivalAt": null,
                          "departureAt": null,
                          "needsAccommodation": true,
                          "needsTransport": true
                        }
                      ]
                    }
                  ]
                }
                """;

        ResponseEntity<String> response = exchange(EVENT_ID, body, "test-user", "test-password");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handlesInvalidInputAndInvalidAttendanceStatuses() {
        String body = """
                {
                  "families": [
                    {
                      "familyId": null,
                      "familyName": "",
                      "originCity": "Warangal",
                      "defaultArrivalAt": "2026-11-22T18:00:00Z",
                      "defaultDepartureAt": "2026-11-20T10:00:00Z",
                      "guests": [
                        {
                          "guestId": null,
                          "fullName": "",
                          "phone": "bad",
                          "attendanceStatus": "INVALID",
                          "arrivalAt": null,
                          "departureAt": null,
                          "needsAccommodation": true,
                          "needsTransport": true
                        }
                      ]
                    }
                  ]
                }
                """;

        ResponseEntity<String> response = exchange(EVENT_ID, body, "test-user", "test-password");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void enforcesAuthorization() {
        ResponseEntity<String> response = exchange(EVENT_ID, minimalValidBody(), "other-user", "test-password");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ResponseEntity<String> exchange(UUID eventId, String body, String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.withBasicAuth(username, password).exchange(
                "/api/weddings/{weddingId}/events/{eventId}/guest-list",
                HttpMethod.PUT,
                new HttpEntity<>(body, headers),
                String.class,
                WEDDING_ID,
                eventId
        );
    }

    private String minimalValidBody() {
        return """
                {
                  "families": [
                    {
                      "familyId": null,
                      "familyName": "Reddy Family",
                      "originCity": "Warangal",
                      "defaultArrivalAt": null,
                      "defaultDepartureAt": null,
                      "guests": [
                        {
                          "guestId": null,
                          "fullName": "Ramesh Reddy",
                          "phone": "9876543210",
                          "attendanceStatus": "CONFIRMED",
                          "arrivalAt": null,
                          "departureAt": null,
                          "needsAccommodation": true,
                          "needsTransport": true
                        }
                      ]
                    }
                  ]
                }
                """;
    }
}

