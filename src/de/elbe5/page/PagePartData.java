/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.base.BaseData;
import de.elbe5.request.RequestData;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class PagePartData extends BaseData implements Comparable<PagePartData> {

    public static final String KEY_PART = "partData";
    public static String LAYOUT_TYPE = "Part";

    protected String cssClass = "";
    protected String sectionName = "";
    protected int position = 0;
    protected String layout="";
    protected Map<String, PartField> fields = new HashMap<>();
    protected LocalDateTime publishDate = null;
    protected String publishedContent = "";
    protected boolean editable = true;

    public static String jspBasePath = "/WEB-INF/_jsp/_layout";

    public PagePartData() {
    }

    public PagePartBean getBean() {
        return PagePartBean.getInstance();
    }

    public void copyData(PagePartData data) {
        setId(PageBean.getInstance().getNextId());
        setLayout(data.getLayout());
        getFields().clear();
        for (PartField f : data.getFields().values()) {
            try {
                getFields().put(f.getName(), (PartField) f.clone());
            } catch (CloneNotSupportedException ignore) {
            }
        }
        setEditable((data.isEditable()));
    }

    @Override
    public int compareTo(PagePartData data) {
        return position - data.position;
    }

    public String getJspPath() {
        return jspBasePath;
    }

    public String getType() {
        return getClass().getName();
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getPartInclude() {
        return getTemplateUrl();
    }

    public String getEditPartInclude() {
        return getTemplateUrl();
    }

    public String getEditTitle() {
        return getLayout() + ", ID=" + getId();
    }

    public String getPartWrapperId() {
        return "part_" + getId();
    }

    public String getPartPositionName() {
        return "partpos_" + getId();
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void prepareCopy() {
        setNew(true);
        setId(PagePartBean.getInstance().getNextPartId());
    }

    public void setCreateValues(RequestData rdata) {
        super.setCreateValues(rdata);
        setSectionName(rdata.getAttributes().getString("sectionName"));
        setLayout(rdata.getAttributes().getString("layout"));
    }

    @Override
    public void setNewId(){
        setId(PagePartBean.getInstance().getNextPartId());
    }

    public void readBackendRequestData(RequestData rdata){

    }

    public void readFrontendRequestData(RequestData rdata) {
        // -1 if deleted
        setPosition(rdata.getAttributes().getInt(getPartPositionName(), -1));
        for (PartField field : getFields().values()) {
            field.readFrontendRequestData(rdata);
        }
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getTemplateUrl() {
        return "/WEB-INF/_jsp/_layout/"+ layout +".jsp";
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public boolean hasUnpublishedDraft() {
        return publishDate == null || publishDate.isBefore(getChangeDate());
    }

    public boolean isPublished() {
        return publishDate != null;
    }

    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }

    public String getPublishedContent() {
        return publishedContent;
    }

    public void setPublishedContent(String publishedContent) {
        this.publishedContent = publishedContent;
    }

    public Map<String, PartField> getFields() {
        return fields;
    }

    public PartField getField(String name) {
        return fields.get(name);
    }

    public PartField ensureHtmlField(String name) {
        PartField field = fields.get(name);
        if (field instanceof PartField)
            return field;
        PartField htmlfield = new PartField();
        htmlfield.setName(name);
        htmlfield.setPartId(getId());
        fields.put(name, htmlfield);
        return htmlfield;
    }


}
