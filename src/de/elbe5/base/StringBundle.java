/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2018 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.base;

import org.apache.commons.text.StringEscapeUtils;

import java.util.*;

public class StringBundle {

    private final Map<String, String> stringMap = new HashMap<>();

    public void addBundle(String name, Locale locale){
        ResourceBundle bundle = ResourceBundle.getBundle(name, locale);
        List<String> presentKeys = new ArrayList<>();
        for (String key : bundle.keySet()){
            if (stringMap.containsKey(key)){
                presentKeys.add(key);
            }
            stringMap.put(key, bundle.getString(key));
        }
        Collections.sort(presentKeys);
        for (String key : presentKeys){
            Log.warn("Replaced key " + key + " with bundle " + name);
        }
    }

    public String string(String key) {
        try {
            String s = stringMap.get(key);
            if (s!=null)
                return s;

        }
        catch (Exception e){
            Log.warn("string not found: " + key);
        }
        return "[" + key + "]";
    }

    public String html(String key) {
        return StringEscapeUtils.escapeHtml4(string(key));
    }

    public String htmlMultiline(String key) {
        return StringEscapeUtils.escapeHtml4(string(key)).replaceAll("\\\\n", "<br/>");
    }

    public String js(String key) {
        return StringEscapeUtils.escapeEcmaScript(string(key));
    }

    public String xml(String key) {
        return StringEscapeUtils.escapeXml11(string(key));
    }

    public String csv(String key) {
        //escape by opencsv
        return string(key);
    }

}
