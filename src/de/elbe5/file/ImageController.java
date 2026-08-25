/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import de.elbe5.base.Log;
import de.elbe5.content.ContentCache;
import de.elbe5.content.ContentData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;
import de.elbe5.request.RequestType;
import de.elbe5.response.*;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.user.UserData;
import jakarta.servlet.http.HttpServletResponse;

public class ImageController extends FileController {

    public static final String KEY = "image";

    private static ImageController instance = null;

    public static void setInstance(ImageController instance) {
        ImageController.instance = instance;
    }

    public static ImageController getInstance() {
        return instance;
    }

    public static void register(ImageController controller){
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
        data.setUpdateValues(rdata);
        ContentData parent=ContentCache.getContent(data.getParentId());
        assertRights(parent.hasUserEditRight(rdata.getLoginUser()));
        rdata.setSessionObject(ContentRequestKeys.KEY_FILE,data);
        return showEditFile();
    }

    public IResponse saveFile(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        int fileId = rdata.getId();
        ImageData data = rdata.getSessionObject(ContentRequestKeys.KEY_FILE,ImageData.class);
        assert fileId == data.getId();
        ContentData parent=ContentCache.getContent(data.getParentId());
        assertRights(parent.hasUserEditRight(rdata.getLoginUser()));
        data.readRequestData(rdata, RequestType.backend);
        if (!rdata.checkFormErrors()) {
            return showEditFile();
        }
        if (!FileBean.getInstance().saveFile(data, data.isNew() || data.getBytes()!=null)) {
            setSaveError(rdata);
            return showEditFile();
        }
        data.setNew(false);
        ContentCache.setDirty();
        rdata.setMessage($S("_fileSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openContentAdministration?contentId=" + parent.getId());
    }

    public IResponse showPreview(RequestData rdata) {
        assertSessionCall(rdata);
        int imageId = rdata.getId();
        return new PreviewResponse(imageId);
    }

    protected IResponse showEditFile() {
        return new ForwardResponse("/WEB-INF/_jsp/file/editImage.ajax.jsp");
    }

}
