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
<%@ page import="de.elbe5.user.UserCache" %>
<%@ page import="de.elbe5.user.UserData" %>
<%@ page import="de.elbe5.link.LinkData" %>
<%@ page import="de.elbe5.content.ContentNavType" %>
<%@ page import="de.elbe5.group.GroupData" %>
<%@ page import="de.elbe5.group.GroupBean" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.base.LocalizedSystemStrings" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    LinkData contentData = ContentData.getSessionContent(rdata, LinkData.class);
    String url = "/ctrl/content/saveBackendContent/" + contentData.getId();
    UserData creator = UserCache.getUser(contentData.getCreatorId());
    String creatorName = creator == null ? "" : creator.getName();
    UserData changer = UserCache.getUser(contentData.getChangerId());
    String changerName = changer == null ? "" : changer.getName();
    String header = contentData.isNew() ? $SH("_newContent") : $SH("_editContentData");
    List<GroupData> groups = GroupBean.getInstance().getAllGroups();
%>
<div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <h5 class="modal-title"><%=header%>:&nbsp;<%=$SH(contentData.getType())%>
            </h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
        <form:form url="<%=url%>" name="pageform" ajax="true" multi="true">
            <div class="modal-body">
                <form:formerror/>
                <h3><%=$SH("_settings")%>
                </h3>
                <form:line label="_idAndUrl"><%=$I(contentData.getId())%> - <%=$H(contentData.getUrl())%>
                </form:line>
                <form:line label="_creation"><%=$DT(contentData.getCreationDate())%> - <%=$H(creatorName)%>
                </form:line>
                <form:line label="_lastChange"><%=$DT(contentData.getChangeDate())%> - <%=$H(changerName)%>
                </form:line>

                <form:text name="displayName" label="_name" required="true" value="<%=$H(contentData.getDisplayName())%>"/>
                <form:text name="linkIcon" label="_linkIcon" required="true" value="<%=$H(contentData.getLinkIcon())%>"/>
                <form:textarea name="description" label="_description" height="5em"><%=$H(contentData.getDescription())%></form:textarea>
                <form:text name="linkUrl" label="_linkUrl" required="true" value="<%=$H(contentData.getLinkUrl())%>"/>
                <form:line label="_openAccess" padded="true">
                    <form:check name="openAccess" value="true" checked="<%=contentData.isOpenAccess()%>"/>
                </form:line>
                <form:select name="readerGroupId" label="_readerGroup">
                    <option value="0"  <%=contentData.getReaderGroupId()==0 ? "selected" : ""%>><%=$SH("_none")%></option>
                    <% for (GroupData group : groups){%>
                    <option value="<%=group.getId()%>" <%=contentData.getReaderGroupId()==group.getId() ? "selected" : ""%>><%=$H(group.getName())%></option>
                    <%}%>
                </form:select>
                <form:select name="editorGroupId" label="_editorGroup">
                    <option value="0"  <%=contentData.getEditorGroupId()==0 ? "selected" : ""%>><%=$SH("_none")%></option>
                    <% for (GroupData group : groups){%>
                    <option value="<%=group.getId()%>" <%=contentData.getEditorGroupId()==group.getId() ? "selected" : ""%>><%=$H(group.getName())%></option>
                    <%}%>
                </form:select>
                <form:select name="navType" label="_navType">
                    <option value="<%=ContentNavType.NONE%>" <%=contentData.getNavType().equals(ContentNavType.NONE) ? "selected" : ""%>><%=LocalizedSystemStrings.getInstance().html("navTypeNone")%>
                    </option>
                    <option value="<%=ContentNavType.HEADER%>" <%=contentData.getNavType().equals(ContentNavType.HEADER) ? "selected" : ""%>><%=LocalizedSystemStrings.getInstance().html("system.navTypeHeader")%>
                    </option>
                    <option value="<%=ContentNavType.FOOTER%>" <%=contentData.getNavType().equals(ContentNavType.FOOTER) ? "selected" : ""%>><%=LocalizedSystemStrings.getInstance().html("system.navTypeFooter")%>
                    </option>
                </form:select>
                <form:line label="_active" padded="true">
                    <form:check name="active" value="true" checked="<%=contentData.isActive()%>"/>
                </form:line>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-dismiss="modal"><%=$SH("_close")%>
                </button>
                <button type="submit" class="btn btn-primary"><%=$SH("_save")%>
                </button>
            </div>
        </form:form>
    </div>
</div>


