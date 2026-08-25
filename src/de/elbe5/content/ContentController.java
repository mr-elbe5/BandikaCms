/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.content;

import de.elbe5.base.BaseData;
import de.elbe5.base.Log;
import de.elbe5.file.FileBean;
import de.elbe5.file.ImageBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;
import de.elbe5.request.RequestType;
import de.elbe5.rights.GlobalRight;
import de.elbe5.servlet.Controller;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.response.CloseDialogResponse;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;
import de.elbe5.servlet.ResponseException;

import jakarta.servlet.http.HttpServletResponse;
import java.util.*;

public class ContentController extends Controller {

    public static final String KEY = "content";

    private static ContentController instance = null;

    public static void setInstance(ContentController instance) {
        ContentController.instance = instance;
    }

    public static ContentController getInstance() {
        return instance;
    }

    public static void register(ContentController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    protected IResponse openJspPage(String jsp) {
        JspContentData contentData = new JspContentData();
        contentData.setJsp(jsp);
        return new ContentResponse(contentData);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse show(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getSafeId();
        ContentData data = ContentCache.getContent(contentId);
        assertRights(data.hasUserReadRight(rdata.getLoginUser()));
        increaseViewCount(data);
        return data.getDefaultView();
    }

    public IResponse show(String url, RequestData rdata) {
        assertSessionCall(rdata);
        ContentData data;
        data = ContentCache.getContent(url);
        assertRights(data.hasUserReadRight(rdata.getLoginUser()));
        //Log.log("show: "+data.getClass().getSimpleName());
        increaseViewCount(data);
        return data.getDefaultView();
    }

    protected void increaseViewCount(ContentData data){
    }

    public IResponse openCreateFrontendContent(RequestData rdata) {
        throw new ResponseException(HttpServletResponse.SC_NOT_FOUND);
    }

    public IResponse openEditFrontendContent(RequestData rdata) {
        throw new ResponseException(HttpServletResponse.SC_NOT_FOUND);
    }

    public IResponse showEditFrontendContent(RequestData rdata) {
        throw new ResponseException(HttpServletResponse.SC_NOT_FOUND);
    }

    public IResponse cancelEditFrontendContent(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        ContentData data = ContentData.getSessionContent(rdata, ContentData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        rdata.removeSessionObject(ContentRequestKeys.KEY_CONTENT);
        return show(rdata);
    }

    public IResponse saveFrontendContent(RequestData rdata) {
        throw new ResponseException(HttpServletResponse.SC_NOT_FOUND);
    }

    public IResponse openContentTree(RequestData rdata) {
        return showContentTree(rdata);
    }

    protected IResponse showContentTree(RequestData rdata) {
        return openAdminPage(rdata, "/WEB-INF/_jsp/content/contentTree.jsp", $S("_contentTree"));
    }

    /* Content Administration */

    public IResponse openCreateBackendContent(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        int parentId = rdata.getAttributes().getInt("parentId");
        ContentData parentData = ContentCache.getContent(parentId);
        String type = rdata.getAttributes().getString("type");
        ContentData data = ContentBean.getInstance().getNewContentData(type);
        data.setCreateValues(rdata, RequestType.backend);
        data.setParentValues(parentData);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT, data);
        return showEditBackendContent(data);
    }

    public IResponse openEditBackendContent(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        int contentId = rdata.getId();
        ContentData data = ContentBean.getInstance().getContent(contentId);
        data.setUpdateValues(ContentCache.getContent(data.getId()), rdata);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT, data);
        return showEditBackendContent(data);
    }

    public IResponse saveBackendContent(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        int contentId = rdata.getId();
        ContentData data = ContentData.getSessionContent(rdata, ContentData.class);
        data.readRequestData(rdata, RequestType.backend);
        if (!rdata.checkFormErrors()) {
            return showEditBackendContent(data);
        }
        data.setChangerId(rdata.getUserId());
        if (!ContentBean.getInstance().saveContent(data)) {
            setSaveError(rdata);
            return showEditBackendContent(data);
        }
        data.setNew(false);
        rdata.removeSessionObject(ContentRequestKeys.KEY_CONTENT);
        ContentCache.setDirty();
        rdata.setMessage($S("_contentSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openContentAdministration?contentId=" + data.getId());
    }

    public IResponse deleteBackendContent(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        int contentId = rdata.getId();
        ContentData data=ContentCache.getContent(contentId);
        if (contentId < BaseData.ID_MIN) {
            rdata.setMessage($S("_notDeletable"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showContentAdministration(rdata, contentId);
        }
        int parentId = ContentCache.getParentContentId(contentId);
        ContentBean.getInstance().deleteContent(contentId);
        ContentCache.setDirty();
        rdata.getAttributes().put("contentId", Integer.toString(parentId));
        ContentCache.setDirty();
        rdata.setMessage($S("_contentDeleted"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return showContentAdministration(rdata,parentId);
    }

    public IResponse cutContent(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        int contentId = rdata.getId();
        ContentData data = ContentBean.getInstance().getContent(contentId);
        rdata.setClipboardData(ContentRequestKeys.KEY_CONTENT, data);
        return showContentAdministration(rdata,data.getId());
    }

    public IResponse pasteContent(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        int parentId = rdata.getAttributes().getInt("parentId");
        ContentData data=rdata.getClipboardData(ContentRequestKeys.KEY_CONTENT,ContentData.class);
        if (data==null){
            rdata.setMessage($S("_actionNotExcecuted"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showContentAdministration(rdata, parentId);
        }
        ContentData parent = ContentCache.getContent(parentId);
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
        ContentBean.getInstance().saveContent(data);
        rdata.clearClipboardData(ContentRequestKeys.KEY_CONTENT);
        ContentCache.setDirty();
        rdata.setMessage($S("_contentPasted"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return showContentAdministration(rdata,data.getId());
    }

    //backend
    public IResponse clearClipboard(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        rdata.clearAllClipboardData();
        return showContentAdministration(rdata, 1);
    }

    //backend
    public IResponse openSortChildContents(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        int contentId = rdata.getId();
        ContentData data = ContentCache.getContent(contentId);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT, data);
        return showSortChildContents();
    }

    //backend
    public IResponse saveChildRankings(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        int contentId = rdata.getId();
        ContentData data = ContentData.getSessionContent(rdata, ContentData.class);
        for (ContentData child : data.getChildren()){
            int ranking=rdata.getAttributes().getInt("select"+child.getId(),-1);
            if (ranking!=-1){
                child.setRanking(ranking);

            }
        }
        Collections.sort(data.getChildren());
        ContentBean.getInstance().updateChildRankings(data);
        rdata.removeSessionObject(ContentRequestKeys.KEY_CONTENT);
        ContentCache.setDirty();
        rdata.setMessage($S("_newRankingSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openContentAdministration?contentId=" + contentId);
    }

    public IResponse reduceImages(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        List<ImageData> list = ContentCache.getFiles(ImageData.class);
        for (ImageData data : list){
            ImageData image = ImageBean.getInstance().getFile(data.getId(), true, ImageData.class);
            if (image.resizeImage()){
                ImageBean.getInstance().saveFile(image, true);
                Log.info("image "+data.getId()+" resized and saved");
            }
        }
        return new ForwardResponse("/ctrl/admin/openContentAdministration");
    }

    protected IResponse showEditBackendContent(ContentData contentData) {
        return new ForwardResponse(contentData.getBackendEditJsp());
    }

    protected IResponse showSortChildContents() {
        return new ForwardResponse("/WEB-INF/_jsp/content/sortChildContents.ajax.jsp");
    }

    protected IResponse showContentAdministration(RequestData rdata, int contentId) {
        return new ForwardResponse("/ctrl/admin/openContentAdministration?contentId=" + contentId);
    }

}
