/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.layout;

import de.elbe5.application.ApplicationPath;
import de.elbe5.base.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LayoutBean {

    private static LayoutBean instance = null;

    public static LayoutBean getInstance() {
        if (instance == null) {
            instance = new LayoutBean();
        }
        return instance;
    }

    public List<LayoutData> getAllLayouts(){
        List<LayoutData> list=new ArrayList<>();
        String layoutPath= ApplicationPath.getAppWEBINFPath()+"/_jsp/_layout";
        File dir= new File(layoutPath);
        if (!dir.exists() || !dir.isDirectory())
            return list;
        File[] files = dir.listFiles();
        if (files!=null){
            for (File f : files){
                LayoutData data=new LayoutData();
                data.setName(FileHelper.getFileNameWithoutExtension(f.getName()));
                data.setCode(FileHelper.readTextFile(f));
                list.add(data);
            }
        }
        return list;
    }

}
