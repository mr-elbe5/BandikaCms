/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.administration;

import de.elbe5.application.ApplicationPath;
import de.elbe5.base.Log;
import de.elbe5.base.FileHelper;
import de.elbe5.content.ContentCache;
import de.elbe5.content.ContentLogBean;
import de.elbe5.file.PreviewCache;
import de.elbe5.request.RequestKeys;
import de.elbe5.response.StatusResponse;
import de.elbe5.rights.GlobalRight;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.servlet.ResponseException;
import de.elbe5.user.UserCache;
import de.elbe5.request.RequestData;
import de.elbe5.servlet.Controller;
import de.elbe5.response.IResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

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
        assertSessionCall(rdata);
        assertLoggedIn(rdata);
        if (GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()))
            return openContentAdministration(rdata);
        if (GlobalRight.hasGlobalUserEditRight(rdata.getLoginUser()))
            return openPersonAdministration(rdata);
        if (GlobalRight.hasGlobalApplicationEditRight(rdata.getLoginUser()))
            return openSystemAdministration(rdata);
        throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
    }

    public IResponse openSystemAdministration(RequestData rdata) {
        if (!rdata.isLoggedIn())
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasElevatedGlobalRights(rdata.getLoginUser()));
        return showSystemAdministration(rdata);
    }

    public IResponse openPersonAdministration(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalUserEditRight(rdata.getLoginUser()));
        return showPersonAdministration(rdata);
    }

    public IResponse openContentAdministration(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        return showContentAdministration(rdata);
    }

    public IResponse openContentLog(RequestData rdata) {
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        return showContentLog(rdata);
    }

    public IResponse resetContentLog(RequestData rdata) {
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        ContentLogBean.getInstance().resetContentLog();
        return showContentLog(rdata);
    }

    protected IResponse showContentLog(RequestData rdata) {
        return openAdminPage(rdata, "/WEB-INF/_jsp/administration/contentLog.jsp", $S("_contentLog"));
    }

    public IResponse restart(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalApplicationEditRight(rdata.getLoginUser()));
        String path = ApplicationPath.getAppROOTPath() + "/WEB-INF/web.xml";
        File f = new File(path);
        try {
            FileHelper.touch(f);
        } catch (IOException e) {
            Log.error("could not touch file " + path, e);
        }
        rdata.setMessage($S("_restartHint"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return openSystemAdministration(rdata);
    }

    public IResponse reloadUserCache(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalApplicationEditRight(rdata.getLoginUser()));
        UserCache.setDirty();
        UserCache.checkDirty();
        rdata.setMessage($S("_cacheReloaded"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return openSystemAdministration(rdata);
    }

    public IResponse clearPreviewCache(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalApplicationEditRight(rdata.getLoginUser()));
        PreviewCache.clear();
        rdata.setMessage($S("_cacheCleared"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return openSystemAdministration(rdata);
    }

    public IResponse reloadContentCache(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalApplicationEditRight(rdata.getLoginUser()));
        ContentCache.setDirty();
        ContentCache.checkDirty();
        rdata.setMessage($S("_cacheReloaded"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return openSystemAdministration(rdata);
    }

    protected IResponse showContentAdministration(RequestData rdata) {
        return openAdminPage(rdata, "/WEB-INF/_jsp/administration/contentAdministration.jsp", $S("_contentAdministration"));
    }

}
