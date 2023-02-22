/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.base.Log;

import java.util.*;

public class PagePartFactory {

    private static final Map<String, PagePartClassInfo> infos = new HashMap<>();

    public static List<String> getTypes() {
        List<String> list = new ArrayList<>(infos.keySet());
        Collections.sort(list);
        return list;
    }

    public static void addClassInfo(Class<? extends PagePartData> contentClass, PagePartBean bean, boolean useLayouts) {
        PagePartClassInfo contentClassInfo = new PagePartClassInfo(contentClass,bean,useLayouts);
        infos.put(contentClass.getSimpleName(),contentClassInfo);
    }

    public static PagePartData getNewData(String type) {
        if (!infos.containsKey(type)) {
            Log.error("no part info for type "+type);
            return null;
        }
        return infos.get(type).getNewData();
    }

    public static PagePartBean getBean(String type){
        if (!infos.containsKey(type)) {
            Log.error("no part info for type "+type);
            return null;
        }
        return infos.get(type).getBean();
    }

    public static boolean useLayouts(String type) {
        if (!infos.containsKey(type)) {
            Log.error("no part info for type "+type);
            return false;
        }
        return infos.get(type).useLayouts();
    }

}
