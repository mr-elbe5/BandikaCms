/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import de.elbe5.page.PageCache;
import de.elbe5.page.PageData;
import de.elbe5.request.RequestData;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.response.CloseDialogResponse;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;

public class DocumentController extends FileController {

    public static final String KEY = "document";

    private static DocumentController instance = null;

    public static void setInstance(DocumentController instance) {
        DocumentController.instance = instance;
    }

    public static DocumentController getInstance() {
        return instance;
    }

    public static void register(DocumentController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse openEditFile(RequestData rdata) {
        assertLoggedIn(rdata);
        FileData data = FileBean.getInstance().getFile(rdata.getId(),true);
        PageData parent= PageCache.getContent(data.getParentId());
        rdata.setSessionFile(data);
        return showEditFile();
    }

    public IResponse saveFile(RequestData rdata) {
        assertLoggedIn(rdata);
        int fileId = rdata.getId();
        DocumentData data = rdata.getSessionDocument();
        assert fileId == data.getId();
        PageData parent= PageCache.getContent(data.getParentId());
        data.readBackendRequestData(rdata);
        if (!rdata.checkFormErrors()) {
            return showEditFile();
        }
        data.setChangerId(rdata.getUserId());
        //bytes=null, if no new file selfileIdected
        if (!FileBean.getInstance().saveFile(data,data.isNew() || data.getBytes()!=null)) {
            setSaveError(rdata);
            return showEditFile();
        }
        data.setNew(false);
        PageCache.setDirty();
        rdata.setMessage($S("_fileSaved"), RequestData.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openContentAdministration?contentId=" + parent.getId());
    }

    protected IResponse showEditFile() {
        return new ForwardResponse("/WEB-INF/_jsp/file/editDocument.ajax.jsp");
    }

}
