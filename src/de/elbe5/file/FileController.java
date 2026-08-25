/*
 Bandika CMS - A Java based modular File Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import de.elbe5.application.ApplicationPath;
import de.elbe5.base.BinaryFile;
import de.elbe5.content.ContentCache;
import de.elbe5.content.ContentData;
import de.elbe5.request.*;
import de.elbe5.response.MemoryFileResponse;
import de.elbe5.response.StatusResponse;
import de.elbe5.servlet.Controller;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;

import de.elbe5.servlet.ControllerCache;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;

public class FileController extends Controller {

    public static final String KEY = "file";

    private static FileController instance = null;

    public static void setInstance(FileController instance) {
        FileController.instance = instance;
    }

    public static FileController getInstance() {
        return instance;
    }

    public static void register(FileController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse download(RequestData rdata) {
        if (rdata.getContext() == RequestContext.api)
            return apiDownload(rdata);
        else
            return sessionDownload(rdata);
    }

    public IResponse apiDownload(RequestData rdata) {
        assertLoggedInApiCall(rdata);
        int id = rdata.getId();
        BinaryFile file = FileBean.getInstance().getBinaryFile(id);
        return new MemoryFileResponse(file);
    }

    public IResponse sessionDownload(RequestData rdata) {
        assertSessionCall(rdata);
        int id = rdata.getId();
        FileData data = ContentCache.getFile(id);
        rdata.getAttributes().put("download", "true");
        return show(data, rdata);
    }

    private IResponse show(FileData data, RequestData rdata){
        assertSessionCall(rdata);
        ContentData parent=ContentCache.getContent(data.getParentId());
        File file = new File(ApplicationPath.getAppFilePath(), data.getStaticFileName());
        // if not exists, create from database
        if (!file.exists() && !FileBean.getInstance().createTempFile(file)) {
            return new StatusResponse(HttpServletResponse.SC_NOT_FOUND);
        }
        RangeInfo rangeInfo = null;
        String rangeHeader = rdata.getRequest().getHeader("Range");
        if (rangeHeader != null) {
            rangeInfo = new RangeInfo(rangeHeader, file.length());
        }
        return new FileResponse(file, data.getDisplayFileName(), rangeInfo);
    }

    public IResponse openCreateFile(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        int parentId = rdata.getAttributes().getInt("parentId");
        ContentData parentData = ContentCache.getContent(parentId);
        assertRights(parentData.hasUserEditRight(rdata.getLoginUser()));
        String type=rdata.getAttributes().getString("type");
        FileData data = FileBean.getInstance().getNewFileData(type);
        data.setCreateValues(rdata, RequestType.backend);
        data.setParentValues(parentData);
        rdata.setSessionObject(ContentRequestKeys.KEY_FILE, data);
        return new ForwardResponse(data.getEditURL());
    }

    public IResponse cutFile(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        int fileId = rdata.getId();
        FileData data = FileBean.getInstance().getFile(fileId,true);
        ContentData parent=ContentCache.getContent(data.getParentId());
        assertRights(parent.hasUserEditRight(rdata.getLoginUser()));
        rdata.setClipboardData(ContentRequestKeys.KEY_FILE, data);
        return showContentAdministration(rdata,parent.getId());
    }

    public IResponse copyFile(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        int fileId = rdata.getId();
        FileData data = FileBean.getInstance().getFile(fileId,true);
        ContentData parent=ContentCache.getContent(data.getParentId());
        assertRights(parent.hasUserEditRight(rdata.getLoginUser()));
        data.setNew(true);
        data.setId(FileBean.getInstance().getNextId());
        data.setCreatorId(rdata.getUserId());
        data.setChangerId(rdata.getUserId());
        rdata.setClipboardData(ContentRequestKeys.KEY_FILE, data);
        return showContentAdministration(rdata,parent.getId());
    }

    public IResponse pasteFile(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        int parentId = rdata.getAttributes().getInt("parentId");
        FileData data=rdata.getClipboardData(ContentRequestKeys.KEY_FILE, FileData.class);
        ContentData parent=ContentCache.getContent(parentId);
        if (parent == null){
            rdata.setMessage($S("_actionNotExcecuted"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showContentAdministration(rdata, parentId);
        }
        assertRights(parent.hasUserEditRight(rdata.getLoginUser()));
        data.setParentId(parentId);
        data.setParent(parent);
        data.setChangerId(rdata.getUserId());
        FileBean.getInstance().saveFile(data, true);
        rdata.clearClipboardData(ContentRequestKeys.KEY_FILE);
        ContentCache.setDirty();
        rdata.setMessage($S("_filePasted"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return showContentAdministration(rdata,data.getId());
    }

    public IResponse deleteFile(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        int fileId = rdata.getId();
        int parentId = ContentCache.getFileParentId(fileId);
        ContentData parent=ContentCache.getContent(parentId);
        assertRights(parent.hasUserReadRight(rdata.getLoginUser()));
        FileData data = ContentCache.getFile(fileId);
        FileBean.getInstance().deleteFile(data);
        ContentCache.setDirty();
        rdata.getAttributes().put("contentId", Integer.toString(parentId));
        rdata.setMessage($S("_fileDeleted"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return showContentAdministration(rdata,parentId);
    }

    protected IResponse showEditFile() {
        return new ForwardResponse("/WEB-INF/_jsp/file/editFile.ajax.jsp");
    }

    protected IResponse showContentAdministration(RequestData rdata, int contentId) {
        return new ForwardResponse("/ctrl/admin/openContentAdministration?contentId=" + contentId);
    }

}
