/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.user;

import de.elbe5.base.Log;
import de.elbe5.content.ContentData;

import java.util.*;

public class UserCache {

    private static int version = 1;
    private static volatile boolean dirty = true;
    private static final Object lockObj = new Object();

    private static Map<Integer, UserData> userMap = new HashMap<>();

    public static synchronized void load() {
        UserBean bean = UserBean.getInstance();
        List<UserData> userList = bean.getAllUsers();
        Map<Integer, UserData> users = new HashMap<>();
        for (UserData user : userList) {
            users.put(user.getId(), user);
        }
        userMap = users;
        Log.info("user cache reloaded");
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

    public static UserData getUser(int id) {
        checkDirty();
        return userMap.get(id);
    }

    public static <T extends UserData> T getUser(int id, Class<T> cls) {
        checkDirty();
        try {
            return cls.cast(userMap.get(id));
        }
        catch(NullPointerException | ClassCastException e){
            return null;
        }
    }
}
