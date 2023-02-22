/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.layout;

import de.elbe5.base.Log;

import java.util.*;

public class LayoutCache {

    private static int version = 1;
    private static boolean dirty = true;
    private static final Integer lockObj = 1;

    private static final Map<String, LayoutData> layoutMap = new HashMap<>();
    private static final Map<String, List<LayoutData>> layoutTypeMap = new HashMap<>();

    public static void addType(String type){
        layoutTypeMap.put(type,new ArrayList<>());
    }

    public static synchronized void load() {
        LayoutBean bean = LayoutBean.getInstance();
        List<LayoutData> allLayouts = bean.getAllLayouts();
        layoutMap.clear();
        for (List<LayoutData> list : layoutTypeMap.values())
            list.clear();
        for (LayoutData layoutData : allLayouts) {
            layoutMap.put(layoutData.getName(), layoutData);
            for (String type : getTypes()) {
                if (layoutData.getName().endsWith(type))
                    layoutTypeMap.get(type).add(layoutData);
            }
        }
        for (String type : layoutTypeMap.keySet()) {
            List<LayoutData> list=layoutTypeMap.get(type);
            Collections.sort(list);
            Log.log("found "+list.size()+" layouts of type "+type);
        }
        dirty=false;
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

    public static Set<String> getTypes(){
        return layoutTypeMap.keySet();
    }

    public static LayoutData getLayout(String name) {
        checkDirty();
        return layoutMap.get(name);
    }

    public static List<LayoutData> getLayouts(String type){
        if (!layoutTypeMap.containsKey(type))
            return new ArrayList<>();
        return layoutTypeMap.get(type);
    }
}
