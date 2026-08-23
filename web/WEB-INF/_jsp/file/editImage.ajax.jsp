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
<%@ page import="de.elbe5.user.UserCache" %>
<%@ page import="de.elbe5.file.ImageData" %>
<%@ page import="de.elbe5.request.ContentRequestKeys" %>
<%@ page import="de.elbe5.user.UserData" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    ImageData imageData = rdata.getSessionObject(ContentRequestKeys.KEY_FILE,ImageData.class);
    String url = "/ctrl/image/saveFile/" + imageData.getId();
    UserData creator = UserCache.getUser(imageData.getCreatorId());
    String creatorName = creator == null ? "" : creator.getName();
    UserData changer = UserCache.getUser(imageData.getChangerId());
    String changerName = changer == null ? "" : changer.getName();
%>
<div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <h5 class="modal-title"><%=$SH("_editImageSettings")%>
            </h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
        <form:form url="<%=url%>" name="imageform" ajax="true" multi="true">
            <div class="modal-body">
                <form:formerror/>
                <form:line label="_idAndFileName"><%=$I(imageData.getId())%> - <%=$H(imageData.getFileName())%>
                </form:line>
                <form:line label="_creation"><%=$DT(imageData.getCreationDate())%> - <%=$H(creatorName)%>
                </form:line>
                <form:line label="_lastChange"><%=$DT(imageData.getChangeDate())%> - <%=$H(changerName)%>
                </form:line>
                <% if (imageData.isNew()){%>
                <form:file name="file" label="_image" required="true"/>
                <%}%>
                <form:text name="displayName" label="_name" value="<%=$H(imageData.getDisplayName())%>"/>
                <form:textarea name="description" label="_description" height="3em"><%=$H(imageData.getDescription())%></form:textarea>
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


