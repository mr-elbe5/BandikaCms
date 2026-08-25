/*
 Bandika CMS - A Java based modular File Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import de.elbe5.application.ApplicationPath;
import de.elbe5.page.PageCache;
import de.elbe5.page.PageData;
import de.elbe5.request.*;
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
        assertLoggedIn(rdata);
        int id = rdata.getId();
        FileData data = PageCache.getFile(id);
        rdata.getAttributes().put("download", "true");
        return show(data, rdata);
    }

    private IResponse show(FileData data, RequestData rdata){
        assertLoggedIn(rdata);
        PageData parent= PageCache.getContent(data.getParentId());
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
        assertLoggedIn(rdata);
        int parentId = rdata.getAttributes().getInt("parentId");
        PageData parentData = PageCache.getContent(parentId);
        String type=rdata.getAttributes().getString("type");
        FileData data = FileBean.getInstance().getNewFileData(type);
        data.setCreateValues(rdata);
        data.setParentValues(parentData);
        rdata.setSessionFile(data);
        return new ForwardResponse(data.getEditURL());
    }

    public IResponse cutFile(RequestData rdata) {
        assertLoggedIn(rdata);
        int fileId = rdata.getId();
        FileData data = FileBean.getInstance().getFile(fileId,true);
        PageData parent= PageCache.getContent(data.getParentId());
        rdata.setClipboardData(RequestData.KEY_FILE, data);
        return showContentAdministration(rdata,parent.getId());
    }

    public IResponse copyFile(RequestData rdata) {
        assertLoggedIn(rdata);
        int fileId = rdata.getId();
        FileData data = FileBean.getInstance().getFile(fileId,true);
        PageData parent= PageCache.getContent(data.getParentId());
        data.setNew(true);
        data.setId(FileBean.getInstance().getNextId());
        data.setCreatorId(rdata.getUserId());
        data.setChangerId(rdata.getUserId());
        rdata.setClipboardData(RequestData.KEY_FILE, data);
        return showContentAdministration(rdata,parent.getId());
    }

    public IResponse pasteFile(RequestData rdata) {
        assertLoggedIn(rdata);
        int parentId = rdata.getAttributes().getInt("parentId");
        FileData data=rdata.getClipboardData(RequestData.KEY_FILE, FileData.class);
        PageData parent= PageCache.getContent(parentId);
        if (parent == null){
            rdata.setMessage($S("_actionNotExcecuted"), RequestData.MESSAGE_TYPE_ERROR);
            return showContentAdministration(rdata, parentId);
        }
        data.setParentId(parentId);
        data.setParent(parent);
        data.setChangerId(rdata.getUserId());
        FileBean.getInstance().saveFile(data, true);
        rdata.clearClipboardData(RequestData.KEY_FILE);
        PageCache.setDirty();
        rdata.setMessage($S("_filePasted"), RequestData.MESSAGE_TYPE_SUCCESS);
        return showContentAdministration(rdata,data.getId());
    }

    public IResponse deleteFile(RequestData rdata) {
        assertLoggedIn(rdata);
        int fileId = rdata.getId();
        int parentId = PageCache.getFileParentId(fileId);
        PageData parent= PageCache.getContent(parentId);
        FileData data = PageCache.getFile(fileId);
        FileBean.getInstance().deleteFile(data);
        PageCache.setDirty();
        rdata.getAttributes().put("contentId", Integer.toString(parentId));
        rdata.setMessage($S("_fileDeleted"), RequestData.MESSAGE_TYPE_SUCCESS);
        return showContentAdministration(rdata,parentId);
    }

    protected IResponse showEditFile() {
        return new ForwardResponse("/WEB-INF/_jsp/file/editFile.ajax.jsp");
    }

    protected IResponse showContentAdministration(RequestData rdata, int contentId) {
        return new ForwardResponse("/ctrl/admin/openContentAdministration?contentId=" + contentId);
    }

}
