/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.tag;

import de.elbe5.base.Log;
import de.elbe5.base.StringFormatter;
import de.elbe5.base.StringHelper;
import de.elbe5.content.ContentData;
import de.elbe5.content.ContentViewType;
import de.elbe5.page.PageData;
import de.elbe5.page.LayoutPartData;
import de.elbe5.page.PagePartData;
import de.elbe5.page.PartTextField;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspWriter;

public class TextFieldTag extends FieldTag {

    private int rows = 1;

    public void setRows(int rows) {
        this.rows = rows;
    }

    @Override
    public int doStartTag() {
        try {
            HttpServletRequest request = (HttpServletRequest) getContext().getRequest();
            RequestData rdata = RequestData.getRequestData(request);
            JspWriter writer = getContext().getOut();
            PageData contentData = rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, PageData.class);
            LayoutPartData partData = rdata.getAttributes().get(PagePartData.KEY_PART, LayoutPartData.class);

            PartTextField field = partData.ensureTextField(name);

            boolean editMode = contentData.getViewType().equals(ContentViewType.EDIT);
            String content = field.getContent();
            if (editMode) {
                if (rows > 1)
                    StringFormatter.write(writer, "<textarea class=\"editField\" name=\"{1}\" rows=\"{2}\">{3}</textarea>", field.getIdentifier(), Integer.toString(rows), StringHelper.toHtml(content.isEmpty() ? placeholder : content));
                else
                    StringFormatter.write(writer, "<input type=\"text\" class=\"editField\" name=\"{1}\" placeholder=\"{2}\" value=\"{3}\" />", field.getIdentifier(), field.getIdentifier(), StringHelper.toHtml(content));
            } else {
                if (content.length() == 0) {
                    writer.write("&nbsp;");
                } else {
                    writer.write(StringHelper.toHtmlMultiline(content));
                }
            }

        } catch (Exception e) {
            Log.error("could not write tag", e);
        }
        return SKIP_BODY;
    }

}

