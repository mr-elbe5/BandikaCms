update  t_content set type = 'de.elbe5.page.PageData' where type = 'PageData';
update  t_page_part set type = 'de.elbe5.page.LayoutPartData' where type = 'LayoutPartData';
update  t_content set type = 'de.elbe5.link.LinkData' where type = 'de.elbe5.content.LinkData';