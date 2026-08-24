/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.base.*;
import de.elbe5.base.BaseData;
import de.elbe5.file.*;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;
import de.elbe5.request.RequestType;
import de.elbe5.response.IMasterInclude;
import de.elbe5.response.IResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;

public class PageData extends BaseData implements IMasterInclude, Comparable<PageData> {

    public static PageData getCurrentPage(RequestData rdata) {
        return rdata.getCurrentPageInRequestOrSession(RequestKeys.KEY_PAGE);
    }

    public static PageData getSessionPage(RequestData rdata) {
        return rdata.getSessionObject(RequestKeys.KEY_PAGE, PageData.class);
    }

    public static final int ID_ROOT = 1;

    // base data
    private String name = "";
    private String path = "";
    private String displayName = "";
    private PageNavType navType = PageNavType.NONE;
    private boolean active = true;
    protected String keywords = "";
    protected String layout = "";
    protected LocalDateTime publishDate = null;
    protected String publishedContent="";

    // tree data
    protected int parentId = 0;
    protected PageData parent = null;
    protected int ranking = 0;

    protected Map<String, SectionData> sections = new HashMap<>();

    public static List<Class<? extends PagePartData>> pagePartClasses = new ArrayList<>();

    private final List<PageData> children = new ArrayList<>();
    private final List<FileData> files = new ArrayList<>();

    //runtime
    boolean editMode = false;
    boolean publishing = false;
    boolean showPublished = false;

