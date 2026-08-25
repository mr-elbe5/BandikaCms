/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupCache {

    private static int version = 1;
    private static volatile boolean dirty = true;
    private static final Object lockObj = new Object();

    private static List<GroupData> groupList = new ArrayList<>();
    private static Map<Integer, GroupData> groupMap = new HashMap<>();

    public static synchronized void load() {
        GroupBean bean = GroupBean.getInstance();
        List<GroupData> list = bean.getAllGroups();
        Map<Integer, GroupData> groups = new HashMap<>();
        for (GroupData user : list) {
            groups.put(user.getId(), user);
        }
        groupList = list;
        groupMap = groups;
    }

    public static void setDirty() {
        increaseVersion();
        dirty = true;
    }

    public static void checkDirty() {
        if (dirty) {
            synchronized (lockObj) {
                if (dirty) {
                    load();
                    dirty = false;
                }
            }
        }
    }

    public static void increaseVersion() {
        version++;
    }

    public static int getVersion() {
        return version;
    }

    public static List<GroupData> getAllGroups(){
        checkDirty();
        return groupList;
    }

    public static GroupData getGroup(int id) {
        checkDirty();
        return groupMap.get(id);
    }
}
