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
<%@ page import="de.elbe5.request.ContentRequestKeys" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    PageData contentData = rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, PageData.class);
%>
    <form action="/ctrl/page/saveContentFrontend/<%=contentData.getId()%>" method="post" id="pageform" name="pageform" accept-charset="UTF-8">
        <div class="btn-group btn-group-sm pageEditButtons">
            <button type="submit" class="btn btn-sm btn-success" onclick="updateEditors();"><%=$SH("_savePage")%></button>
            <button class="btn btn-sm btn-secondary" onclick="return linkTo('/ctrl/page/cancelEditContentFrontend/<%=contentData.getId()%>');"><%=$SH("_cancel")%></button>
        </div>
        <jsp:include page="<%=contentData.getLayoutUrl()%>" flush="true" />
    </form>
    <script type="text/javascript">

        function confirmDelete() {
            return confirm('<%=$SJ("_confirmDelete")%>');
        }

        function movePart(id,direction){
            let $partWrapper=$('#part_'+id);
            if (direction===1){
                let $nextPart=$partWrapper.next();
                if (!$nextPart || $nextPart.length===0){
                    return false;
                }
                $partWrapper.detach();
                $nextPart.after($partWrapper);
            }
            else{
                let $prevPart=$partWrapper.prev();
                if (!$prevPart || $prevPart.length===0){
                    return false;
                }
                $partWrapper.detach();
                $prevPart.before($partWrapper);
            }
            updatePartPositions();
            return false;
        }

        function deletePart(id){
            let $partWrapper=$('#part_'+id);
            $partWrapper.remove();
            updatePartPositions();
            return false;
        }

        function addPart(fromId, section, type, layout){
            let data = {
                fromPartId: fromId,
                sectionName: section,
                partType: type,
                layout: layout
            };
            $.ajax({
                url: '/ctrl/page/addPart/'+<%=contentData.getId()%>,
                type: 'POST',
                data: data,
                dataType: 'html'
            }).success(function (html, textStatus) {
                if (fromId === -1) {
                    let $section=$('#pageform').find('#section_'+section);
                    $section.append(html);
                }
                else{
                    let $fromPartWrapper = $('#part_' + fromId);
                    if ($fromPartWrapper) {
                        $fromPartWrapper.after(html);
                    }
                }
                updatePartPositions();
            });
            return false;
        }

        function updateEditors(){
            if (CKEDITOR) {
                $(".ckeditField").each(function () {
                    let id = $(this).attr('id');
                    $('input[name="' + id + '"]').val(CKEDITOR.instances[id].getData());
                });
            }
        }

        function updatePartEditors($part){
            if (CKEDITOR) {
                $(".ckeditField",$part).each(function () {
                    let id = $(this).attr('id');
                    $('input[name="' + id + '"]').val(CKEDITOR.instances[id].getData());
                });
            }
        }

        function updatePartPositions(){
            let $sections=$('#pageform').find('.section');
            $sections.each(function(){
                updateSectionPartPositions($(this));
            });
        }

        function updateSectionPartPositions($section){
            let $inputs = $section.find('input.partPos');
            $inputs.each(function (index) {
                $(this).attr('value', index);
            });
        }

        updatePartPositions();

    </script>





