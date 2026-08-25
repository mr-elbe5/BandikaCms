/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.administration;

import de.elbe5.page.PageCache;
import de.elbe5.file.PreviewCache;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.user.UserCache;
import de.elbe5.request.RequestData;
import de.elbe5.servlet.Controller;
import de.elbe5.response.IResponse;

public class AdminController extends Controller {

    public static final String KEY = "admin";

    private static AdminController instance = null;

    public static void setInstance(AdminController instance) {
        AdminController.instance = instance;
    }

    public static AdminController getInstance() {
        return instance;
    }

    public static void register(AdminController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse openAdministration(RequestData rdata){
        assertLoggedIn(rdata);
        if (rdata.isEditor())
            return openContentAdministration(rdata);
        assertIsAdmin(rdata);
        return openPersonAdministration(rdata);
    }

    public IResponse openSystemAdministration(RequestData rdata) {
        assertIsAdmin(rdata);
        return showSystemAdministration(rdata);
    }

    public IResponse openPersonAdministration(RequestData rdata) {
        assertIsAdmin(rdata);
        return showPersonAdministration(rdata);
    }

    public IResponse openContentAdministration(RequestData rdata) {
        assertIsEditor(rdata);
        return showContentAdministration(rdata);
    }

    public IResponse reloadUserCache(RequestData rdata) {
        assertIsAdmin(rdata);
        UserCache.setDirty();
        UserCache.checkDirty();
        rdata.setMessage($S("_cacheReloaded"), RequestData.MESSAGE_TYPE_SUCCESS);
        return openSystemAdministration(rdata);
    }

    public IResponse clearPreviewCache(RequestData rdata) {
        assertIsAdmin(rdata);
        PreviewCache.clear();
        rdata.setMessage($S("_cacheCleared"), RequestData.MESSAGE_TYPE_SUCCESS);
        return openSystemAdministration(rdata);
    }

    public IResponse reloadContentCache(RequestData rdata) {
        assertIsAdmin(rdata);
        PageCache.setDirty();
        PageCache.checkDirty();
        rdata.setMessage($S("_cacheReloaded"), RequestData.MESSAGE_TYPE_SUCCESS);
        return openSystemAdministration(rdata);
    }

}
