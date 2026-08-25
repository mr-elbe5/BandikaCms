/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.base.BaseData;
import de.elbe5.base.Log;
import de.elbe5.file.ImageBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;
import de.elbe5.request.RequestType;
import de.elbe5.servlet.Controller;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.response.CloseDialogResponse;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;

import java.util.*;

public class PageController extends Controller {

    public static final String KEY = "content";

    private static PageController instance = null;
    private PageData data;

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
    }

    public IResponse show(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getSafeId();
        PageData data = PageCache.getContent(contentId);
        return data.getDefaultView();
    }

    public IResponse show(String url, RequestData rdata) {
        assertLoggedIn(rdata);
        PageData data;
        data = PageCache.getContent(url);
        return data.getDefaultView();
    }

    public IResponse openEditFrontendContent(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = PageBean.getInstance().getPage(contentId);
        data.setUpdateValues(PageCache.getContent(data.getId()), rdata);
        data.setEditMode(true);
        rdata.setSessionObject(RequestKeys.KEY_PAGE, data);
        return data.getDefaultView();
    }

    public IResponse showEditFrontendContent(RequestData rdata) {
        assertLoggedIn(rdata);
        PageData data = rdata.getSessionObject(RequestKeys.KEY_PAGE, PageData.class);
        return data.getDefaultView();
    }

    public IResponse saveFrontendContent(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = rdata.getSessionObject(RequestKeys.KEY_PAGE, PageData.class);
        assert(contentId == data.getId());
        data.readRequestData(rdata, RequestType.frontend);
        data.setChangerId(rdata.getUserId());
        if (!PageBean.getInstance().savePage(data)) {
            setSaveError(rdata);
            return data.getDefaultView();
        }
        data.setEditMode(false);
        rdata.removeSessionObject(RequestKeys.KEY_PAGE);
        PageCache.setDirty();
        return show(rdata);
    }

    public IResponse cancelEditFrontendContent(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = rdata.getSessionObject(RequestKeys.KEY_PAGE, PageData.class);
        assert data.getId() == contentId;
        data.setEditMode(false);
        return data.getDefaultView();
    }

    public IResponse openContentTree(RequestData rdata) {
        return showContentTree(rdata);
    }

    protected IResponse showContentTree(RequestData rdata) {
        return openAdminPage(rdata, "/WEB-INF/_jsp/content/contentTree.jsp", $S("_contentTree"));
    }

    /* Content Administration */

    public IResponse openCreateBackendContent(RequestData rdata) {
        assertLoggedIn(rdata);
        int parentId = rdata.getAttributes().getInt("parentId");
        PageData parentData = PageCache.getContent(parentId);
        String type = rdata.getAttributes().getString("type");
        PageData data = new PageData();
        data.setCreateValues(rdata, RequestType.backend);
        data.setParentValues(parentData);
        rdata.setSessionObject(RequestKeys.KEY_PAGE, data);
        return showEditBackendContent(data);
    }

    public IResponse openEditBackendContent(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = PageBean.getInstance().getPage(contentId);
        data.setUpdateValues(PageCache.getContent(data.getId()), rdata);
        rdata.setSessionObject(RequestKeys.KEY_PAGE, data);
        return showEditBackendContent(data);
    }

    public IResponse saveBackendContent(RequestData rdata) {
        assertLoggedIn(rdata);
        PageData data = PageData.getSessionPage(rdata);
        data.readRequestData(rdata, RequestType.backend);
        if (!rdata.checkFormErrors()) {
            return showEditBackendContent(data);
        }
        data.setChangerId(rdata.getUserId());
        if (!PageBean.getInstance().savePage(data)) {
            setSaveError(rdata);
            return showEditBackendContent(data);
        }
        data.setNew(false);
        rdata.removeSessionObject(RequestKeys.KEY_PAGE);
        PageCache.setDirty();
        rdata.setMessage($S("_contentSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openContentAdministration?contentId=" + data.getId());
    }

    public IResponse deleteBackendContent(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data= PageCache.getContent(contentId);
        if (contentId < BaseData.ID_MIN) {
            rdata.setMessage($S("_notDeletable"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showContentAdministration(rdata, contentId);
        }
        int parentId = PageCache.getParentContentId(contentId);
        PageBean.getInstance().deletePage(contentId);
        PageCache.setDirty();
        rdata.getAttributes().put("contentId", Integer.toString(parentId));
        PageCache.setDirty();
        rdata.setMessage($S("_contentDeleted"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return showContentAdministration(rdata,parentId);
    }

    public IResponse cutContent(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = PageBean.getInstance().getPage(contentId);
        rdata.setClipboardData(RequestKeys.KEY_PAGE, data);
        return showContentAdministration(rdata,data.getId());
    }

    public IResponse pasteContent(RequestData rdata) {
        assertLoggedIn(rdata);
        int parentId = rdata.getAttributes().getInt("parentId");
        PageData data=rdata.getClipboardData(RequestKeys.KEY_PAGE, PageData.class);
        if (data==null){
            rdata.setMessage($S("_actionNotExcecuted"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showContentAdministration(rdata, parentId);
        }
        PageData parent = PageCache.getContent(parentId);
        if (parent == null){
            rdata.setMessage($S("_actionNotExcecuted"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showContentAdministration(rdata, parentId);
        }
        Set<Integer> parentIds=new HashSet<>();
        parent.collectParentIds(parentIds);
        if (parentIds.contains(data.getId())){
            rdata.setMessage($S("_actionNotExcecuted"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showContentAdministration(rdata, parentId);
        }
        data.setParentId(parentId);
        data.setParent(parent);
        data.generatePath();
        data.setChangerId(rdata.getUserId());
        PageBean.getInstance().savePage(data);
        rdata.clearClipboardData(RequestKeys.KEY_PAGE);
        PageCache.setDirty();
        rdata.setMessage($S("_contentPasted"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return showContentAdministration(rdata,data.getId());
    }

    //backend
    public IResponse clearClipboard(RequestData rdata) {
        assertLoggedIn(rdata);
        rdata.clearAllClipboardData();
        return showContentAdministration(rdata, 1);
    }

    //backend
    public IResponse openSortChildContents(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = PageCache.getContent(contentId);
        rdata.setSessionObject(RequestKeys.KEY_PAGE, data);
        return showSortChildContents();
    }

    //backend
    public IResponse saveChildRankings(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = PageData.getSessionPage(rdata);
        for (PageData child : data.getChildren()){
            int ranking=rdata.getAttributes().getInt("select"+child.getId(),-1);
            if (ranking!=-1){
                child.setRanking(ranking);

            }
        }
        Collections.sort(data.getChildren());
        PageBean.getInstance().updateChildRankings(data);
        rdata.removeSessionObject(RequestKeys.KEY_PAGE);
        PageCache.setDirty();
        rdata.setMessage($S("_newRankingSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openContentAdministration?contentId=" + contentId);
    }

    public IResponse reduceImages(RequestData rdata) {
        assertLoggedIn(rdata);
        List<ImageData> list = PageCache.getFiles(ImageData.class);
        for (ImageData data : list){
            ImageData image = ImageBean.getInstance().getFile(data.getId(), true, ImageData.class);
            if (image.resizeImage()){
                ImageBean.getInstance().saveFile(image, true);
                Log.info("image "+data.getId()+" resized and saved");
            }
        }
        return new ForwardResponse("/ctrl/admin/openContentAdministration");
    }

    protected IResponse showEditBackendContent(PageData contentData) {
        return new ForwardResponse(contentData.getBackendEditJsp());
    }

    protected IResponse showSortChildContents() {
        return new ForwardResponse("/WEB-INF/_jsp/content/sortChildContents.ajax.jsp");
    }

    protected IResponse showContentAdministration(RequestData rdata, int contentId) {
        return new ForwardResponse("/ctrl/admin/openContentAdministration?contentId=" + contentId);
    }

    public IResponse showDraft(RequestData rdata){
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = PageCache.getContent(contentId, PageData.class);
        assert(data!=null);
        return data.getDefaultView();
    }

    public IResponse showPublished(RequestData rdata){
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = PageCache.getContent(contentId, PageData.class);
        assert(data!=null);
        data.showPublished(true);
        return data.getDefaultView();
    }

    //frontend
    public IResponse publishPage(RequestData rdata){
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        Log.log("Publishing page" + contentId);
        PageData data= PageCache.getContent(contentId,PageData.class);
        assert(data!=null);
        data.setPublishing(true);
        data.setPublishDate(PageBean.getInstance().getServerTime());
        return data.getDefaultView();
    }

    public IResponse openLinkBrowser(RequestData rdata) {
        assertLoggedIn(rdata);
        PageData data=rdata.getSessionObject(RequestKeys.KEY_PAGE, PageData.class);
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/browseLinks.jsp");
    }

    public IResponse openImageBrowser(RequestData rdata) {
        assertLoggedIn(rdata);
        PageData data=rdata.getSessionObject(RequestKeys.KEY_PAGE, PageData.class);
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/browseImages.jsp");
    }

    public IResponse addImage(RequestData rdata) {
        assertLoggedIn(rdata);
        PageData data=rdata.getSessionObject(RequestKeys.KEY_PAGE, PageData.class);
        ImageData image=new ImageData();
        image.setCreateValues(rdata, RequestType.frontend);
        image.setParentValues(data);
        image.readRequestData(rdata, RequestType.frontend);
        ImageBean.getInstance().saveFile(image,true);
        PageCache.setDirty();
        rdata.getAttributes().put("imageId", Integer.toString(image.getId()));
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/addImage.ajax.jsp");
    }

    public IResponse addPart(RequestData rdata) {
        assertLoggedIn(rdata);
        int contentId = rdata.getId();
        PageData data = rdata.getSessionObject(RequestKeys.KEY_PAGE, PageData.class);
        int fromPartId = rdata.getAttributes().getInt("fromPartId", -1);
        String partType = rdata.getAttributes().getString("partType");
        PagePartData pdata = PageBean.getInstance().getNewPagePartData(partType);
        pdata.setCreateValues(rdata, RequestType.frontend);
        data.addPart(pdata, fromPartId, true);
        rdata.getAttributes().put(PagePartData.KEY_PART, pdata);
        return new ForwardResponse("/WEB-INF/_jsp/page/newPart.ajax.jsp");
    }


}
