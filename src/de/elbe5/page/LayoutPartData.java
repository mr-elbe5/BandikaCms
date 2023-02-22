/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.request.RequestData;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LayoutPartData extends PagePartData {

    protected String layout="";
    protected LocalDateTime publishDate = null;
    protected String publishedContent = "";

    protected Map<String, PartField> fields = new HashMap<>();

    public LayoutPartData() {
    }

    public String getJspPath() {
        return jspBasePath + "/page";
    }

    public void copyData(PagePartData data) {
        super.copyData(data);
        if (!(data instanceof LayoutPartData))
            return;
        LayoutPartData tpdata=(LayoutPartData)data;
        setLayout(tpdata.getLayout());
        getFields().clear();
        for (PartField f : tpdata.getFields().values()) {
            try {
                getFields().put(f.getName(), (PartField) f.clone());
            } catch (CloneNotSupportedException ignore) {
            }
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

    public String getPartInclude() {
        return getTemplateUrl();
    }

    public String getEditPartInclude() {
        return getTemplateUrl();
    }

    public String getEditTitle() {
        return getLayout() + ", ID=" + getId();
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

    public PartTextField ensureTextField(String name) {
        PartField field = fields.get(name);
        if (field instanceof PartTextField)
            return (PartTextField) field;
        PartTextField textfield = new PartTextField();
        textfield.setName(name);
        textfield.setPartId(getId());
        fields.put(name, textfield);
        return textfield;
    }

    public PartHtmlField ensureHtmlField(String name) {
        PartField field = fields.get(name);
        if (field instanceof PartHtmlField)
            return (PartHtmlField) field;
        PartHtmlField htmlfield = new PartHtmlField();
        htmlfield.setName(name);
        htmlfield.setPartId(getId());
        fields.put(name, htmlfield);
        return htmlfield;
    }

    public PartScriptField ensureScriptField(String name) {
        PartField field = fields.get(name);
        if (field instanceof PartScriptField)
            return (PartScriptField) field;
        PartScriptField scriptField = new PartScriptField();
        scriptField.setName(name);
        scriptField.setPartId(getId());
        fields.put(name, scriptField);
        return scriptField;
    }

    @Override
    public void setCreateValues(RequestData rdata) {
        super.setCreateValues(rdata);
        setLayout(rdata.getAttributes().getString("layout"));
    }

    @Override
    public void readFrontendRequestData(RequestData rdata) {
        super.readFrontendRequestData(rdata);
        for (PartField field : getFields().values()) {
            field.readFrontendRequestData(rdata);
        }
    }

    public PartField getNewField(String type) {
        switch (type) {
            case PartTextField.FIELDTYPE:
                return new PartTextField();
            case PartHtmlField.FIELDTYPE:
                return new PartHtmlField();
            case PartScriptField.FIELDTYPE:
                return new PartScriptField();
        }
        return null;
    }

}
