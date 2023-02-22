/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.base.Log;

import java.lang.reflect.Constructor;

class PagePartClassInfo {

    private String type;
    private Constructor<? extends PagePartData> ctor;
    private PagePartBean bean;
    private boolean useLayouts=false;

    public PagePartClassInfo(Class<? extends PagePartData> contentClass, PagePartBean bean, boolean useLayouts){
        type = contentClass.getSimpleName();
        try {
            ctor = contentClass.getConstructor();
        } catch (Exception e) {
            Log.error("no valid constructor found", e);
        }
        this.bean=bean;
        this.useLayouts=useLayouts;
    }

    public PagePartData getNewData(){
        try {
            return ctor.newInstance();
        } catch (Exception e) {
            Log.error("could not create page part data for type "+type);
        }
        return null;
    }

    public PagePartBean getBean(){
        return bean;
    }

    public boolean useLayouts() {
        return useLayouts;
    }
}
