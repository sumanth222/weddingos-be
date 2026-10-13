create table weddings (
    id uuid primary key,
    owner_user_id varchar(100) not null,
    name varchar(200) not null
);

create table events (
    id uuid primary key,
    wedding_id uuid not null references weddings(id) on delete cascade,
    name varchar(200) not null
);

create index idx_events_wedding_id on events(wedding_id);

create table families (
    id uuid primary key,
    wedding_id uuid not null references weddings(id) on delete cascade,
    family_name varchar(200) not null,
    origin_city varchar(200),
    default_arrival_at timestamptz,
    default_departure_at timestamptz
);

create index idx_families_wedding_id on families(wedding_id);

create table guests (
    id uuid primary key,
    wedding_id uuid not null references weddings(id) on delete cascade,
    family_id uuid not null references families(id) on delete restrict,
    full_name varchar(200) not null,
    phone varchar(50),
    attendance_status varchar(30) not null,
    arrival_at timestamptz,
    departure_at timestamptz,
    needs_accommodation boolean not null,
    needs_transport boolean not null,
    constraint chk_guests_attendance_status check (attendance_status in ('INVITED', 'CONFIRMED', 'DECLINED', 'PENDING'))
);

create index idx_guests_wedding_id on guests(wedding_id);
create index idx_guests_family_id on guests(family_id);

create table event_guests (
    id uuid primary key,
    event_id uuid not null references events(id) on delete cascade,
    guest_id uuid not null references guests(id) on delete cascade,
    attendance_status varchar(30) not null,
    constraint uq_event_guests_event_guest unique (event_id, guest_id),
    constraint chk_event_guests_attendance_status check (attendance_status in ('INVITED', 'CONFIRMED', 'DECLINED', 'PENDING'))
);

create index idx_event_guests_event_id on event_guests(event_id);
create index idx_event_guests_guest_id on event_guests(guest_id);

