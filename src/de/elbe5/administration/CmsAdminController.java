/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.administration;

import de.elbe5.content.ContentLogBean;
import de.elbe5.request.RequestData;
import de.elbe5.response.IResponse;
import de.elbe5.rights.GlobalRight;

public class CmsAdminController extends AdminController {

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

}
