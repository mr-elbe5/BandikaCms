<%--
  Bandika CMS - A Java based modular Content Management System
  Copyright (C) 2009-2021 Michael Roennau

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
--%>
<%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.file.ImageData" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    ContentData contentData = rdata.getRequestObject("treePage", ContentData.class);
%>
<li class="open">
    <a id="<%=contentData.getId()%>"><%=contentData.getName()%>
    </a>
    <ul>
        <% if (contentData.hasUserReadRight(rdata.getLoginUser())) {
            List<ImageData> images = contentData.getFiles(ImageData.class);
            for (ImageData image : images) {%>
        <li>
            <div class="treeline">
                <a id="<%=image.getId()%>" href="" onclick="return ckLinkCallback('<%=image.getStaticURL()%>');">
                    <img src="<%=image.getPreviewURL()%>" alt="<%=$H(image.getDisplayName())%>"/>
                    <%=$H(image.getDisplayName())%>
                </a>
                <a class="fa fa-eye" title="<%=$SH("_view")%>" href="<%=image.getStaticURL()%>" target="_blank"> </a>
            </div>
        </li>
        <%}
        }
        for (ContentData subPage : contentData.getChildren()) {
            rdata.setRequestObject("treePage", subPage);%>
        <jsp:include page="/WEB-INF/_jsp/ckeditor/imageLinkBrowserFolder.inc.jsp" flush="true"/>
        <%}
        rdata.setRequestObject("treePage", contentData);
        %>
    </ul>
</li>
