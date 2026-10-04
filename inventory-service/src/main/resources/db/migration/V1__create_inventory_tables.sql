create table stock_item (
                            equipment_id varchar(100) primary key,
                            total integer not null,
                            held integer not null,

                            constraint stock_item_total_non_negative
                                check (total >= 0),

                            constraint stock_item_held_within_total
                                check (
                                    held >= 0
                                        and held <= total
                                    )
);

insert into stock_item (
    equipment_id,
    total,
    held
)
values
    ('camera', 5, 0);

create table inventory_hold (
                                booking_id uuid primary key,
                                equipment_id varchar(100) not null,
                                quantity integer not null,
                                status varchar(20) not null,
                                reason_code varchar(50),

                                constraint inventory_hold_quantity_positive
                                    check (quantity > 0),

                                constraint inventory_hold_status_valid
                                    check (status in ('HELD', 'RELEASED', 'REFUSED')),

                                constraint inventory_hold_reason_valid
                                    check (
                                        reason_code is null
                                            or reason_code in (
                                                               'UNKNOWN_EQUIPMENT',
                                                               'INSUFFICIENT_STOCK'
                                            )
                                        ),

                                constraint inventory_hold_result_consistent
                                    check (
                                        (
                                            status = 'REFUSED'
                                                and reason_code is not null
                                            )
                                            or
                                        (
                                            status in ('HELD', 'RELEASED')
                                                and reason_code is null
                                            )
                                        )
);

create index inventory_hold_equipment_id_idx
    on inventory_hold (equipment_id);