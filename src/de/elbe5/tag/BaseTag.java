/*
  Bandika CMS - A Java based modular Content Management System
  Copyright (C) 2009-2015 Michael Roennau

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.tag;

import de.elbe5.base.LocalizedStrings;
import de.elbe5.base.StringHelper;
import de.elbe5.request.RequestData;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.Tag;
import java.io.Writer;

public class BaseTag implements Tag {
    protected Tag parent = null;
    protected PageContext context = null;

    @Override
    public void setPageContext(PageContext pageContext) {
        context = pageContext;
    }

    public PageContext getContext() {
        return context;
    }

    public HttpServletRequest getRequest() {
        return (HttpServletRequest) getContext().getRequest();
    }

    public Writer getWriter() {
        return context.getOut();
    }

    @Override
    public void setParent(Tag tag) {
        parent = tag;
    }

    @Override
    public Tag getParent() {
        return parent;
    }

    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() {
        return EVAL_PAGE;
    }

    @Override
    public void release() {
    }

    protected String toHtml(String s) {
        return StringHelper.toHtml(s);
    }

    protected RequestData getRequestData() {
        return RequestData.getRequestData(getRequest());
    }

    protected String $S(String key){
        return LocalizedStrings.getInstance().string(key);
    }

    protected String $SH(String key){
        return LocalizedStrings.getInstance().html(key);
    }

    public String $SHM(String key){
        return LocalizedStrings.getInstance().htmlMultiline(key);
    }

}
