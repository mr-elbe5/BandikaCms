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
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.request.ContentRequestKeys" %>
<%@ page import="de.elbe5.base.LocalizedSystemStrings" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    ContentData contentData = ContentData.getCurrentContent(rdata, ContentData.class);
    @SuppressWarnings("unchecked")
    List<Integer> openIds = rdata.getAttributes().get("openIds", List.class);
    String liClass = openIds != null
            ? openIds.contains(contentData.getId()) ? "open" : ""
            : "";
    List<Class<? extends ContentData>> childClasses = contentData.getChildClasses();
%>
<li class="<%=liClass%>">
    <span>
        <%=$H(contentData.getDisplayName())%>
    </span>
    <%if (contentData.hasUserEditRight(rdata.getLoginUser())) {%>
    <div class="icons">
        <a class="icon fa fa-eye" href="" onclick="return linkTo('/ctrl/content/show/<%=contentData.getId()%>');" title="<%=$SH("_view")%>"> </a>
        <a class="icon fa fa-pencil" href="" onclick="return openModalDialog('/ctrl/content/openEditBackendContent/<%=contentData.getId()%>');" title="<%=$SH("_edit")%>"> </a>
        <% if (contentData.getId() != ContentData.ID_ROOT){%>
        <a class="icon fa fa-scissors" href="" onclick="return linkTo('/ctrl/content/cutContent/<%=contentData.getId()%>');" title="<%=$SH("_cut")%>"> </a>
        <%}%>
        <%if (contentData.hasChildren()){%>
        <a class="icon fa fa-sort" href="" onclick="return openModalDialog('/ctrl/content/openSortChildContents/<%=contentData.getId()%>');" title="<%=$SH("_sortChildPages")%>"> </a>
        <%}%>
        <% if (contentData.getId() != ContentData.ID_ROOT){%>
        <a class="icon fa fa-trash-o" href="" onclick="if (confirmDelete()) return linkTo('/ctrl/content/deleteBackendContent/<%=contentData.getId()%>');" title="<%=$SH("_delete")%>"> </a>
        <%}%>
        <% if (rdata.hasClipboardData(ContentRequestKeys.KEY_CONTENT)) {%>
        <a class="icon fa fa-paste" href="" onclick="return linkTo('/ctrl/content/pasteContent?parentId=<%=contentData.getId()%>');" title="<%=$SH("_pasteContent")%>"> </a>
        <%
        }
        if (!childClasses.isEmpty()) {
            if (childClasses.size() == 1){%>
        <a class="icon fa fa-plus" onclick="return openModalDialog('/ctrl/content/openCreateBackendContent?parentId=<%=contentData.getId()%>&type=<%=childClasses.get(0).getName()%>');" title="<%=$SH("_newContent")%>"></a>
        <%} else {%>
        <a class="icon fa fa-plus dropdown-toggle" data-toggle="dropdown" title="<%=$SH("_newContent")%>"></a>
        <div class="dropdown-menu">
            <%for (Class<? extends ContentData> pageType : childClasses) {
                String name = LocalizedSystemStrings.getInstance().html(pageType.getName());
            %>
            <a class="dropdown-item" onclick="return openModalDialog('/ctrl/content/openCreateBackendContent?parentId=<%=contentData.getId()%>&type=<%=pageType.getName()%>');"><%=name%>
            </a>
            <%
                }%>
        </div>
        <%}
        }%>
    </div>
    <%}%>
    <ul>
        <jsp:include page="/WEB-INF/_jsp/content/backendTreeContentFiles.inc.jsp" flush="true" />
        <%if (contentData.hasChildren()) {
            for (ContentData childData : contentData.getChildren()) {
                childData.displayBackendTreeContent(pageContext, rdata);
            }
        }%>
    </ul>
</li>

