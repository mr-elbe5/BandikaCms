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
import de.elbe5.page.PageData;
import de.elbe5.page.PagePartData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspWriter;

public class PartTag extends BaseTag {

    private String cssClass = "";

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    static String editStart="<div id=\"{1}\" class=\"partWrapper {2}\" title=\"{3}\">";
    static String viewStart="<div id=\"{1}\" class=\"partWrapper {2}\">";
    static String end="</div>";

    public int doStartTag() {
        try {
            HttpServletRequest request = (HttpServletRequest) getContext().getRequest();
            RequestData rdata = RequestData.getRequestData(request);
            PageData contentData = rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, PageData.class);
            PagePartData partData = rdata.getAttributes().get(PagePartData.KEY_PART, PagePartData.class);
            JspWriter writer = getContext().getOut();
            if (partData != null) {
                partData.setCssClass(cssClass);

                if (contentData.isEditMode()) {
                    StringFormatter.write(writer,editStart,
                            partData.getPartWrapperId(),
                            StringHelper.toHtml(partData.getCssClass()),
                            StringHelper.toHtml(partData.getEditTitle())
                    );
                    getContext().include("/WEB-INF/_jsp/page/editPartHeader.inc.jsp", true);
                }
                else{
                    StringFormatter.write(writer,viewStart,
                            partData.getPartWrapperId(),
                            StringHelper.toHtml(partData.getCssClass())
                    );
                }
            }
        } catch (Exception e) {
            Log.error("could not write tag", e);
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag(){
        try {
            JspWriter writer = getContext().getOut();
            StringFormatter.write(writer,end);
        } catch (Exception e) {
            Log.error("could not write tag", e);
        }
        return EVAL_PAGE;
    }

}
