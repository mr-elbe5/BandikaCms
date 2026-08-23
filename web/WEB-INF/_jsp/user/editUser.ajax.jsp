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
<%@ page import="de.elbe5.group.GroupBean" %>
<%@ page import="de.elbe5.group.GroupData" %>
<%@ page import="de.elbe5.user.UserData" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    UserData user = (UserData) rdata.getSessionObject("userData");
    List<GroupData> groups = GroupBean.getInstance().getAllGroups();
    String label;
    String url = "/ctrl/user/saveUser/" + user.getId();
%>
<div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <h5 class="modal-title"><%=$SH("_editUser")%>
            </h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
        <form:form url="<%=url%>" name="userform" multi="true" ajax="true">
            <div class="modal-body">
                <form:formerror/>
                <h3><%=$SH("_settings")%>
                </h3>
                <form:line label="_id"><%=$I(user.getId())%>
                </form:line>
                <form:text name="login" label="_login" required="true" value="<%=$H(user.getLogin())%>"/>
                <%if (user.isNew()){%>
                <form:password name="password" label="_password" required="true"/>
                <form:password name="password2" label="_retypePassword" required="true"/>
                <%}else{%>
                <form:password name="password" label="_password"/>
                <form:password name="password2" label="_retypePassword"/>
                <%}%>
                <form:text name="name" label="_name" value="<%=$H(user.getName())%>"/>
                <form:text name="email" label="_email" value="<%=$H(user.getEmail())%>"/>
                <form:line label="_active" padded="true">
                    <form:check name="active" value="true" checked="<%=user.isActive()%>"/>
                </form:line>
                <h3><%=$SH("_groups")%>
                </h3>
                <form:line label="_group"><%=$SH("_inGroup")%>
                </form:line>
                <% for (GroupData gdata : groups) {%><%
                label = gdata.getName();%>
                <form:line label="<%=label%>" padded="true">
                    <form:check name="groupIds" value="<%=$I(gdata.getId())%>" checked="<%=user.getGroupIds().contains(gdata.getId())%>"/>
                </form:line>
                <%}%>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-dismiss="modal"><%=$SH("_close")%>
                </button>
                <button type="submit" class="btn btn-outline-primary"><%=$SH("_save")%>
                </button>
            </div>
        </form:form>
    </div>
</div>
