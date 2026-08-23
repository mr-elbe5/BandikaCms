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
<%@ page import="de.elbe5.content.ContentCache" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.request.ContentRequestKeys" %>
<%@ page import="de.elbe5.request.RequestKeys" %>
<%@ page import="de.elbe5.response.IMasterInclude" %>
<%@ page import="de.elbe5.base.LocalizedSystemStrings" %>
<%@ page import="de.elbe5.application.Configuration" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    IMasterInclude masterInclude = rdata.getRequestObject(RequestKeys.KEY_MASTERINCLUDE, IMasterInclude.class);
    ContentData contentData = rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, ContentData.class);
    List<Integer> parentIds = ContentCache.getParentContentIds(contentData);
    String title = rdata.getAttributes().getString(RequestKeys.KEY_TITLE, Configuration.getAppTitle()) + (contentData != null ? " | " + contentData.getDisplayName() : "");
    String keywords = contentData != null ? contentData.getKeywords() : title;
    String description = contentData != null ? contentData.getDescription() : "";
%>
<!DOCTYPE html>
<html lang="<%=Configuration.getLocale().getLanguage()%>">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"/>
    <title><%=$H(title)%>
    </title>
    <meta name="keywords" content="<%=$H(keywords)%>">
    <meta name="description" content="<%=$H(description)%>">
    <link rel="shortcut icon" href="/favicon.ico"/>
    <link rel="stylesheet" href="/static-content/css/bootstrap.css?v=1"/>
    <link rel="stylesheet" href="/static-content/css/fontmplus1p.css?v=1"/>
    <link rel="stylesheet" href="/static-content/css/bandika.css?v=1"/>
    <link rel="stylesheet" href="/static-content/css/layout.css?v=1"/>
    <script type="text/javascript" src="/static-content/js/jquery-1.12.4.min.js?v=1"></script>
    <script type="text/javascript" src="/static-content/js/bootstrap.bundle.min.js?v=1"></script>
    <script type="text/javascript" src="/static-content/js/bootstrap.tree.js?v=1"></script>
    <script type="text/javascript" src="/static-content/ckeditor/ckeditor.js?v=1"></script>
    <script>
        CKEDITOR.config.language = '<%=Configuration.getLocale().getLanguage()%>';
    </script>
    <script type="text/javascript" src="/static-content/ckeditor/adapters/jquery.js?v=1"></script>
    <script type="text/javascript" src="/static-content/js/bandika-webbase.js?v=1"></script>
</head>
<body>
<div class="container">
    <header>
        <div class="header">
            <div class="logo-area">
                <a href="/"><img class="logo" src="/static-content/img/logo.png" alt="Bandika"/></a>
            </div>
            <div class = "nav-area">
                <div class="sysnav">
                    <jsp:include page="/WEB-INF/_jsp/_include/_sysnav.inc.jsp" flush="true"/>
                </div>
                <div class="menu-area">
                    <jsp:include page="/WEB-INF/_jsp/_include/_mainnav.inc.jsp" flush="true"/>
                </div>
            </div>
        </div>

        <div class="row">
            <section class="col-12 breadcrumb">
                <ol class="nav">
                    <%
                        for (int i = parentIds.size() - 1; i >= 0; i--) {
                            ContentData content = ContentCache.getContent(parentIds.get(i));
                            if (content != null) {
                    %>
                    <li class="breadcrumb-item">
                        <a href="<%=content.getUrl()%>"><%=$H(content.getDisplayName())%>
                        </a>
                    </li>
                    <%
                            }
                        }
                    %>
                </ol>
            </section>
        </div>
    </header>

    <main id="main" role="main">
        <div id="pageContainer" class="container">
        <% if (masterInclude != null) {
            try {
                masterInclude.displayContent(pageContext, rdata);
            } catch (Exception ignore) {
            }
        }%>
        </div>
    </main>

    <footer>
        <ul class="nav">
            <li class="nav-item">
                <a class="nav-link">&copy; <%=LocalizedSystemStrings.getInstance().html("copyright")%>
                </a>
            </li>
            <% for (ContentData data : ContentCache.getFooterList()) {
                if (data.isActive() && data.hasUserReadRight(rdata.getLoginUser())) {%>
            <li class="nav-item">
                <a class="nav-link" href="<%=data.getUrl()%>"><%=$H(data.getDisplayName())%>
                </a>
            </li>
            <%
                    }
                }
            %>
        </ul>
    </footer>

</div>
<div class="modal" id="modalDialog" tabindex="-1" role="dialog"></div>
</body>
</html>
