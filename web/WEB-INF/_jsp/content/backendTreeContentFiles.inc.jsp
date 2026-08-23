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
<%@ page import="de.elbe5.file.FileData" %>
<%@ page import="de.elbe5.base.LocalizedSystemStrings" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    ContentData contentData = ContentData.getCurrentContent(rdata);
    List<Class<? extends FileData>> fileClasses=contentData.getFileClasses();
    int fileId=rdata.getAttributes().getInt("fileId");
%>
        <li class="files open">
            <span>[<%=$SH("_files")%>]</span>
            <%if (contentData.hasUserEditRight(rdata.getLoginUser())) {%>
            <div class="icons">
                <% if (rdata.hasClipboardData(ContentRequestKeys.KEY_FILE)) {%>
                <a class="icon fa fa-paste" href="" onclick="return linkTo('/ctrl/file/pasteFile?parentId=<%=contentData.getId()%>');" title="<%=$SH("_pasteFile")%>"> </a>
                <%}
                    if (!fileClasses.isEmpty()) {
                        if (fileClasses.size() == 1){%>
                <a class="icon fa fa-plus" onclick="return openModalDialog('/ctrl/file/openCreateFile?parentId=<%=contentData.getId()%>&type=<%=fileClasses.get(0).getName()%>');" title="<%=$SH("_newFile")%>"></a>
                        <%} else {%>
                <a class="icon fa fa-plus dropdown-toggle" data-toggle="dropdown" title="<%=$SH("_newFile")%>"></a>
                <div class="dropdown-menu">
                    <%
                        for (Class<? extends FileData> fileType : fileClasses) {
                        String name = LocalizedSystemStrings.getInstance().html(fileType.getName());
                    %>
                    <a class="dropdown-item" onclick="return openModalDialog('/ctrl/file/openCreateFile?parentId=<%=contentData.getId()%>&type=<%=fileType.getName()%>');"><%=name%>
                    </a>
                        <%}%>
                </div>
                    <%}
                }%>
            </div>
            <%}%>
            <ul>
                <%
                    List<FileData> files = contentData.getFiles();
                    for (FileData file : files) {%>
                <li class="<%=fileId==file.getId() ? "current" : ""%>">
                    <div class="treeline">
                        <span class="fa <%=file.getIconStyle()%> hoverLine" id="<%=file.getId()%>">
                            <%=file.getDisplayName()%>
                            <% if (file.isImage()){%>
                            <span class="hoverImage">
                                <img src="/ctrl/image/showPreview/<%=file.getId()%>" alt="<%=$H(file.getFileName())%>"/>
                            </span>
                            <%}%>
                        </span>
                        <div class="icons">
                            <a class="icon fa fa-eye" href="<%=file.getStaticURL()%>" target="_blank" title="<%=$SH("_view")%>"> </a>
                            <a class="icon fa fa-download" href="<%=file.getStaticURL()%>?download=true" title="<%=$SH("_download")%>"> </a>
                            <a class="icon fa fa-pencil" href="" onclick="return openModalDialog('/ctrl/<%=file.getControllerKey()%>/openEditFile/<%=file.getId()%>');" title="<%=$SH("_edit")%>"> </a>
                            <a class="icon fa fa-scissors" href="" onclick="return linkTo('/ctrl/file/cutFile/<%=file.getId()%>');" title="<%=$SH("_cut")%>"> </a>
                            <a class="icon fa fa-copy" href="" onclick="return linkTo('/ctrl/file/copyFile/<%=file.getId()%>');" title="<%=$SH("_copy")%>"> </a>
                            <a class="icon fa fa-trash-o" href="" onclick="if (confirmDelete()) return linkTo('/ctrl/file/deleteFile/<%=file.getId()%>');" title="<%=$SH("_delete")%>"> </a>
                        </div>
                    </div>
                </li>
                <%}%>
            </ul>
        </li>


