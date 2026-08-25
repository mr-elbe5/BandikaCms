/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.base.Log;
import de.elbe5.content.*;
import de.elbe5.file.ImageBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.*;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;

public class PageController extends ContentController {

    public static final String KEY = "page";

    private static PageController instance = null;

    public static void setInstance(PageController instance) {
        PageController.instance = instance;
    }

    public static PageController getInstance() {
        return instance;
    }

    public static void register(PageController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    };

    @Override
    public IResponse openEditFrontendContent(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = ContentBean.getInstance().getContent(contentId,PageData.class);
        data.setUpdateValues(ContentCache.getContent(data.getId()), rdata);
        data.setEditMode(true);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT, data);
        return data.getDefaultView();
    }

    @Override
    public IResponse showEditFrontendContent(RequestData rdata) {
        assertLoggedIn(rdata);
        PageData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, PageData.class);
        return data.getDefaultView();
    }

    @Override
    public IResponse saveFrontendContent(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, PageData.class);
        assert(contentId == data.getId());
        data.readRequestData(rdata, RequestType.frontend);
        data.setChangerId(rdata.getUserId());
        if (!ContentBean.getInstance().saveContent(data)) {
            setSaveError(rdata);
            return data.getDefaultView();
        }
        data.setEditMode(false);
        rdata.removeSessionObject(ContentRequestKeys.KEY_CONTENT);
        ContentCache.setDirty();
        return show(rdata);
    }

    @Override
    public IResponse cancelEditFrontendContent(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, PageData.class);
        assert data.getId() == contentId;
        data.setEditMode(false);
        return data.getDefaultView();
    }

    public IResponse showDraft(RequestData rdata){
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = ContentCache.getContent(contentId, PageData.class);
        assert(data!=null);
        return data.getDefaultView();
    }

    public IResponse showPublished(RequestData rdata){
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = ContentCache.getContent(contentId, PageData.class);
        assert(data!=null);
        data.showPublished(true);
        return data.getDefaultView();
    }

    //frontend
    public IResponse publishPage(RequestData rdata){
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        Log.log("Publishing page" + contentId);
        PageData data=ContentCache.getContent(contentId,PageData.class);
        assert(data!=null);
        data.setPublishing(true);
        data.setPublishDate(PageBean.getInstance().getServerTime());
        return data.getDefaultView();
    }

    public IResponse openLinkBrowser(RequestData rdata) {
        assertLoggedIn(rdata);
        ContentData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/browseLinks.jsp");
    }

    public IResponse openImageBrowser(RequestData rdata) {
        assertLoggedIn(rdata);
        ContentData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/browseImages.jsp");
    }

    public IResponse addImage(RequestData rdata) {
        assertLoggedIn(rdata);
        ContentData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        ImageData image=new ImageData();
        image.setCreateValues(rdata, RequestType.frontend);
        image.setParentValues(data);
        image.readRequestData(rdata, RequestType.frontend);
        ImageBean.getInstance().saveFile(image,true);
        ContentCache.setDirty();
        rdata.getAttributes().put("imageId", Integer.toString(image.getId()));
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/addImage.ajax.jsp");
    }

    public IResponse addPart(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, PageData.class);
        int fromPartId = rdata.getAttributes().getInt("fromPartId", -1);
        String partType = rdata.getAttributes().getString("partType");
        PagePartData pdata = PageBean.getInstance().getNewPagePartData(partType);
        pdata.setCreateValues(rdata, RequestType.frontend);
        data.addPart(pdata, fromPartId, true);
        rdata.getAttributes().put(PagePartData.KEY_PART, pdata);
        return new ForwardResponse("/WEB-INF/_jsp/page/newPart.ajax.jsp");
    }

}
