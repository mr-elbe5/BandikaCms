
CREATE TABLE IF NOT EXISTS t_page
(
    id            INTEGER       NOT NULL,
    keywords      VARCHAR(500)  NOT NULL DEFAULT '',
    layout        VARCHAR(255) NOT NULL default 'defaultPage',
    publish_date  TIMESTAMP     NULL,
    published_content TEXT      NOT NULL DEFAULT '',
    CONSTRAINT t_page_pk PRIMARY KEY (id),
    CONSTRAINT t_page_fk1 FOREIGN KEY (id) REFERENCES t_content (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS t_content_log
(
    content_id INTEGER     NOT NULL,
    day        DATE        NOT NULL,
    count      INTEGER 	   NOT NULL,
    CONSTRAINT t_content_log_pk PRIMARY KEY (content_id, day),
    CONSTRAINT t_content_log_fk1 FOREIGN KEY (content_id) REFERENCES t_content (id) ON DELETE CASCADE
);

CREATE SEQUENCE s_page_part_id START 1000;

CREATE TABLE IF NOT EXISTS t_page_part
(
    id            INTEGER      NOT NULL,
    type          VARCHAR(30)  NOT NULL DEFAULT 'LayoutPartData',
    page_id       INTEGER      NULL,
    section       VARCHAR(60)  NOT NULL,
    position      INTEGER      NOT NULL DEFAULT 0,
    name          VARCHAR(60)  NOT NULL DEFAULT '',
    creation_date TIMESTAMP    NOT NULL DEFAULT now(),
    change_date   TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT t_page_part_pk PRIMARY KEY (id),
    CONSTRAINT t_page_part_fk1 FOREIGN KEY (page_id) REFERENCES t_page (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS t_layout_part
(
    id            INTEGER      NOT NULL,
    layout        VARCHAR(255) NOT NULL,
    CONSTRAINT t_layout_part_pk PRIMARY KEY (id),
    CONSTRAINT t_layout_part_fk1 FOREIGN KEY (id) REFERENCES t_page_part (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS t_part_field
(
    part_id    INTEGER     NOT NULL,
    field_type VARCHAR(60) NOT NULL,
    name       VARCHAR(60) NOT NULL DEFAULT '',
    content    TEXT        NOT NULL DEFAULT '',
    CONSTRAINT t_part_field_pk PRIMARY KEY (part_id, name),
    CONSTRAINT t_part_field_fk1 FOREIGN KEY (part_id) REFERENCES t_page_part (id) ON DELETE CASCADE
);

