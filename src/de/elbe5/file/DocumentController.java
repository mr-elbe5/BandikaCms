/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import de.elbe5.content.ContentCache;
import de.elbe5.content.ContentData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;
import de.elbe5.request.RequestType;
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
        assertLoggedInSessionCall(rdata);
        FileData data = FileBean.getInstance().getFile(rdata.getId(),true);
        ContentData parent=ContentCache.getContent(data.getParentId());
        assertRights(parent.hasUserEditRight(rdata.getLoginUser()));
        rdata.setSessionObject(ContentRequestKeys.KEY_FILE,data);
        return showEditFile();
    }

    public IResponse saveFile(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        int fileId = rdata.getId();
        DocumentData data = rdata.getSessionObject(ContentRequestKeys.KEY_FILE,DocumentData.class);
        assert fileId == data.getId();
        ContentData parent=ContentCache.getContent(data.getParentId());
        assertRights(parent.hasUserEditRight(rdata.getLoginUser()));
        data.readRequestData(rdata, RequestType.backend);
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
        ContentCache.setDirty();
        rdata.setMessage($S("_fileSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openContentAdministration?contentId=" + parent.getId());
    }

    protected IResponse showEditFile() {
        return new ForwardResponse("/WEB-INF/_jsp/file/editDocument.ajax.jsp");
    }

}
