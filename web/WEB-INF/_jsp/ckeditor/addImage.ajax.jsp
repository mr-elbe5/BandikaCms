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
<%@ page import="de.elbe5.page.PageData" %>
<%@ page import="de.elbe5.file.ImageData" %>
<%@ page import="de.elbe5.page.PageCache" %>
<%@ page import="de.elbe5.request.RequestKeys" %>
<%@ page import="de.elbe5.page.PageData" %>
<%@ page import="de.elbe5.page.PageCache" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    PageData contentData = rdata.getSessionObject(RequestData.KEY_PAGE, PageData.class);
    int imageId=rdata.getAttributes().getInt("imageId");
    ImageData image = PageCache.getFile(imageId,ImageData.class);
    assert(image != null);
%>
<li>
    <div class="treeline">
        <a id="<%=image.getId()%>" href="" onclick="return ckImgCallback('<%=image.getStaticURL()%>');">
            <img src="/ctrl/image/showPreview/<%=image.getId()%>" alt="<%=$H(image.getDisplayName())%>"/>
            <%=$H(image.getDisplayName())%>
        </a>
        <a class="fa fa-eye" title="<%=$SH("_view")%>" href="<%=image.getStaticURL()%>" target="_blank"> </a>
    </div>
</li>
