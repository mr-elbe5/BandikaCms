/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.content;

import de.elbe5.base.Log;
import de.elbe5.database.DbBean;
import de.elbe5.rights.Right;

import java.lang.reflect.Constructor;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentLogBean extends DbBean {

    private static ContentLogBean instance = null;

    public static ContentLogBean getInstance() {
        if (instance == null) {
            instance = new ContentLogBean();
        }
        return instance;
    }

    private static final String GET_ALL_VIEW_COUNTS_SQL = "SELECT day, content_id, count FROM t_content_log ORDER BY day, content_id";

    public List<ContentDayLog> getAllViewCounts() {
        Connection con = getConnection();
        PreparedStatement pst = null;
        List<ContentDayLog> logs = new ArrayList<>();
        try {
            pst = con.prepareStatement(GET_ALL_VIEW_COUNTS_SQL);
            ContentDayLog dayLog = null;
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                LocalDate date = rs.getDate(1).toLocalDate();
                if (dayLog == null || !dayLog.getDay().equals(date)){
                    dayLog = new ContentDayLog();
                    dayLog.setDay(date);
                    logs.add(dayLog);
                }
                ContentLog log = new ContentLog();
                log.setId(rs.getInt(2));
                log.setCount(rs.getInt(3));
                dayLog.getLogs().add(log);
            }
            rs.close();
            return logs;
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
        return null;
    }

    private static final String GET_VIEW_COUNT_SQL = "SELECT count FROM t_content_log WHERE content_id=? and day=?";
    private static final String INSERT_VIEW_COUNT_SQL = "INSERT INTO t_content_log (count, content_id, day) VALUES(?,?,?)";
    private static final String UPDATE_VIEW_COUNT_SQL = "UPDATE t_content_log set count=? where content_id=? and day=?";

    private Date getDate(){
        LocalDate localDate = LocalDate.now();
        return Date.valueOf(localDate);
    }

    public boolean increaseViewCount(int id) {
        //Log.info("increase view count");
        Connection con = startTransaction();
        PreparedStatement pst = null;
        try {
            int count = 0;
            boolean newCount = true;
            Date date = getDate();
            pst = con.prepareStatement(GET_VIEW_COUNT_SQL);
            pst.setInt(1, id);
            pst.setDate(2, date);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
                newCount = false;
            }
            rs.close();
            pst.close();
            count++;
            pst = con.prepareStatement(newCount ? INSERT_VIEW_COUNT_SQL : UPDATE_VIEW_COUNT_SQL);
            pst.setInt(1, count);
            pst.setInt(2, id);
            pst.setDate(3, date);
            pst.executeUpdate();
            return commitTransaction(con);
        } catch (Exception se) {
            Log.error("sql error", se);
            return rollbackTransaction(con, se);
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
    }

    private static final String RESET_VIEW_COUNT_SQL = "DELETE FROM t_content_log";

    public boolean resetContentLog(){
        //Log.info("reset view count");
        Connection con = startTransaction();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(RESET_VIEW_COUNT_SQL);
            pst.executeUpdate();
            return commitTransaction(con);
        } catch (Exception se) {
            Log.error("sql error", se);
            return rollbackTransaction(con, se);
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
    }

}
