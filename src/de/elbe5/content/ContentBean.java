/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.content;

import de.elbe5.base.DateHelper;
import de.elbe5.base.Log;
import de.elbe5.database.DbBean;

import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.*;

public class ContentBean extends DbBean {

    private static ContentBean instance = null;

    public static ContentBean getInstance() {
        if (instance == null) {
            instance = new ContentBean();
        }
        return instance;
    }

    ContentData getNewContentData(String className){
        try {
            Class<? extends ContentData> cls = Class.forName(className).asSubclass(ContentData.class);
            Constructor<? extends ContentData> ctor = cls.getConstructor();
            return ctor.newInstance();
        }
        catch(Exception e){
            Log.error("could not create class " + className,  e);
        }
        return null;
    }

    public int getNextId() {
        return getNextId("s_content_id");
    }

    private static final String GET_ALL_CONTENT_SQL = "SELECT type,id,creator_id,changer_id,creation_date,change_date,parent_id,ranking,name,display_name,description,open_access,reader_group_id,editor_group_id,nav_type,active FROM t_content";

    public List<ContentData> getAllContents() {
        List<ContentData> list = new ArrayList<>();
        Connection con = getConnection();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_ALL_CONTENT_SQL);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ContentData data=readContentData(rs);
                    if (data!=null) {
                        ContentBean extBean = data.getBean();
                        if (extBean != null)
                            extBean.readContentExtras(con, data);
                        list.add(data);
                    }
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

    public ContentData getContent(int id) {
        ContentData data = null;
        Connection con = getConnection();
        try {
            data = readContent(con, id);
            ContentBean extBean = data.getBean();
            if (extBean != null)
                extBean.readContentExtras(con, data);
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeConnection(con);
        }
        return data;
    }

    public <T extends ContentData> T getContent(int id, Class<T> cls) {
        try {
            return cls.cast(getContent(id));
        }
        catch(NullPointerException | ClassCastException e){
            return null;
        }
    }

    private static final String GET_CONTENT_SQL = "SELECT type,id,creator_id,changer_id,creation_date,change_date,parent_id,ranking,name,display_name,description,open_access,reader_group_id,editor_group_id,nav_type,active FROM t_content WHERE id=?";

    public ContentData readContent(Connection con, int id) throws SQLException {
        ContentData data = null;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_CONTENT_SQL);
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    data = readContentData(rs);
                }
            }
        } finally {
            closeStatement(pst);
        }
        return data;
    }

    public void readContentExtras(Connection con, ContentData contentData) throws SQLException{
    }

    private ContentData readContentData(ResultSet rs) throws SQLException{
        int i = 1;
        String type = rs.getString(i++);
        ContentData data = getNewContentData(type);
        if (data != null) {
            data.setId(rs.getInt(i++));
            data.setCreatorId(rs.getInt(i++));
            data.setChangerId(rs.getInt(i++));
            data.setCreationDate(rs.getTimestamp(i++).toLocalDateTime());
            data.setChangeDate(rs.getTimestamp(i++).toLocalDateTime());
            data.setParentId(rs.getInt(i++));
            data.setRanking(rs.getInt(i++));
            data.setName(rs.getString(i++));
            data.setDisplayName(rs.getString(i++));
            data.setDescription(rs.getString(i++));
            data.setOpenAccess(rs.getBoolean(i++));
            data.setReaderGroupId(rs.getInt(i++));
            data.setEditorGroupId(rs.getInt(i++));
            data.setNavType(rs.getString(i++));
            data.setActive(rs.getBoolean(i));
        }
        return data;
    }

    public boolean saveContent(ContentData data) {
        Connection con = startTransaction();
        try {
            ContentBean extrasBean = data.getBean();
            data.setChangeDate(DateHelper.getCurrentTime());
            if (data.isNew()){
                data.setCreationDate(data.getChangeDate());
                createContent(con,data);
                if (extrasBean != null)
                    extrasBean.createContentExtras(con, data);
            }
            else{
                updateContent(con,data);
                if (extrasBean != null)
                    extrasBean.updateContentExtras(con, data);
            }
            return commitTransaction(con);
        } catch (Exception se) {
            return rollbackTransaction(con, se);
        }
    }

    private static final String INSERT_CONTENT_SQL = "insert into t_content (type,creator_id,changer_id,creation_date,change_date,parent_id,ranking,name,display_name,description,open_access,reader_group_id,editor_group_id,nav_type,active,id) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    protected void createContent(Connection con, ContentData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(INSERT_CONTENT_SQL);
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
            pst.setString(i++, data.getDescription());
            pst.setBoolean(i++, data.isOpenAccess());
            if (data.getReaderGroupId() == 0) {
                pst.setNull(i++, Types.INTEGER);
            } else {
                pst.setInt(i++, data.getReaderGroupId());
            }
            if (data.getEditorGroupId() == 0) {
                pst.setNull(i++, Types.INTEGER);
            } else {
                pst.setInt(i++, data.getEditorGroupId());
            }
            pst.setString(i++, data.getNavTypeString());
            pst.setBoolean(i++,data.isActive());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    private static final String UPDATE_CONTENT_SQL = "update t_content set changer_id=?,change_date=?,parent_id=?,ranking=?,name=?,display_name=?,description=?,open_access=?,reader_group_id=?,editor_group_id=?,nav_type=?,active=? where id=?";

    protected void updateContent(Connection con, ContentData data) throws SQLException {
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
            pst.setString(i++, data.getDescription());
            pst.setBoolean(i++, data.isOpenAccess());
            if (data.getReaderGroupId() == 0) {
                pst.setNull(i++, Types.INTEGER);
            } else {
                pst.setInt(i++, data.getReaderGroupId());
            }
            if (data.getEditorGroupId() == 0) {
                pst.setNull(i++, Types.INTEGER);
            } else {
                pst.setInt(i++, data.getEditorGroupId());
            }
            pst.setString(i++, data.getNavTypeString());
            pst.setBoolean(i++,data.isActive());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    public void createContentExtras(Connection con, ContentData contentData) throws SQLException{
    }

    public void updateContentExtras(Connection con, ContentData contentData) throws SQLException{
    }

    private static final String UPDATE_RANKING_SQL = "UPDATE t_content SET ranking=? WHERE id=?";

    public void updateChildRankings(ContentData data) {
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

    private static final String DELETE_SQL = "DELETE FROM t_content WHERE id=?";

    public boolean deleteContent(int id) {
        return deleteItem(DELETE_SQL, id);
    }

}
