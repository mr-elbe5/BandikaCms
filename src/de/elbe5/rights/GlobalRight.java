/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.rights;

import de.elbe5.application.Configuration;
import de.elbe5.user.UserData;

import java.util.*;

public enum GlobalRight {
    APPLICATION,
    USER,
    CONTENTREAD,
    CONTENTEDIT;

    private static final List<GlobalRight> elevatedRights = List.of(APPLICATION, USER, CONTENTREAD, CONTENTEDIT);
    private static final List<GlobalRight> contentReadRights = List.of(CONTENTREAD, CONTENTEDIT);
    private static final List<GlobalRight> contentEditRights = List.of(CONTENTEDIT);

    private static boolean includesAnyRightOf(Set<GlobalRight> rights, List<GlobalRight> validRights){
        for (GlobalRight right : validRights){
            if (rights.contains(right)){
                return true;
            }
        }
        return false;
    }

    public static boolean hasElevatedGlobalRights(UserData user) {
        return user!=null && (includesAnyRightOf(user.getGlobalRights(), elevatedRights) || user.isRoot());
    }

    public static boolean hasGlobalContentReadRight(UserData user) {
        return !Configuration.useReadRights() || (user!=null && (includesAnyRightOf(user.getGlobalRights(), contentReadRights) || user.isRoot()));
    }

    public static boolean hasGlobalContentEditRight(UserData user) {
        return user!=null && (includesAnyRightOf(user.getGlobalRights(), contentEditRights) || user.isRoot());
    }

    public static boolean hasGlobalUserEditRight(UserData user) {
        return user!=null && (user.getGlobalRights().contains(GlobalRight.USER) || user.isRoot());
    }

    public static boolean hasGlobalApplicationEditRight(UserData user) {
        return user!=null && (user.getGlobalRights().contains(GlobalRight.APPLICATION) || user.isRoot());
    }
}


