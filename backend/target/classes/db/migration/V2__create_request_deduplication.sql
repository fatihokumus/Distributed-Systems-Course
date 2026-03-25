create table if not exists request_deduplication (
    request_id varchar(100) primary key,
    response_status int not null,
    response_body text not null,
    created_at timestamptz not null default now()
);