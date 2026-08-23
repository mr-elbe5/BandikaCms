/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2018 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import de.elbe5.base.BinaryFile;

import java.util.HashMap;
import java.util.Map;

public class PreviewCache {

    private static final Object lockObj = new Object();
    private static final Map<Integer, BinaryFile> map = new HashMap<>();

    public static BinaryFile get(Integer id) {
        synchronized (lockObj) {
            if (!map.containsKey(id)){
                BinaryFile file= ImageBean.getInstance().getBinaryPreviewFile(id);
                if (file != null){
                    map.put(id,file);
                }
            }
            return map.get(id);
        }
    }

    public static void remove(Integer id) {
        synchronized (lockObj) {
            map.remove(id);
        }
    }

    public static void clear(){
        map.clear();
    }

}
