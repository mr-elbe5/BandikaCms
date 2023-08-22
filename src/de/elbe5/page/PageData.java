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
import de.elbe5.file.FileData;
import de.elbe5.request.RequestData;
import de.elbe5.response.IResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageData extends ContentData {

    public static String LAYOUT_TYPE = "Page";

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    private String keywords = "";
    protected String layout = "";
    protected LocalDateTime publishDate = null;
    protected String publishedContent="";

    protected Map<String, SectionData> sections = new HashMap<>();

    public static List<Class<? extends PagePartData>> pagePartClasses = new ArrayList<>();

    public ContentBean getBean() {
        return PageBean.getInstance();
    }

    public List<Class<? extends ContentData>> getChildClasses(){
        return PageData.childClasses;
    }
    public List<Class<? extends FileData>> getFileClasses(){
        return PageData.fileClasses;
    }

    // base data

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

    public PagePartData getPart(String pid) {
        for (SectionData section : getSections().values()) {
            PagePartData part = section.getPart(pid);
            if (part!=null)
                return part;
        }
        return null;
    }

    public void addPart(PagePartData part, String fromPartId, boolean setRanking) {
        SectionData section = getSection(part.getSectionName());
        if (section == null) {
            section = new SectionData();
            section.setPageId(getId());
            section.setName(part.getSectionName());
            sections.put(part.getSectionName(), section);
        }
        section.addPart(part, fromPartId, setRanking);
    }

    public void movePart(String sectionName, String id, int dir) {
        SectionData section = getSection(sectionName);
        section.movePart(id, dir);
    }

    public void deletePart(String pid) {
        for (SectionData section : getSections().values()) {
            PagePartData part = section.getPart(pid);
            if (part!=null) {
                section.deletePart(pid);
                break;
            }
        }
    }

    //used in controller
    @Override
    public String getContentDataJsp() {
        return "/WEB-INF/_jsp/page/editData.ajax.jsp";
    }

    //used in jsp
    protected void displayEditContent(PageContext context, JspWriter writer, RequestData rdata) throws IOException, ServletException {
        context.include("/WEB-INF/_jsp/page/editContent.inc.jsp");
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
        return new ContentResponse(this);
    }

    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        JspWriter writer = context.getOut();
        switch (getViewType()) {
            case VIEW_TYPE_PUBLISH: {
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
                setViewType(ContentData.VIEW_TYPE_SHOW);
                ContentCache.setDirty();
                writer.write("</div>");
            }
            break;
            case VIEW_TYPE_EDIT: {
                writer.write("<div id=\"pageContent\" class=\"editArea\">");
                displayEditContent(context, context.getOut(), rdata);
                writer.write("</div>");
            }
            break;
            case VIEW_TYPE_SHOWPUBLISHED: {
                writer.write("<div id=\"pageContent\" class=\"viewArea\">");
                if (isPublished())
                    displayPublishedContent(context, context.getOut(), rdata);
                writer.write("</div>");
            }
            break;
            default: {
                writer.write("<div id=\"pageContent\" class=\"viewArea\">");
                if (isPublished() && !hasUserEditRight(rdata))
                    displayPublishedContent(context, context.getOut(), rdata);
                else
                    displayDraftContent(context, context.getOut(), rdata);
                writer.write("</div>");
            }
            break;
        }
    }

    // multiple data

    public void copyData(ContentData data, RequestData rdata) {
        if (!(data instanceof PageData))
            return;
        PageData hcdata=(PageData)data;
        super.copyData(hcdata,rdata);
        setKeywords(hcdata.getKeywords());
        setLayout(hcdata.getLayout());
        for (String sectionName : hcdata.sections.keySet()) {
            SectionData section = new SectionData();
            section.setPageId(getId());
            section.copyData(hcdata.sections.get(sectionName));
            sections.put(sectionName, section);
        }
    }

    @Override
    public void readRequestData(RequestData rdata) {
        super.readRequestData(rdata);
        setKeywords(rdata.getAttributes().getString("keywords"));
        setLayout(rdata.getAttributes().getString("layout"));
        if (layout.isEmpty()) {
            rdata.addIncompleteField("layout");
        }
    }

    public void readFrontendRequestData(RequestData rdata) {
        for (SectionData section : getSections().values()) {
            section.readFrontendRequestData(rdata);
        }
    }

}
