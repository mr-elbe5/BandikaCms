/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.link;

import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;

import java.sql.*;

public class LinkBean extends ContentBean {

    private static LinkBean instance = null;

    public static LinkBean getInstance() {
        if (instance == null) {
            instance = new LinkBean();
        }
        return instance;
    }

    private static final String GET_CONTENT_EXTRAS_SQL = "SELECT link_url,link_icon FROM t_link WHERE id=?";

    @Override
    public void readContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof LinkData))
            return;
        LinkData data = (LinkData) contentData;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_CONTENT_EXTRAS_SQL);
            pst.setInt(1, data.getId());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int i = 1;
                    data.setLinkUrl(rs.getString(i++));
                    data.setLinkIcon(rs.getString(i));
                }
            }
        } finally {
            closeStatement(pst);
        }
    }

    private static final String INSERT_CONTENT_EXTRAS_SQL = "insert into t_link (link_url,link_icon,id) values(?,?,?)";

    @Override
    public void createContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof LinkData))
            return;
        LinkData data = (LinkData) contentData;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(INSERT_CONTENT_EXTRAS_SQL);
            setExtraValues(pst,data);
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    private static final String UPDATE_CONTENT_EXTRAS_SQL = "update t_link set link_url=?,link_icon=? where id=?";

    @Override
    public void updateContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof LinkData))
            return;
        LinkData data = (LinkData) contentData;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(UPDATE_CONTENT_EXTRAS_SQL);
            setExtraValues(pst,data);
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    private void setExtraValues(PreparedStatement pst, LinkData data) throws SQLException{
        int i = 1;
        pst.setString(i++, data.getLinkUrl());
        pst.setString(i++, data.getLinkIcon());
        pst.setInt(i, data.getId());
    }

}
