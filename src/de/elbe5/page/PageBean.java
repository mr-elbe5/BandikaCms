/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.base.Log;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;

import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class PageBean extends ContentBean {

    private static PageBean instance = null;

    public static PageBean getInstance() {
        if (instance == null) {
            instance = new PageBean();
        }
        return instance;
    }

    PagePartData getNewPagePartData(String className){
        try {
            Class<? extends PagePartData> cls = Class.forName(className).asSubclass(PagePartData.class);
            Constructor<? extends PagePartData> ctor = cls.getConstructor();
            return ctor.newInstance();
        }
        catch(Exception e){
            Log.error("could not create class " + className,  e);
        }
        return null;
    }

    private static final String GET_CONTENT_EXTRAS_SQL = "SELECT keywords, layout, publish_date, published_content FROM t_page WHERE id=?";

    @Override
    public void readContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof PageData))
            return;
        PageData data = (PageData) contentData;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_CONTENT_EXTRAS_SQL);
            pst.setString(1, data.getId());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int i = 1;
                    data.setKeywords(rs.getString(i++));
                    data.setLayout(rs.getString(i++));
                    Timestamp ts = rs.getTimestamp(i++);
                    data.setPublishDate(ts == null ? null : ts.toLocalDateTime());
                    data.setPublishedContent(rs.getString(i));
                    readParts(con, data);
                    data.sortParts();
                }
            }
        } finally {
            closeStatement(pst);
        }
    }

    private static final String INSERT_CONTENT_EXTRAS_SQL = "insert into t_page (keywords,layout,publish_date,published_content,id) values(?,?,?,?,?)";

    @Override
    public void createContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof PageData))
            return;
        PageData data = (PageData) contentData;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(INSERT_CONTENT_EXTRAS_SQL);
            setExtraValues(pst,data);
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
        writeAllParts(con, data);
    }

    private static final String UPDATE_CONTENT_EXTRAS_SQL = "update t_page set keywords=?,layout=?,publish_date=?,published_content=? where id=?";

    @Override
    public void updateContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof PageData))
            return;
        PageData data = (PageData) contentData;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(UPDATE_CONTENT_EXTRAS_SQL);
            setExtraValues(pst,data);
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
        writeAllParts(con, data);
    }

    private void setExtraValues(PreparedStatement pst, PageData data) throws SQLException{
        int i = 1;
        pst.setString(i++, data.getKeywords());
        pst.setString(i++, data.getLayout());
        if (data.getPublishDate()==null)
            pst.setNull(i++,Types.TIMESTAMP);
        else
            pst.setTimestamp(i++, Timestamp.valueOf(data.getPublishDate()));
        pst.setString(i++,data.getPublishedContent());
        pst.setString(i, data.getId());
    }

    public boolean publishPage(PageData data) {
        Connection con = startTransaction();
        try {
            if (!data.isNew() && ContentBean.getInstance().changedContent(con, data)) {
                return rollbackTransaction(con);
            }
            publishPage(con, data);
            return commitTransaction(con);
        } catch (Exception se) {
            return rollbackTransaction(con, se);
        }
    }

    private static final String PUBLISH_CONTENT_SQL = "update t_page set publish_date=?,published_content=? where id=?";

    public void publishPage(Connection con, PageData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(PUBLISH_CONTENT_SQL);
            int i = 1;
            pst.setTimestamp(i++, Timestamp.valueOf(data.getPublishDate()));
            pst.setString(i++,data.getPublishedContent());
            pst.setString(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    private static final String REPLACE_IN_PAGE_SQL = "UPDATE t_page set published_content = REPLACE(published_content,?,?)";

    public void replaceStringInContent(Connection con, String current, String replacement) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(REPLACE_IN_PAGE_SQL);
            pst.setString(1, current);
            pst.setString(2, replacement);
            pst.executeUpdate();
        } finally {
            closeStatement(pst);
        }
    }

    private static final String READ_PARTS_SQL = "SELECT type,section,position,id,change_date FROM t_page_part WHERE page_id=? ORDER BY position";

    public void readParts(Connection con, PageData contentData) throws SQLException {
        PreparedStatement pst = null;
        PagePartData part;
        try {
            pst = con.prepareStatement(READ_PARTS_SQL);
            pst.setString(1, contentData.getId());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int i = 1;
                    String type = rs.getString(i++);
                    part = getNewPagePartData(type);
                    if (part != null) {
                        part.setSectionName(rs.getString(i++));
                        part.setPosition(rs.getInt(i++));
                        part.setId(rs.getString(i++));
                        part.setChangeDate(rs.getTimestamp(i).toLocalDateTime());
                        PagePartBean extBean = part.getBean();
                        if (extBean != null)
                            extBean.readPartExtras(con, part);
                        //todo?
                        contentData.addPart(part, "", false);
                    }
                }
            }
        } finally {
            closeStatement(pst);
        }
    }

    private static final String GET_PART_IDS_SQL = "SELECT id FROM t_page_part where page_id=?";
    private static final String INSERT_PART_SQL = "INSERT INTO t_page_part (type,change_date,page_id,section,position,id) VALUES(?,?,?,?,?,?)";
    private static final String UPDATE_PART_SQL = "UPDATE t_page_part SET type=?,change_date=?,page_id=?,section=?,position=? WHERE id=?";
    private static final String DELETE_PART_SQL = "DELETE FROM t_page_part WHERE id=?";

    public void writeAllParts(Connection con, PageData page) throws SQLException {
        PreparedStatement pstIds = null;
        PreparedStatement pstIns = null;
        PreparedStatement pstUpd = null;
        PreparedStatement pstDel = null;
        PreparedStatement pst;
        Set<Integer> ids=new HashSet<>();
        try {
            pstIds = con.prepareStatement(GET_PART_IDS_SQL);
            pstIds.setString(1,page.getId());
            ResultSet rs= pstIds.executeQuery();
            while (rs.next())
                ids.add(rs.getInt(1));
            pstIns = con.prepareStatement(INSERT_PART_SQL);
            pstUpd = con.prepareStatement(UPDATE_PART_SQL);
            for (SectionData section : page.getSections().values()) {
                for (PagePartData part : section.getParts()) {
                    ids.remove(part.getId());
                    part.setChangeDate(page.getChangeDate());
                    pst = part.isNew() ? pstIns : pstUpd;
                    int i = 1;
                    pst.setString(i++, part.getClass().getName());
                    pst.setTimestamp(i++, Timestamp.valueOf(part.getChangeDate()));
                    pst.setString(i++, page.getId());
                    pst.setString(i++, part.getSectionName());
                    pst.setInt(i++, part.getPosition());
                    pst.setString(i, part.getId());
                    pst.executeUpdate();
                    PagePartBean extBean = part.getBean();
                    if (extBean != null)
                        extBean.writePartExtras(con, part);
                }
            }
            pstDel = con.prepareStatement(DELETE_PART_SQL);
            for (int id : ids){
                pstDel.setInt(1, id);
                pstDel.executeUpdate();
            }
        } finally {
            closeStatement(pstIds);
            closeStatement(pstIns);
            closeStatement(pstUpd);
            closeStatement(pstDel);
        }
    }

    public boolean deletePart(int id) {
        Connection con = getConnection();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(DELETE_PART_SQL);
            pst.setInt(1, id);
            pst.executeUpdate();
            return true;
        } catch (SQLException se) {
            Log.error("sql error", se);
            return false;
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
    }
}
