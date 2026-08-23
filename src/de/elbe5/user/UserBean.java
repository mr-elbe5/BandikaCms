/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.user;

import de.elbe5.base.Log;
import de.elbe5.base.StringFormatter;
import de.elbe5.application.Configuration;
import de.elbe5.database.DbBean;
import de.elbe5.rights.GlobalRight;

import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    UserData getNewUserData(String className){
        try {
            Class<? extends UserData> cls = Class.forName(className).asSubclass(UserData.class);
            Constructor<? extends UserData> ctor = cls.getConstructor();
            return ctor.newInstance();
        }
        catch(Exception e){
            Log.error("could not create class " + className,  e);
        }
        return null;
    }

    public int getNextId() {
        return getNextId("s_user_id");
    }

    private static final String CHANGED_SQL = "SELECT change_date FROM t_user WHERE id=?";

    private static final String GET_ALL_USERS_SQL = "SELECT type,id,creator_id,changer_id,creation_date,change_date,name,email,login,active FROM t_user";

    public List<UserData> getAllUsers() {
        List<UserData> list = new ArrayList<>();
        Connection con = getConnection();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_ALL_USERS_SQL);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    UserData data = readUserData(rs);
                    if (data != null) {
                        UserBean extBean = data.getBean();
                        if (extBean != null)
                            extBean.readUserExtras(con, data);
                        readUserGroups(con, data);
                        readUserRights(con,data);
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

    public UserData getUser(int id) {
        UserData data = null;
        Connection con = getConnection();
        try {
            data = readUser(con, id);
            UserBean extBean = data.getBean();
            if (extBean != null)
                extBean.readUserExtras(con, data);
            readUserGroups(con, data);
            readUserRights(con,data);
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

    private static final String GET_USER_SQL = "SELECT type,id,creator_id,changer_id,creation_date,change_date,name,email,login,active FROM t_user WHERE id=?";

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

    public void readUserExtras(Connection con, UserData userData) throws SQLException{
    }

    private UserData readUserData(ResultSet rs) throws SQLException {
        int i = 1;
        String type = rs.getString(i++);
        UserData data = getNewUserData(type);
        if (data!=null) {
            data.setId(rs.getInt(i++));
            data.setCreatorId(rs.getInt(i++));
            data.setChangerId(rs.getInt(i++));
            data.setCreationDate(rs.getTimestamp(i++).toLocalDateTime());
            data.setChangeDate(rs.getTimestamp(i++).toLocalDateTime());
            data.setName(rs.getString(i++));
            data.setEmail(rs.getString(i++));
            data.setLogin(rs.getString(i++));
            data.setPassword("");
            data.setActive(rs.getBoolean(i));
        }
        return data;
    }

    private static final String LOGIN_SQL = "SELECT pwd,type,id,creator_id,changer_id,creation_date,change_date,name,email FROM t_user WHERE login=? AND active=TRUE";

    public UserData loginUser(String login, String pwd) {
        Connection con = getConnection();
        PreparedStatement pst = null;
        UserData data = null;
        try {
            pst = con.prepareStatement(LOGIN_SQL);
            pst.setString(1, login);
            boolean passed;
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int i = 1;
                    String encrypted = rs.getString(i++);
                    if (UserSecurity.encryptPassword(pwd, Configuration.getSalt()).equals(encrypted)){
                        String type = rs.getString(i++);
                        data = getNewUserData(type);
                        if (data!=null) {
                            data.setId(rs.getInt(i++));
                            data.setLogin(login);
                            data.setCreatorId(rs.getInt(i++));
                            data.setChangerId(rs.getInt(i++));
                            data.setCreationDate(rs.getTimestamp(i++).toLocalDateTime());
                            data.setChangeDate(rs.getTimestamp(i++).toLocalDateTime());
                            data.setName(rs.getString(i++));
                            data.setEmail(rs.getString(i));
                            data.setActive(true);
                            UserBean extBean = data.getBean();
                            if (extBean != null)
                                extBean.readUserExtras(con, data);
                            readUserGroups(con, data);
                            readUserRights(con, data);
                        }
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

    private static final String API_LOGIN_SQL = "SELECT pwd,type,id,creator_id,changer_id,creation_date,change_date,name,email,token FROM t_user WHERE login=? AND active=TRUE";

    //todo
    public UserData loginApiUser(String login, String pwd) {
        Connection con = getConnection();
        PreparedStatement pst = null;
        UserData data = null;
        try {
            pst = con.prepareStatement(API_LOGIN_SQL);
            pst.setString(1, login);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int i = 1;
                    String encrypted = rs.getString(i++);
                    String encryptedLogin = UserSecurity.encryptPassword(pwd, Configuration.getSalt());
                    if (encryptedLogin != null && encryptedLogin.equals(encrypted)) {
                        String type = rs.getString(i++);
                        data = getNewUserData(type);
                        if (data!=null) {
                            data = new UserData();
                            data.setId(rs.getInt(i++));
                            data.setLogin(login);
                            data.setCreatorId(rs.getInt(i++));
                            data.setChangerId(rs.getInt(i++));
                            data.setCreationDate(rs.getTimestamp(i++).toLocalDateTime());
                            data.setChangeDate(rs.getTimestamp(i++).toLocalDateTime());
                            data.setName(rs.getString(i++));
                            data.setEmail(rs.getString(i++));
                            data.setToken(rs.getString(i));
                            data.setActive(true);
                            UserBean extBean = data.getBean();
                            if (extBean != null)
                                extBean.readUserExtras(con, data);
                            readUserGroups(con, data);
                            readUserRights(con, data);
                        }
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

    private static final String SET_TOKEN_SQL = "UPDATE t_user SET token=? WHERE id=?";

    public boolean setToken(UserData data){
        Connection con = startTransaction();
        PreparedStatement pst;
        try {
            String token=UUID.randomUUID().toString();
            pst = con.prepareStatement(SET_TOKEN_SQL);
            pst.setString(1, token);
            pst.setInt(2, data.getId());
            pst.executeUpdate();
            pst.close();
            data.setToken(token);
            return commitTransaction(con);
        } catch (Exception se) {
            return rollbackTransaction(con, se);
        }
    }

    private static final String LOGIN_BY_TOKEN_SQL = "SELECT type,id,login,creator_id,changer_id,creation_date,change_date,name,email FROM t_user WHERE token=? AND active=TRUE";

    public UserData loginUserByToken(String token) {
        Connection con = getConnection();
        PreparedStatement pst = null;
        UserData data = null;
        try {
            pst = con.prepareStatement(LOGIN_BY_TOKEN_SQL);
            pst.setString(1, token);
            boolean passed;
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int i = 1;
                    String type = rs.getString(i++);
                    data = getNewUserData(type);
                    if (data!=null) {
                        data.setId(rs.getInt(i++));
                        data.setLogin(rs.getString(i++));
                        data.setCreatorId(rs.getInt(i++));
                        data.setChangerId(rs.getInt(i++));
                        data.setCreationDate(rs.getTimestamp(i++).toLocalDateTime());
                        data.setChangeDate(rs.getTimestamp(i++).toLocalDateTime());
                        data.setName(rs.getString(i++));
                        data.setEmail(rs.getString(i));
                        data.setActive(true);
                        UserBean extBean = data.getBean();
                        if (extBean != null)
                            extBean.readUserExtras(con, data);
                        readUserGroups(con, data);
                        readUserRights(con, data);
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

    private static final String GET_X_EMAIL_SQL = "SELECT 'x' FROM t_user WHERE email=?";

    public boolean doesEmailExist(String login) {
        Connection con = getConnection();
        PreparedStatement pst = null;
        boolean exists = false;
        try {
            pst = con.prepareStatement(GET_X_EMAIL_SQL);
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

    private static final String READ_USER_GROUPS_SQL = "SELECT group_id FROM t_user2group WHERE user_id=?";

    protected void readUserGroups(Connection con, UserData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(READ_USER_GROUPS_SQL);
            pst.setInt(1, data.getId());
            try (ResultSet rs = pst.executeQuery()) {
                data.getGroupIds().clear();
                while (rs.next()) {
                    data.getGroupIds().add(rs.getInt(1));
                }
            }
        } finally {
            closeStatement(pst);
        }
    }

    public boolean saveUser(UserData data) {
        Connection con = startTransaction();
        try {
            UserBean extrasBean = data.getBean();
            if (data.isNew()){
                createUser(con,data);
                if (extrasBean != null)
                    extrasBean.createUserExtras(con, data);
            }
            else{
                updateUser(con,data);
                if (extrasBean != null)
                    extrasBean.updateUserExtras(con, data);
            }
            writeUserGroups(con, data);
            return commitTransaction(con);
        } catch (Exception se) {
            return rollbackTransaction(con, se);
        }
    }

    private static final String INSERT_USER_SQL = "insert into t_user (type,creator_id,changer_id,creation_date,change_date,name,email,login,pwd,active,id) values(?,?,?,?,?,?,?,?,?,?,?)";

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
            pst.setString(i++, data.getEmail());
            pst.setString(i++, data.getLogin());
            if (data.hasPassword()) {
                pst.setString(i++, data.getPasswordHash());
            }
            pst.setBoolean(i++, data.isActive());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }
    private static final String UPDATE_USER_PWD_SQL = "update t_user set changer_id=?,change_date=?,name=?,email=?,login=?,pwd=?,active=? where id=?";
    private static final String UPDATE_USER_NOPWD_SQL = "update t_user set changer_id=?,change_date=?,name=?,email=?,login=?,active=? where id=?";

    protected void updateUser(Connection con, UserData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(data.hasPassword() ? UPDATE_USER_PWD_SQL : UPDATE_USER_NOPWD_SQL);
            int i = 1;
            pst.setInt(i++, data.getChangerId());
            pst.setTimestamp(i++, Timestamp.valueOf(data.getChangeDate()));
            pst.setString(i++, data.getName());
            pst.setString(i++, data.getEmail());
            pst.setString(i++, data.getLogin());
            if (data.hasPassword()) {
                pst.setString(i++, data.getPasswordHash());
            }
            pst.setBoolean(i++, data.isActive());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    public void createUserExtras(Connection con, UserData userData) throws SQLException{
    }

    public void updateUserExtras(Connection con, UserData userData) throws SQLException{
    }

    public boolean saveUserProfile(UserData data) {
        Connection con = startTransaction();
        try {
            data.setChangeDate(getServerTime(con));
            writeUserProfile(con, data);
            UserBean extrasBean = data.getBean();
            if (extrasBean != null)
                extrasBean.updateProfileExtras(con, data);
            return commitTransaction(con);
        } catch (Exception se) {
            return rollbackTransaction(con, se);
        }
    }

    private static final String UPDATE_PROFILE_SQL = "UPDATE t_user SET changer_id=?,change_date=?,name=?,email=? WHERE id=?";

    protected void writeUserProfile(Connection con, UserData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(UPDATE_PROFILE_SQL);
            int i = 1;
            pst.setInt(i++, data.getChangerId());
            pst.setTimestamp(i++, Timestamp.valueOf(data.getChangeDate()));
            pst.setString(i++, data.getName());
            pst.setString(i++, data.getEmail());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    public void updateProfileExtras(Connection con, UserData userData) throws SQLException{
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

    private static final String DELETE_USERGROUPS_SQL = "DELETE FROM t_user2group WHERE user_id=?";
    private static final String INSERT_USERGROUP_SQL = "INSERT INTO t_user2group (user_id,group_id) VALUES(?,?)";

    protected void writeUserGroups(Connection con, UserData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(DELETE_USERGROUPS_SQL);
            pst.setInt(1, data.getId());
            pst.execute();
            if (data.getGroupIds() != null) {
                pst.close();
                pst = con.prepareStatement(INSERT_USERGROUP_SQL);
                pst.setInt(1, data.getId());
                for (int groupId : data.getGroupIds()) {
                    pst.setInt(2, groupId);
                    pst.executeUpdate();
                }
            }
        } finally {
            closeStatement(pst);
        }
    }

    private static final String DELETE_USER_SQL = "delete from t_user WHERE id=?";
    private static final String DELETE_ALL_USERGROUPS_SQL = "DELETE FROM t_user2group WHERE user_id=?";

    public boolean deleteUser(int id) {
        Connection con = getConnection();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(DELETE_USER_SQL);
            pst.setInt(1, id);
            pst.executeUpdate();
            pst.close();
            pst = con.prepareStatement(DELETE_ALL_USERGROUPS_SQL);
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

    private static final String GET_SYSTEM_RIGHTS_SQL = "select name from t_system_right where group_id in({1})";

    public void readUserRights(Connection con, UserData data) {
        data.clearGlobalRights();
        PreparedStatement pst = null;
        try {
            if (data.getGroupIds().isEmpty()) {
                return;
            }
            StringBuilder buffer = new StringBuilder();
            for (int id : data.getGroupIds()) {
                if (buffer.length() > 0) {
                    buffer.append(',');
                }
                buffer.append(id);
            }
            pst = con.prepareStatement(StringFormatter.format(GET_SYSTEM_RIGHTS_SQL, buffer.toString()));
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                data.addGlobalRight(GlobalRight.valueOf(rs.getString(1)));
            }
            rs.close();
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeStatement(pst);
        }
    }

}
