create table eventuate.saga_instance_participants
(
    saga_type   varchar(255) not null,
    saga_id     varchar(100) not null,
    destination varchar(100) not null,
    resource    varchar(100) not null,
    primary key (saga_type, saga_id, destination, resource)
);

create table eventuate.saga_instance
(
    saga_type      varchar(255)  not null,
    saga_id        varchar(100)  not null,
    state_name     varchar(100)  not null,
    last_request_id varchar(100),
    end_state      boolean,
    compensating   boolean,
    failed         boolean,
    saga_data_type varchar(1000) not null,
    saga_data_json varchar(1000) not null,
    primary key (saga_type, saga_id)
);

create table eventuate.saga_lock_table
(
    target    varchar(100) primary key,
    saga_type varchar(255) not null,
    saga_id   varchar(100) not null
);

create table eventuate.saga_stash_table
(
    message_id      varchar(100) primary key,
    target          varchar(100)  not null,
    saga_type       varchar(255)  not null,
    saga_id         varchar(100)  not null,
    message_headers varchar(1000) not null,
    message_payload varchar(1000) not null
);