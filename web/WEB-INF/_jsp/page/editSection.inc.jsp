<%--
  Bandika CMS - A Java based modular Content Management System
  Copyright (C) 2009-2021 Michael Roennau

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
--%><%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="de.elbe5.page.SectionData" %>
<%@ page import="de.elbe5.page.PagePartData" %>
<%@ page import="de.elbe5.layout.LayoutCache" %>
<%@ page import="de.elbe5.layout.LayoutData" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.page.PageData" %>
<%@ page import="de.elbe5.page.LayoutPartData" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    SectionData sectionData = rdata.getAttributes().get(SectionData.KEY_SECTION, SectionData.class);
    List<LayoutData> partLayouts = LayoutCache.getLayouts(PagePartData.LAYOUT_TYPE);
%>
<div class="section <%=sectionData.getCssClass()%>" id="<%=sectionData.getSectionId()%>" title="Section <%=sectionData.getName()%>">
    <%-- add new top part --%>
    <div class="sectionEditButtons">
        <div class="btn-group btn-group-sm" role="group">
            <div class="btn-group btn-group-sm" role="group">
                <button type="button" class="btn btn-secondary fa fa-plus dropdown-toggle" data-toggle="dropdown" title="<%=$SH("_newPart")%>"></button>
                <div class="dropdown-menu">
                    <% for (Class<? extends PagePartData> cls : PageData.pagePartClasses) {
                        if (cls.equals(LayoutPartData.class)){
                            for (LayoutData layout : partLayouts){%>
                    <a class="dropdown-item" href="" onclick="return addPart(-1,'<%=$H(sectionData.getName())%>','<%=cls.getName()%>','<%=$H(layout.getName())%>');"><%=$SH(layout.getKey())%>
                    </a>
                    <%}
                    } else {%>
                    <a class="dropdown-item" href="" onclick="return addPart(-1,'<%=$H(sectionData.getName())%>','<%=cls.getName()%>');"><%=$SH("class."+cls.getName())%>
                    </a>
                    <%}
                    }%>
                </div>
            </div>
        </div>
    </div>
    <%-- existing parts --%>
    <%for (PagePartData partData : sectionData.getParts()) {
        rdata.getAttributes().put(PagePartData.KEY_PART, partData);
        String include = partData.getEditPartInclude();
        if (include != null) {%>
            <jsp:include page="<%=include%>" flush="true"/>
        <%}
        rdata.getAttributes().remove(PagePartData.KEY_PART);
    }%>
</div>








