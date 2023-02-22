-- update from v11.x to v 12.0

--change to one master page, one page type
alter table t_page drop column master;
alter table t_page add layout VARCHAR(255) NOT NULL default 'defaultPage';
update t_page t1 set layout = (select t2.layout from t_section_page t2 where t1.id = t2.id);
update t_page set layout = 'defaultPage' where layout = 'defaultSectionPage';
update t_page set layout = 'contentAsidePage' where layout = 'contentAsideSectionPage';

CREATE OR REPLACE FUNCTION ADDPAGE (id INTEGER,parent_id INTEGER,name VARCHAR,display_name VARCHAR,
                                    description VARCHAR,user_id INTEGER, layout VARCHAR)
    RETURNS VOID AS
$$
BEGIN
    INSERT INTO t_content (id,type,parent_id,ranking,name,display_name,description,creator_id,changer_id,access_type,nav_type)
    VALUES (id,'PageData',parent_id,0,name,display_name,description,user_id,user_id,'OPEN','HEADER');
    INSERT INTO t_page (id, keywords, layout)
    VALUES (id, '', layout);
END
$$
    LANGUAGE plpgsql;

drop function ADDSECTIONPAGE;

update t_content set type = 'PageData' where type = 'SectionPageData';

alter table t_part_field drop CONSTRAINT t_part_field_fk1;
alter table t_field_section_part drop CONSTRAINT t_field_section_part_pk;
alter table t_field_section_part drop CONSTRAINT t_field_section_part_fk1;
alter table t_field_section_part rename to t_layout_part;
alter table t_layout_part add CONSTRAINT t_layout_part_pk PRIMARY KEY (id);

alter table t_section_part drop CONSTRAINT t_section_part_pk;
alter table t_section_part drop CONSTRAINT t_section_part_fk1;
alter table t_section_part rename to t_page_part;
alter table t_page_part add CONSTRAINT t_page_part_pk PRIMARY KEY (id);
alter table t_page_part alter column type set DEFAULT 'LayoutPartData';
alter table t_page_part add CONSTRAINT t_page_part_fk1 FOREIGN KEY (page_id) REFERENCES t_page (id) ON DELETE CASCADE;
alter table t_layout_part add CONSTRAINT t_layout_part_fk1 FOREIGN KEY (id) REFERENCES t_page_part (id) ON DELETE CASCADE;
alter table t_part_field add CONSTRAINT t_part_field_fk1 FOREIGN KEY (part_id) REFERENCES t_page_part (id) ON DELETE CASCADE;

update t_page_part set type = 'PagePartData' where type = 'SectionPartData';
update t_page_part set type = 'LayoutPartData' where type = 'FieldSectionPartData';

drop table t_section_page;

--end change to one master page, one page type

ALTER SEQUENCE s_section_part_id RENAME TO s_page_part_id;