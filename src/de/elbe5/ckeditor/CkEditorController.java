/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.ckeditor;

import de.elbe5.content.ContentCache;
import de.elbe5.content.ContentController;
import de.elbe5.content.ContentData;
import de.elbe5.file.ImageBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestType;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;

public class CkEditorController extends ContentController {

    public static final String KEY = "ckeditor";

    private static CkEditorController instance = null;

    public static void setInstance(CkEditorController instance) {
        CkEditorController.instance = instance;
    }

    public static CkEditorController getInstance() {
        return instance;
    }

    public static void register(CkEditorController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse openLinkBrowser(RequestData rdata) {
        assertSessionCall(rdata);
        ContentData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/browseLinks.jsp");
    }

    public IResponse openImageBrowser(RequestData rdata) {
        assertSessionCall(rdata);
        ContentData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/browseImages.jsp");
    }

    public IResponse addImage(RequestData rdata) {
        assertSessionCall(rdata);
        ContentData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        ImageData image=new ImageData();
        image.setCreateValues(data,rdata);
        image.readRequestData(rdata, RequestType.frontend);
        ImageBean.getInstance().saveFile(image,true);
        ContentCache.setDirty();
        rdata.getAttributes().put("imageId", Integer.toString(image.getId()));
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/addImage.ajax.jsp");
    }


}
