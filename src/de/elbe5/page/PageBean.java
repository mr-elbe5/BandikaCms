/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.base.DateHelper;
import de.elbe5.base.Log;
import de.elbe5.database.DbBean;

import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.*;

public class PageBean extends DbBean {

    private static PageBean instance = null;

    public static PageBean getInstance() {
        if (instance == null) {
            instance = new PageBean();
        }
        return instance;
    }

    public int getNextId() {
        return getNextId("s_content_id");
    }

    private static final String GET_ALL_CONTENT_SQL = "SELECT id,creator_id,changer_id,creation_date,change_date,parent_id,ranking,name,display_name,nav_type,active,keywords,layout,publish_date,published_content FROM t_page";

    public List<PageData> getAllPages() {
        List<PageData> list = new ArrayList<>();
        Connection con = getConnection();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_ALL_CONTENT_SQL);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    PageData data= readPageData(rs);
                    readParts(con, data);
                    data.sortParts();
                    list.add(data);
                }
            }
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
        return list;
    }

    public PageData getPage(int id) {
        PageData data = null;
        Connection con = getConnection();
        try {
            data = readPage(con, id);
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeConnection(con);
        }
        return data;
    }

    private static final String GET_PAGE_SQL = "SELECT id,creator_id,changer_id,creation_date,change_date,parent_id,ranking,name,display_name,nav_type,active,keywords,layout,publish_date,published_content FROM t_page WHERE id=?";

    public PageData readPage(Connection con, int id) throws SQLException {
        PageData data = null;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_PAGE_SQL);
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    data = readPageData(rs);
                    readParts(con, data);
                    data.sortParts();
                }
            }
        } finally {
            closeStatement(pst);
        }
        return data;
    }

    private PageData readPageData(ResultSet rs) throws SQLException{
        int i = 1;
        PageData data = new PageData();
        data.setId(rs.getInt(i++));
        data.setCreatorId(rs.getInt(i++));
        data.setChangerId(rs.getInt(i++));
        data.setCreationDate(rs.getTimestamp(i++).toLocalDateTime());
        data.setChangeDate(rs.getTimestamp(i++).toLocalDateTime());
        data.setParentId(rs.getInt(i++));
        data.setRanking(rs.getInt(i++));
        data.setName(rs.getString(i++));
        data.setDisplayName(rs.getString(i++));
        data.setNavType(rs.getString(i++));
        data.setActive(rs.getBoolean(i));
        data.setKeywords(rs.getString(i++));
        data.setLayout(rs.getString(i++));
        Timestamp ts = rs.getTimestamp(i++);
        data.setPublishDate(ts == null ? null : ts.toLocalDateTime());
        data.setPublishedContent(rs.getString(i));
        return data;
    }

    public boolean savePage(PageData data) {
        Connection con = startTransaction();
        try {
            data.setChangeDate(DateHelper.getCurrentTime());
            if (data.isNew()){
                data.setCreationDate(data.getChangeDate());
                createContent(con,data);
            }
            else{
                updateContent(con,data);
            }
            return commitTransaction(con);
        } catch (Exception se) {
            return rollbackTransaction(con, se);
        }
    }

    private static final String INSERT_PAGE_SQL = "insert into t_page (creator_id,changer_id,creation_date,change_date,parent_id,ranking,name,display_name,nav_type,active,keywords,layout,publish_date,published_content,id) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    protected void createContent(Connection con, PageData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(INSERT_PAGE_SQL);
            int i = 1;
            pst.setString(i++, data.getClass().getName());
            pst.setInt(i++, data.getCreatorId());
            pst.setInt(i++, data.getChangerId());
            pst.setTimestamp(i++, Timestamp.valueOf(data.getCreationDate()));
            pst.setTimestamp(i++, Timestamp.valueOf(data.getChangeDate()));
            if (data.getParentId() == 0) {
                pst.setNull(i++, Types.INTEGER);
            } else {
                pst.setInt(i++, data.getParentId());
            }
            pst.setInt(i++, data.getRanking());
            pst.setString(i++, data.getName());
            pst.setString(i++, data.getDisplayName());
            pst.setString(i++, data.getNavTypeString());
            pst.setBoolean(i++,data.isActive());
            pst.setString(i++, data.getKeywords());
            pst.setString(i++, data.getLayout());
            if (data.getPublishDate()==null)
                pst.setNull(i++,Types.TIMESTAMP);
            else
                pst.setTimestamp(i++, Timestamp.valueOf(data.getPublishDate()));
            pst.setString(i++,data.getPublishedContent());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    private static final String UPDATE_CONTENT_SQL = "update t_page set changer_id=?,change_date=?,parent_id=?,ranking=?,name=?,display_name=?,nav_type=?,active=?,keywords=?,layout=?,publish_date=?,published_content=? where id=?";

    protected void updateContent(Connection con, PageData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(UPDATE_CONTENT_SQL);
            int i = 1;
            pst.setInt(i++, data.getChangerId());
            pst.setTimestamp(i++, Timestamp.valueOf(data.getChangeDate()));
            if (data.getParentId() == 0){
                pst.setNull(i++, Types.INTEGER);
            }
            else{
                pst.setInt(i++, data.getParentId());
            }
            pst.setInt(i++, data.getRanking());
            pst.setString(i++, data.getName());
            pst.setString(i++, data.getDisplayName());
            pst.setString(i++, data.getNavTypeString());
            pst.setBoolean(i++,data.isActive());
            pst.setString(i++, data.getKeywords());
            pst.setString(i++, data.getLayout());
            if (data.getPublishDate()==null)
                pst.setNull(i++,Types.TIMESTAMP);
            else
                pst.setTimestamp(i++, Timestamp.valueOf(data.getPublishDate()));
            pst.setString(i++,data.getPublishedContent());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    private static final String UPDATE_RANKING_SQL = "UPDATE t_page SET ranking=? WHERE id=?";

    public void updateChildRankings(PageData data) {
        Connection con = startTransaction();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(UPDATE_RANKING_SQL);
            for (int i = 0; i < data.getChildren().size(); i++) {
                int id = data.getChildren().get(i).getId();
                pst.setInt(1, i + 1);
                pst.setInt(2, id);
                pst.executeUpdate();
            }
            commitTransaction(con);
        } catch (Exception e){
            rollbackTransaction(con);
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
    }

    private static final String DELETE_SQL = "DELETE FROM t_page WHERE id=?";

    public boolean deletePage(int id) {
        return deleteItem(DELETE_SQL, id);
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

    public boolean publishPage(PageData data) {
        Connection con = startTransaction();
        try {
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
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    private static final String REPLACE_IN_PAGE_SQL = "UPDATE t_page set published_content = REPLACE(published_content,?,?)";

    public void replaceStringInPage(Connection con, String current, String replacement) throws SQLException {
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

    private static final String READ_PARTS_SQL = "SELECT section,position,id,change_date,layout FROM t_page_part WHERE page_id=? ORDER BY position";

    public void readParts(Connection con, PageData contentData) throws SQLException {
        PreparedStatement pst = null;
        PagePartData part;
        try {
            pst = con.prepareStatement(READ_PARTS_SQL);
            pst.setInt(1, contentData.getId());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int i = 1;
                    String type = rs.getString(i++);
                    part = getNewPagePartData(type);
                    if (part != null) {
                        part.setSectionName(rs.getString(i++));
                        part.setPosition(rs.getInt(i++));
                        part.setId(rs.getInt(i++));
                        part.setChangeDate(rs.getTimestamp(i).toLocalDateTime());
                        part.setLayout(rs.getString(i));
                        readAllPartFields(con, part);
                        contentData.addPart(part, -1, false);
                    }
                }
            }
        } finally {
            closeStatement(pst);
        }
    }

    private static final String READ_PART_FIELDS_SQL = "SELECT field_type, name, content FROM t_part_field WHERE part_id=?";

    public void readAllPartFields(Connection con, PagePartData data) throws SQLException {
        PreparedStatement pst = null;
        PartField field;
        data.getFields().clear();
        try {
            pst = con.prepareStatement(READ_PART_FIELDS_SQL);
            pst.setInt(1, data.getId());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int i = 1;
                    String fieldType = rs.getString(i++);
                    field = new PartField();
                    field.setPartId(data.getId());
                    field.setName(rs.getString(i++));
                    field.setContent(rs.getString(i));
                    data.getFields().put(field.getName(), field);
                }
            }
        } finally {
            closeStatement(pst);
        }
    }

    private static final String GET_PART_IDS_SQL = "SELECT id,layout FROM t_page_part where page_id=?";
    private static final String INSERT_PART_SQL = "INSERT INTO t_page_part (change_date,page_id,section,position,layout,id) VALUES(?,?,?,?,?,?)";
    private static final String UPDATE_PART_SQL = "UPDATE t_page_part SET change_date=?,page_id=?,section=?,position=?,layout=? WHERE id=?";
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
            pstIds.setInt(1,page.getId());
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
                    pst.setInt(i++, page.getId());
                    pst.setString(i++, part.getSectionName());
                    pst.setInt(i++, part.getPosition());
                    pst.setString(i++, part.getLayout());
                    pst.setInt(i, part.getId());
                    pst.executeUpdate();
                    writeAllPartFields(con, part);
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

    private static final String DELETE_PART_FIELDS_SQL = "DELETE FROM t_part_field WHERE part_id=?";
    private static final String INSERT_PART_FIELD_SQL = "INSERT INTO t_part_field (field_type,name,content,part_id) VALUES(?,?,?,?)";

    public void writeAllPartFields(Connection con, PagePartData part) throws SQLException {
        PreparedStatement pstDelFields = null;
        PreparedStatement pstIns = null;
        try {
            pstDelFields = con.prepareStatement(DELETE_PART_FIELDS_SQL);
            pstDelFields.setInt(1, part.getId());
            pstDelFields.executeUpdate();
            pstDelFields.close();
            pstIns = con.prepareStatement(INSERT_PART_FIELD_SQL);
            for (PartField field : part.getFields().values()) {
                int i = 1;
                pstIns.setString(i++, field.getName());
                pstIns.setString(i++, field.getContent());
                pstIns.setInt(i, part.getId());
                pstIns.executeUpdate();
            }
        } finally {
            closeStatement(pstDelFields);
            closeStatement(pstIns);
        }
    }

    private static final String REPLACE_IN_FIELD_SQL = "UPDATE t_part_field set content = REPLACE(content,?,?)";

    public void replaceStringInPart(Connection con, String current, String replacement) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(REPLACE_IN_FIELD_SQL);
            pst.setString(1, current);
            pst.setString(2, replacement);
            pst.executeUpdate();
        } finally {
            closeStatement(pst);
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
