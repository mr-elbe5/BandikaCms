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
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="de.elbe5.base.DateHelper" %>
<%@ page import="de.elbe5.content.*" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    List<ContentDayLog> dayLogs = ContentLogBean.getInstance().getAllViewCounts();
%>
<div id="pageContent">
    <form:message/>
    <section class="logSection">
        <h3><%=$SH("_clicksPerDay")%></h3>
        <a class="icon fa fa-trash-o" href="/ctrl/admin/resetContentLog" title="<%=$SH("_reset")%>"></a>
        <% if (rdata.hasContentEditRight()) { %>
        <table>
            <% for (ContentDayLog dayLog : dayLogs) {%>
            <tr>
                <th colspan="2">
                    <%=DateHelper.toHtmlDate(dayLog.getDay())%>
                </th>
            </tr>
            <% for (ContentLog log : dayLog.getLogs()) {%>
            <tr>
                <td>
                    <%=$H(ContentCache.getContent(log.getId()).getDisplayName())%>
                </td>
                <td>
                    <%=log.getCount()%>
                </td>
            </tr>
            <%}%><%}%>
        </table>
        <%}%>
    </section>
</div>

