CREATE TABLE IF NOT EXISTS t_link
(
    id            INTEGER       NOT NULL,
    link_url      VARCHAR(500)  NOT NULL DEFAULT '',
    link_icon     VARCHAR(255)  NOT NULL DEFAULT '',
    CONSTRAINT t_link_pk PRIMARY KEY (id),
    CONSTRAINT t_link_fk1 FOREIGN KEY (id) REFERENCES t_content (id) ON DELETE CASCADE
);


update  t_content set type = 'de.elbe5.page.PageData' where type = 'PageData';
update  t_page_part set type = 'de.elbe5.page.LayoutPartData' where type = 'LayoutPartData';
update  t_content set type = 'de.elbe5.link.LinkData' where type = 'de.elbe5.content.LinkData';