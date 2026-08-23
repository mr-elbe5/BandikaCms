/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.request;

import de.elbe5.base.BaseData;

import java.util.HashMap;

public class ClipboardData extends HashMap<String, BaseData> {

    public void putData(String key, BaseData obj){
        put(key,obj);
    }

    public BaseData getData(String key){
        return get(key);
    }

    public boolean hasData(String key){
        return containsKey(key);
    }

    public void clearData(String key){
        remove(key);
    }
}