    public PageData() {
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

    public PageNavType getNavType() {
        return navType;
    }

    public String getNavTypeString() {
        return navType.toString();
    }

    public boolean isInHeaderNav() {
        return navType.equals(PageNavType.HEADER);
    }

    public boolean isInFooterNav() {
        return navType.equals(PageNavType.FOOTER);
    }

    public void setNavType(PageNavType navType) {
        this.navType = navType;
    }
    public void setNavType(String type) {
        try{
            navType = PageNavType.valueOf(type);
        }
        catch(IllegalArgumentException e){
            navType = PageNavType.NONE;
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getLayout() {
        return layout;
    }

    public String getLayoutUrl() {
        return "/WEB-INF/_jsp/_layout/"+ layout +".jsp";
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate=publishDate;
    }

    public String getPublishedContent() {
        return publishedContent;
    }

    public void setPublishedContent(String publishedContent) {
        this.publishedContent = publishedContent;
    }

    public void reformatPublishedContent() {
        Document doc= Jsoup.parseBodyFragment(getPublishedContent());
        setPublishedContent(doc.body().html());
    }

    public boolean hasUnpublishedDraft() {
        return publishDate == null || publishDate.isBefore(getChangeDate());
    }

    public boolean isPublished() {
        return getPublishDate() != null;
    }

    public boolean isPublishing() {
        return publishing;
    }

    public void setPublishing(boolean publishing) {
        this.publishing = publishing;
    }

    public boolean showPublished() {
        return showPublished;
    }

    public void showPublished(boolean showPublished) {
        this.showPublished = showPublished;
    }

    public Map<String, SectionData> getSections() {
        return sections;
    }

    public SectionData getSection(String sectionName) {
        return sections.get(sectionName);
    }

    public SectionData ensureSection(String sectionName) {
        if (!sections.containsKey(sectionName)) {
            SectionData section = new SectionData();
            section.setPageId(getId());
            section.setName(sectionName);
            sections.put(sectionName, section);
            return section;
        }
        return sections.get(sectionName);
    }

    // part data

    public void sortParts() {
        for (SectionData section : sections.values()) {
            section.sortParts();
        }
    }

    public PagePartData getPart(int pid) {
        for (SectionData section : getSections().values()) {
            PagePartData part = section.getPart(pid);
            if (part!=null)
                return part;
        }
        return null;
    }

    public void addPart(PagePartData part, int fromPartId, boolean setRanking) {
        SectionData section = getSection(part.getSectionName());
        if (section == null) {
            section = new SectionData();
            section.setPageId(getId());
            section.setName(part.getSectionName());
            sections.put(part.getSectionName(), section);
        }
        section.addPart(part, fromPartId, setRanking);
    }

    public void movePart(String sectionName, int id, int dir) {
        SectionData section = getSection(sectionName);
        section.movePart(id, dir);
    }

    public void deletePart(int pid) {
        for (SectionData section : getSections().values()) {
            PagePartData part = section.getPart(pid);
            if (part!=null) {
                section.deletePart(pid);
                break;
            }
        }
    }

    //used in controller
    public String getBackendEditJsp() {
        return "/WEB-INF/_jsp/page/editBackendContent.ajax.jsp";
    }

    //used in jsp
    protected void displayEditContent(PageContext context, JspWriter writer, RequestData rdata) throws IOException, ServletException {
        context.include("/WEB-INF/_jsp/page/editFrontendContent.inc.jsp");
    }

    //used in jsp
    protected void displayDraftContent(PageContext context, JspWriter writer, RequestData rdata) throws IOException, ServletException {
        context.include(getLayoutUrl());
    }

    //used in jsp
    protected void displayPublishedContent(PageContext context, JspWriter writer, RequestData rdata) throws IOException, ServletException {
        writer.write(publishedContent);
    }

    public IResponse getDefaultView(){
        return new PageResponse(this);
    }

    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        JspWriter writer = context.getOut();
        if (isPublishing()){
            writer.write("<div id=\"pageContent\" class=\"viewArea\">");
            StringWriter stringWriter = new StringWriter();
            context.pushBody(stringWriter);
            displayDraftContent(context, context.getOut(), rdata);
            setPublishedContent(stringWriter.toString());
            reformatPublishedContent();
            context.popBody();
            //Log.log("publishing page " + getDisplayName());
            if (!PageBean.getInstance().publishPage(this)) {
                Log.error("error writing published content");
            }
            writer.write(getPublishedContent());
            setPublishing(false);
            setEditMode(false);
            PageCache.setDirty();
            writer.write("</div>");
        }
        else if (isEditMode()){
            writer.write("<div id=\"pageContent\" class=\"editArea\">");
            displayEditContent(context, context.getOut(), rdata);
            writer.write("</div>");
        }
        else if (isPublished() && showPublished()){
            writer.write("<div id=\"pageContent\" class=\"viewArea\">");
            displayPublishedContent(context, context.getOut(), rdata);
            writer.write("</div>");
            showPublished(false);
        }
        else {
            writer.write("<div id=\"pageContent\" class=\"viewArea\">");
            if (isPublished() && !rdata.isLoggedIn())
                displayPublishedContent(context, context.getOut(), rdata);
            else
                displayDraftContent(context, context.getOut(), rdata);
            writer.write("</div>");
        }
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

    public PageData getParent() {
        return parent;
    }

    public <T extends PageData> T getParent(Class<T> cls) {
        try {
            return cls.cast(getParent());
        }
        catch(NullPointerException | ClassCastException e){
            return null;
        }
    }

    public void setParent(PageData parent) {
        this.parent = parent;
    }

    public boolean setParent(PageData parent, Class<? extends PageData> cls) {
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

    public List<PageData> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    public <T extends PageData> List<T> getChildren(Class<T> cls) {
        List<T> list = new ArrayList<>();
        try {
            for (PageData data : getChildren()) {
                if (cls.isInstance(data))
                    list.add(cls.cast(data));
            }
        } catch (NullPointerException | ClassCastException e) {
            return null;
        }
        return list;
    }

    public void getAllChildren(List<PageData> list) {
        if (!hasChildren())
            return;
        for (PageData data : getChildren()) {
            list.add(data);
            data.getAllChildren(list);
        }
    }

    public <T extends PageData> void getAllChildren(List<T> list, Class<T> cls) {
        if (!hasChildren())
            return;
        for (PageData data : getChildren()) {
            try {
                if (cls.isInstance(data))
                    list.add(cls.cast(data));
            } catch (NullPointerException | ClassCastException e) {
                // ignore
            }
            data.getAllChildren(list, cls);
        }
    }

    public int getChildIndex(PageData child){
        for (int i= 0; i<children.size(); i++){
            if (children.get(i).getId() == child.getId()){
                return i;
            }
        }
        return -1;
    }

    public void addChild(PageData data) {
        children.add(data);
    }

    public void initializeChildren() {
        if (hasChildren()) {
            Collections.sort(children);
            for (PageData child : children) {
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

    // view

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getBackendContentTreeJsp() {
        return "/WEB-INF/_jsp/content/backendTreeContent.inc.jsp";
    }

    //used in admin jsp
    public void displayBackendTreeContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        //backup
        PageData currentContent = rdata.getRequestObject(RequestKeys.KEY_PAGE, PageData.class);
        rdata.setRequestObject(RequestKeys.KEY_PAGE, this);
        context.include(getBackendContentTreeJsp(), true);
        //restore
        rdata.setRequestObject(RequestKeys.KEY_PAGE, currentContent);
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
        PageData currentContent = rdata.getCurrentPageInRequestOrSession(RequestKeys.KEY_PAGE);
        rdata.setRequestObject(RequestKeys.KEY_PAGE, this);
        context.include(getFrontendContentTreeJsp(), true);
        //restore
        rdata.setRequestObject(RequestKeys.KEY_PAGE, currentContent);
    }

    //used in jsp/tag
    public void displayPage(PageContext context, RequestData rdata) throws IOException, ServletException {
    }

    @Override
    public void appendContent(StringBuilder sb, RequestData rdata) {

    }

    // multiple data

    @Override
    public void setNewId(){
        setId(PageBean.getInstance().getNextId());
    }

    public void setParentValues(PageData parent){
        setParentId(parent.getId());
        setParent(parent);
        setRanking(parent.getChildren().size());
    }

    // on openEditBackend
    public void setUpdateValues(PageData cachedData, RequestData rdata) {
        if (cachedData == null)
            return;
        super.setUpdateValues(rdata);
        setParent(cachedData.getParent());
        setPath(cachedData.getPath());
        for (PageData subContent : cachedData.getChildren()) {
            getChildren().add(subContent);
        }
        for (FileData file : cachedData.getFiles()) {
            getFiles().add(file);
        }
    }

    public void readRequestData(RequestData rdata, RequestType type){
        Log.log("PageData.readRequestData");
        super.readRequestData(rdata, type);
        switch (type) {
            case backend -> {
                setDisplayName(rdata.getAttributes().getString("displayName").trim());
                setName(StringHelper.toSafeWebName(getDisplayName()));
                setNavType(rdata.getAttributes().getString("navType"));
                setActive(rdata.getAttributes().getBoolean("active"));
                setKeywords(rdata.getAttributes().getString("keywords"));
                setLayout(rdata.getAttributes().getString("layout"));
                if (layout.isEmpty()) {
                    rdata.addIncompleteField("layout");
                }
                if (name.isEmpty()) {
                    rdata.addIncompleteField("name");
                }
            }
            case frontend -> {
                for (SectionData section : getSections().values()) {
                    section.readRequestData(rdata, type);
                }
            }
        }
    }

    @Override
    public int compareTo(PageData data) {
        int i = getRanking() - data.getRanking();
        if (i != 0)
            return i;
        return getDisplayName().compareTo(data.getDisplayName());
    }

}
