create table customer_quota (
                                customer_id varchar(100) primary key,
                                booking_limit integer not null,
                                confirmed_count integer not null,

                                constraint customer_quota_limit_non_negative
                                    check (booking_limit >= 0),

                                constraint customer_quota_count_within_limit
                                    check (
                                        confirmed_count >= 0
                                            and confirmed_count <= booking_limit
                                        )
);

insert into customer_quota (
    customer_id,
    booking_limit,
    confirmed_count
)
values
    ('customer-normal', 2, 0),
    ('customer-compensation', 0, 0);

create table booking (
                         booking_id uuid primary key,
                         customer_id varchar(100) not null,
                         equipment_id varchar(100) not null,
                         quantity integer not null,
                         status varchar(20) not null,
                         rejection_reason varchar(50),

                         constraint booking_customer_fk
                             foreign key (customer_id)
                                 references customer_quota (customer_id),

                         constraint booking_quantity_positive
                             check (quantity > 0),

                         constraint booking_status_valid
                             check (status in ('PENDING', 'CONFIRMED', 'REJECTED')),

                         constraint booking_rejection_reason_valid
                             check (
                                 rejection_reason is null
                                     or rejection_reason in (
                                                             'UNKNOWN_EQUIPMENT',
                                                             'INSUFFICIENT_STOCK',
                                                             'HOLD_CONFLICT',
                                                             'CUSTOMER_LIMIT_EXCEEDED'
                                     )
                                 ),

                         constraint booking_result_consistent
                             check (
                                 (
                                     status = 'REJECTED'
                                         and rejection_reason is not null
                                     )
                                     or
                                 (
                                     status in ('PENDING', 'CONFIRMED')
                                         and rejection_reason is null
                                     )
                                 )
);

create index booking_customer_id_idx
    on booking (customer_id);