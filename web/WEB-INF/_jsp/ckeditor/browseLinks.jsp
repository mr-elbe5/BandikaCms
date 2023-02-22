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
<%@ page import="de.elbe5.content.ContentCache" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="de.elbe5.request.ContentRequestKeys" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    int callbackNum = rdata.getAttributes().getInt("CKEditorFuncNum", -1);
%>
<div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <h5 class="modal-title"><%=$SH("_selectLink")%>
            </h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
        <div class="modal-body">
            <form:message/>
            <ul class="nav nav-tabs" id="selectTab" role="tablist">
                <li class="nav-item">
                    <a class="nav-link active" id="pages-tab" data-toggle="tab" href="#pages" role="tab" aria-controls="pages" aria-selected="true"><%=$SH("_pages")%>
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" id="documents-tab" data-toggle="tab" href="#documents" role="tab" aria-controls="documents" aria-selected="false"><%=$SH("_documents")%>
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" id="images-tab" data-toggle="tab" href="#images" role="tab" aria-controls="images" aria-selected="false"><%=$SH("_images")%>
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" id="media-tab" data-toggle="tab" href="#media" role="tab" aria-controls="media" aria-selected="false"><%=$SH("_media")%>
                    </a>
                </li>
            </ul>

            <div class="tab-content" id="pageTabContent">
                <div class="tab-pane fade show active" id="pages" role="tabpanel" aria-labelledby="pages-tab">
                    <section class="treeSection">
                        <ul class="tree filetree">
                            <%rdata.setRequestObject("treePage", ContentCache.getContentRoot());%>
                            <jsp:include page="/WEB-INF/_jsp/ckeditor/pageLinkBrowserFolder.inc.jsp" flush="true"/>
                            <%rdata.removeRequestObject("treePage");%>
                        </ul>
                    </section>
                </div>
                <div class="tab-pane fade" id="documents" role="tabpanel" aria-labelledby="documents-tab">
                    <section class="treeSection">
                        <% if (rdata.hasAnyContentRight()) { %>
                        <ul class="tree filetree">
                            <%rdata.setRequestObject("treePage", ContentCache.getContentRoot());%>
                            <jsp:include page="/WEB-INF/_jsp/ckeditor/documentLinkBrowserFolder.inc.jsp" flush="true"/>
                        </ul>
                        <%rdata.removeRequestObject("treePage"); }%>
                    </section>
                </div>
                <div class="tab-pane fade" id="images" role="tabpanel" aria-labelledby="images-tab">
                    <section class="treeSection">
                        <% if (rdata.hasAnyContentRight()) { %>
                        <ul class="tree filetree">
                            <%rdata.setRequestObject("treePage", ContentCache.getContentRoot());%>
                            <jsp:include page="/WEB-INF/_jsp/ckeditor/imageLinkBrowserFolder.inc.jsp" flush="true"/>
                        </ul>
                        <%rdata.removeRequestObject("treePage"); }%>
                    </section>
                </div>
                <div class="tab-pane fade" id="media" role="tabpanel" aria-labelledby="media-tab">
                    <section class="treeSection">
                        <% if (rdata.hasAnyContentRight()) { %>
                        <ul class="tree filetree">
                            <%rdata.setRequestObject("treePage", ContentCache.getContentRoot());%>
                            <jsp:include page="/WEB-INF/_jsp/ckeditor/mediaLinkBrowserFolder.inc.jsp" flush="true"/>
                        </ul>
                        <%rdata.removeRequestObject("treePage");
                        }%>
                    </section>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-outline-secondary" data-dismiss="modal"><%=$SH("_cancel")%>
            </button>
        </div>
    </div>
    <script type="text/javascript">
        $('.tree').treed('fa fa-minus-square-o', 'fa fa-plus-square-o');
        function ckLinkCallback(url) {
            if (CKEDITOR)
                CKEDITOR.tools.callFunction(<%=callbackNum%>, url);
            return closeModalDialog();
        }
    </script>
</div>
