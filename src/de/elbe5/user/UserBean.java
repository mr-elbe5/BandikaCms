/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.user;

import de.elbe5.base.Log;
import de.elbe5.application.Configuration;
import de.elbe5.database.DbBean;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class UserBean is the persistence class for users and groups. <br>
 * Usage:
 */
public class UserBean extends DbBean {

    private static UserBean instance = null;

    public static UserBean getInstance() {
        if (instance == null) {
            instance = new UserBean();
        }
        return instance;
    }

    public int getNextId() {
        return getNextId("s_user_id");
    }

    private static final String GET_ALL_USERS_SQL = "SELECT id,creator_id,changer_id,creation_date,change_date,name,login,editor,admin,active FROM t_user";

    public List<UserData> getAllUsers() {
        List<UserData> list = new ArrayList<>();
        Connection con = getConnection();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_ALL_USERS_SQL);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    UserData data = readUserData(rs);
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

    public UserData getUser(int id) {
        UserData data = null;
        Connection con = getConnection();
        try {
            data = readUser(con, id);
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeConnection(con);
        }
        return data;
    }

    public <T extends UserData> T getUser(int id, Class<T> cls) {
        try {
            return cls.cast(getUser(id));
        }
        catch(NullPointerException | ClassCastException e){
            return null;
        }
    }

    private static final String GET_USER_SQL = "SELECT id,creator_id,changer_id,creation_date,change_date,name,login,editor,admin,active FROM t_user WHERE id=?";

    public UserData readUser(Connection con, int id) throws SQLException {
        UserData data = null;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_USER_SQL);
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    data = readUserData(rs);
                }
            }
        } finally {
            closeStatement(pst);
        }
        return data;
    }

    private UserData readUserData(ResultSet rs) throws SQLException {
        int i = 1;
        UserData data = new UserData();
        data.setId(rs.getInt(i++));
        data.setCreatorId(rs.getInt(i++));
        data.setChangerId(rs.getInt(i++));
        data.setCreationDate(rs.getTimestamp(i++).toLocalDateTime());
        data.setChangeDate(rs.getTimestamp(i++).toLocalDateTime());
        data.setName(rs.getString(i++));
        data.setLogin(rs.getString(i++));
        data.setPassword("");
        data.setActive(rs.getBoolean(i++));
        data.setAdmin(rs.getBoolean(i++));
        data.setActive(rs.getBoolean(i));
        return data;
    }

    private static final String LOGIN_SQL = "SELECT pwd,id,creator_id,changer_id,creation_date,change_date,name,editor,admin FROM t_user WHERE login=? AND active=TRUE";

    public UserData loginUser(String login, String pwd) {
        Connection con = getConnection();
        PreparedStatement pst = null;
        UserData data = null;
        try {
            pst = con.prepareStatement(LOGIN_SQL);
            pst.setString(1, login);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int i = 1;
                    String encrypted = rs.getString(i++);
                    String test = UserSecurity.encryptPassword(pwd, Configuration.getSalt());
                    if (test != null && test.equals(encrypted)){
                        data = new UserData();
                        data.setId(rs.getInt(i++));
                        data.setLogin(login);
                        data.setCreatorId(rs.getInt(i++));
                        data.setChangerId(rs.getInt(i++));
                        data.setCreationDate(rs.getTimestamp(i++).toLocalDateTime());
                        data.setChangeDate(rs.getTimestamp(i++).toLocalDateTime());
                        data.setName(rs.getString(i++));
                        data.setEditor(rs.getBoolean(i++));
                        data.setAdmin(rs.getBoolean(i));
                        data.setActive(true);
                    }
                }
            }
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
        return data;
    }

    private static final String GET_X_LOGIN_SQL = "SELECT 'x' FROM t_user WHERE login=?";

    public boolean doesLoginExist(String login) {
        Connection con = getConnection();
        PreparedStatement pst = null;
        boolean exists = false;
        try {
            pst = con.prepareStatement(GET_X_LOGIN_SQL);
            pst.setString(1, login);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    exists = true;
                }
            }
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
        return exists;
    }

    public boolean saveUser(UserData data) {
        Connection con = startTransaction();
        try {
            if (data.isNew()){
                createUser(con,data);
            }
            else{
                updateUser(con,data);
            }
            return commitTransaction(con);
        } catch (Exception se) {
            return rollbackTransaction(con, se);
        }
    }

    private static final String INSERT_USER_SQL = "insert into t_user (type,creator_id,changer_id,creation_date,change_date,name,login,pwd,editor,admin,active,id) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

    protected void createUser(Connection con, UserData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(INSERT_USER_SQL);
            int i = 1;
            pst.setString(i++, data.getClass().getName());
            pst.setInt(i++, data.getCreatorId());
            pst.setInt(i++, data.getChangerId());
            pst.setTimestamp(i++, Timestamp.valueOf(data.getCreationDate()));
            pst.setTimestamp(i++, Timestamp.valueOf(data.getChangeDate()));
            pst.setString(i++, data.getName());
            pst.setString(i++, data.getLogin());
            if (data.hasPassword()) {
                pst.setString(i++, data.getPasswordHash());
            }
            pst.setBoolean(i++, data.isEditor());
            pst.setBoolean(i++, data.isAdmin());
            pst.setBoolean(i++, data.isActive());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }
    private static final String UPDATE_USER_PWD_SQL = "update t_user set changer_id=?,change_date=?,name=?,login=?,pwd=?,editor=?,admin=?,active=? where id=?";
    private static final String UPDATE_USER_NOPWD_SQL = "update t_user set changer_id=?,change_date=?,name=?,login=?,editor=?,admin=?,active=? where id=?";

    protected void updateUser(Connection con, UserData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(data.hasPassword() ? UPDATE_USER_PWD_SQL : UPDATE_USER_NOPWD_SQL);
            int i = 1;
            pst.setInt(i++, data.getChangerId());
            pst.setTimestamp(i++, Timestamp.valueOf(data.getChangeDate()));
            pst.setString(i++, data.getName());
            pst.setString(i++, data.getLogin());
            if (data.hasPassword()) {
                pst.setString(i++, data.getPasswordHash());
            }
            pst.setBoolean(i++, data.isEditor());
            pst.setBoolean(i++, data.isAdmin());
            pst.setBoolean(i++, data.isActive());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    public boolean saveUserPassword(UserData data) {
        Connection con = startTransaction();
        try {
            writeUserPassword(con, data);
            return commitTransaction(con);
        } catch (Exception se) {
            return rollbackTransaction(con, se);
        }
    }

    private static final String UPDATE_PASSWORD_SQL = "UPDATE t_user SET changer_id=?,change_date=?, pwd=? WHERE id=?";

    protected void writeUserPassword(Connection con, UserData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(UPDATE_PASSWORD_SQL);
            int i = 1;
            pst.setInt(i++, data.getChangerId());
            pst.setTimestamp(i++, Timestamp.valueOf(data.getChangeDate()));
            pst.setString(i++, data.getPasswordHash());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    private static final String DELETE_USER_SQL = "delete from t_user WHERE id=?";

    public boolean deleteUser(int id) {
        Connection con = getConnection();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(DELETE_USER_SQL);
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException se) {
            Log.error("user caanot be deleted", se);
            return false;
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
        return true;
    }

}
