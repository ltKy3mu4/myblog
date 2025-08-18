create table if not exists posts
(
    id         bigint primary key generated always as identity,
    title      varchar(256) not null,
    text       text,
    likesCount int
);

create table if not exists comments
(
    id      bigint primary key generated always as identity,
    post_id bigint not null,
    text    text   not null,

    constraint post_id_fk foreign key (post_id) references posts (id)

);

create table if not exists tags
(
    id   bigint primary key generated always as identity,
    name varchar(100) not null unique
);

create index name_idx on tags (name);

create table if not exists posts_tags
(
    post_id bigint not null,
    tag_id  bigint not null,

    constraint post_id_fk foreign key (post_id) references posts (id),
    constraint tag_id_fk foreign key (tag_id) references tags (id)
);

create index post_id_idx on posts_tags (post_id);

create index tag_id_idx on posts_tags (tag_id);

create table if not exists images
(
    id        bigint primary key generated always as identity,
    post_id   bigint       not null,
    file_name varchar(255) not null,
    data      bytea        not null,

    constraint post_id_fk foreign key (post_id) references posts (id) on delete cascade

);

create index post_id_idx on images (post_id);
