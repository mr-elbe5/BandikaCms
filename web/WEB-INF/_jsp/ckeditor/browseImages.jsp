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
<%@ page import="de.elbe5.content.ContentCache" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.request.ContentRequestKeys" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    ContentData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT,ContentData.class);
    List<Integer> parentIds=ContentCache.getParentContentIds(data.getId());
    parentIds.add(data.getId());
    rdata.setRequestObject("parentIds",parentIds);
    int callbackNum = rdata.getAttributes().getInt("CKEditorFuncNum", -1);
%>
<div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <h5 class="modal-title"><%=$SH("_selectImage")%>
            </h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
        <div class="modal-body">
            <form:message/>
            <section class="treeSection">
                <ul class="tree filetree">
                    <% rdata.setRequestObject("treePage", ContentCache.getContentRoot());%>
                    <jsp:include page="/WEB-INF/_jsp/ckeditor/imageBrowserFolder.inc.jsp" flush="true"/>
                    <% rdata.removeRequestObject("treePage");%>
                </ul>
            </section>
            <section class="addImage">
                <div><input type="file" name="file" id="addedFile"/>&nbsp;<button class="btn btn-sm btn-outline-primary" onclick="return addImage()"><%=$SH("_add")%></button></div>
            </section>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-outline-secondary" data-dismiss="modal"><%=$SH("_cancel")%>
            </button>
        </div>
    </div>
    <script type="text/javascript">
        $('.tree').treed('fa fa-minus-square-o', 'fa fa-plus-square-o');

        function ckImgCallback(url) {
            if (CKEDITOR)
                CKEDITOR.tools.callFunction(<%=callbackNum%>, url);
            return closeModalDialog();
        }

        function addImage() {
            let $fileInput = $('#addedFile');
            let file = $fileInput.prop('files')[0];
            if (!file)
                return false;
            let formData = new FormData();
            formData.append('file', file);
            $.ajax({
                url: '/ctrl/ckeditor/addImage/<%=data.getId()%>',
                type: 'POST',
                data: formData,
                cache: false,
                dataType: 'html',
                enctype: 'multipart/form-data',
                contentType: false,
                processData: false
            }).success(function (html) {
                $('#page_ul_<%=data.getId()%>').append(html);
            });
            return false;
        }
    </script>
</div>
<%
    rdata.removeRequestObject("parentIds");
%>
