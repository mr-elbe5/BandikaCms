/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.content;

import de.elbe5.base.*;
import de.elbe5.base.BaseData;
import de.elbe5.file.*;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestType;
import de.elbe5.response.IMasterInclude;
import de.elbe5.response.IResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;

import java.io.IOException;
import java.util.*;

public class ContentData extends BaseData implements IMasterInclude, Comparable<ContentData> {

    public static <T extends ContentData> T getCurrentContent(RequestData rdata, Class<T> cls) {
        return rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, cls);
    }

    public static <T extends ContentData> T getSessionContent(RequestData rdata, Class<T> cls) {
        return rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, cls);
    }

    public static ContentData getCurrentContent(RequestData rdata) {
        return rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, ContentData.class);
    }

    public static final int ID_ROOT = 1;

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    // base data
    private String name = "";
    private String path = "";
    private String displayName = "";
    private String description = "";
    private ContentNavType navType = ContentNavType.NONE;
    private boolean active = true;

    // tree data
    protected int parentId = 0;
    protected ContentData parent = null;
    protected int ranking = 0;
    private final List<ContentData> children = new ArrayList<>();
    private final List<FileData> files = new ArrayList<>();

    //runtime
    boolean editMode = false;
    //protected ContentViewType viewType = ContentViewType.SHOW;

    public ContentData() {
    }

    public String getType() {
        return getClass().getName();
    }

    public ContentBean getBean() {
        return ContentBean.getInstance();
    }

    public List<Class<? extends ContentData>> getChildClasses() {
        return ContentData.childClasses;
    }

    public List<Class<? extends FileData>> getFileClasses() {
        return ContentData.fileClasses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void generatePath() {
        if (getParent() == null)
            return;
        setPath(getParent().getPath() + "/" + StringHelper.toUrl(getName().toLowerCase()));
    }

    public String getUrl() {
        if (getPath().isEmpty())
            return "/home.html";
        return getPath() + ".html";
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNavDisplay() {
        return StringHelper.toHtml(getDisplayName());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public ContentNavType getNavType() {
        return navType;
    }

    public String getNavTypeString() {
        return navType.toString();
    }

    public boolean isInHeaderNav() {
        return navType.equals(ContentNavType.HEADER);
    }

    public boolean isInFooterNav() {
        return navType.equals(ContentNavType.FOOTER);
    }

    public void setNavType(ContentNavType navType) {
        this.navType = navType;
    }
    public void setNavType(String type) {
        try{
            navType = ContentNavType.valueOf(type);
        }
        catch(IllegalArgumentException e){
            navType = ContentNavType.NONE;
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // tree data

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        if (parentId == getId()) {
            Log.error("parentId must not be this: " + parentId);
            this.parentId = 0;
        } else {
            this.parentId = parentId;
        }
    }

    public ContentData getParent() {
        return parent;
    }

    public <T extends ContentData> T getParent(Class<T> cls) {
        try {
            return cls.cast(getParent());
        }
        catch(NullPointerException | ClassCastException e){
            return null;
        }
    }

    public void setParent(ContentData parent) {
        this.parent = parent;
    }

    public boolean setParent(ContentData parent, Class<? extends ContentData> cls) {
        try {
            if (cls.isInstance(parent)) {
                this.parent = parent;
                return true;
            }
        } catch (NullPointerException | ClassCastException e) {
            // ignore
        }
        this.parent = null;
        Log.error("could not set parent of correct class");
        return false;
    }

    public void collectParentIds(Set<Integer> ids) {
        ids.add(getId());
        if (parent != null)
            parent.collectParentIds(ids);
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public List<ContentData> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    public <T extends ContentData> List<T> getChildren(Class<T> cls) {
        List<T> list = new ArrayList<>();
        try {
            for (ContentData data : getChildren()) {
                if (cls.isInstance(data))
                    list.add(cls.cast(data));
            }
        } catch (NullPointerException | ClassCastException e) {
            return null;
        }
        return list;
    }

    public void getAllChildren(List<ContentData> list) {
        if (!hasChildren())
            return;
        for (ContentData data : getChildren()) {
            list.add(data);
            data.getAllChildren(list);
        }
    }

    public <T extends ContentData> void getAllChildren(List<T> list, Class<T> cls) {
        if (!hasChildren())
            return;
        for (ContentData data : getChildren()) {
            try {
                if (cls.isInstance(data))
                    list.add(cls.cast(data));
            } catch (NullPointerException | ClassCastException e) {
                // ignore
            }
            data.getAllChildren(list, cls);
        }
    }

    public int getChildIndex(ContentData child){
        for (int i= 0; i<children.size(); i++){
            if (children.get(i).getId() == child.getId()){
                return i;
            }
        }
        return -1;
    }

    public void addChild(ContentData data) {
        children.add(data);
    }

    public void initializeChildren() {
        if (hasChildren()) {
            Collections.sort(children);
            for (ContentData child : children) {
                child.generatePath();
                child.initializeChildren();
            }
        }
    }

    public List<FileData> getFiles() {
        return files;
    }

    public boolean hasFiles() {
        return !getFiles().isEmpty();
    }

    public <T extends FileData> List<T> getFiles(Class<T> cls) {
        List<T> list = new ArrayList<>();
        try {
            for (FileData data : getFiles()) {
                if (cls.isInstance(data))
                    list.add(cls.cast(data));
            }
        } catch (NullPointerException | ClassCastException e) {
            return null;
        }
        return list;
    }

    public <T extends FileData> T getFileWithId(int id, Class<T> cls) {
        try {
            for (FileData data : getFiles()) {
                if (data.getId() == id) {
                    if (cls.isInstance(data))
                        return cls.cast(data);
                    else
                        return null;
                }
            }
        } catch (NullPointerException | ClassCastException e) {
            return null;
        }
        return null;
    }

    public void addFile(FileData data) {
        files.add(data);
    }

    // defaults for overriding

    public String getKeywords() {
        return "";
    }

    // view

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public IResponse getDefaultView() {
        return new ContentResponse(this);
    }

    public String getBackendContentTreeJsp() {
        return "/WEB-INF/_jsp/content/backendTreeContent.inc.jsp";
    }

    //used in admin jsp
    public void displayBackendTreeContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        //backup
        ContentData currentContent = rdata.getRequestObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, this);
        context.include(getBackendContentTreeJsp(), true);
        //restore
        rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, currentContent);
    }

    public String getBackendEditJsp() {
        return "/WEB-INF/_jsp/content/editBackendContent.ajax.jsp";
    }

    public String getFrontendContentTreeJsp() {
        return "/WEB-INF/_jsp/content/frontendTreeContent.inc.jsp";
    }

    public String getFrontendEditJsp() {
        return "";
    }

    //used in jsp
    public void displayFrontendTreeContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        //backup
        ContentData currentContent = rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, this);
        context.include(getFrontendContentTreeJsp(), true);
        //restore
        rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, currentContent);
    }

    //used in jsp/tag
    @Override
    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
    }

    @Override
    public void appendContent(StringBuilder sb, RequestData rdata) {

    }

    // multiple data

    @Override
    public void setNewId(){
        setId(ContentBean.getInstance().getNextId());
    }

    public void setParentValues(ContentData parent){
        setParentId(parent.getId());
        setParent(parent);
        setRanking(parent.getChildren().size());
    }

    // on openEditBackend
    public void setUpdateValues(ContentData cachedData, RequestData rdata) {
        if (cachedData == null)
            return;
        super.setUpdateValues(rdata);
        setParent(cachedData.getParent());
        setPath(cachedData.getPath());
        for (ContentData subContent : cachedData.getChildren()) {
            getChildren().add(subContent);
        }
        for (FileData file : cachedData.getFiles()) {
            getFiles().add(file);
        }
    }

    public void readRequestData(RequestData rdata, RequestType type){
        Log.log("ContentData.readRequestData");
        switch (type) {
            case backend -> {
                setDisplayName(rdata.getAttributes().getString("displayName").trim());
                setName(StringHelper.toSafeWebName(getDisplayName()));
                setDescription(rdata.getAttributes().getString("description"));
                setNavType(rdata.getAttributes().getString("navType"));
                setActive(rdata.getAttributes().getBoolean("active"));
                if (name.isEmpty()) {
                    rdata.addIncompleteField("name");
                }
            }
            case frontend -> {
            }
        }
    }

    @Override
    public int compareTo(ContentData data) {
        int i = getRanking() - data.getRanking();
        if (i != 0)
            return i;
        return getDisplayName().compareTo(data.getDisplayName());
    }

}
