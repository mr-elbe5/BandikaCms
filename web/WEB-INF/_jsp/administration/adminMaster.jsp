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
<%@ page import="de.elbe5.request.RequestKeys" %>
<%@ page import="de.elbe5.rights.GlobalRight" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.base.LocalizedSystemStrings" %>
<%@ page import="de.elbe5.application.Configuration" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    String title = rdata.getAttributes().getString(RequestKeys.KEY_TITLE);
    String includeUrl = rdata.getAttributes().getString(RequestKeys.KEY_JSP);
%>
<!DOCTYPE html>
<html lang="<%=Configuration.getLocale().getLanguage()%>">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"/>
    <title><%=title%></title>
    <link rel="shortcut icon" href="/favicon.ico"/>
    <link rel="stylesheet" href="/static-content/css/bootstrap.css"/>
    <link rel="stylesheet" href="/static-content/css/bandika.css"/>
    <link rel="stylesheet" href="/static-content/css/layout.css"/>
    <script type="text/javascript" src="/static-content/js/jquery-1.12.4.min.js"></script>
    <script type="text/javascript" src="/static-content/js/bootstrap.bundle.min.js"></script>
    <script type="text/javascript" src="/static-content/js/bootstrap.tree.js"></script>
    <script type="text/javascript" src="/static-content/js/bootstrap-datepicker.js"></script>
    <script type="text/javascript" src="/static-content/js/locales/bootstrap-datepicker.de.js"></script>
    <script type="text/javascript" src="/static-content/js/bandika-webbase.js"></script>
</head>

<body class="admin">
<div class="container">
    <header>
        <div class="header">
            <div class="logo-area">
                <a href="/"><img class="logo" src="/static-content/img/logo.png" alt="Bandika"/></a>
            </div>
            <div class = "nav-area">
                <div class="sysnav">
                    <ul class="nav justify-content-end">
                        <li class="nav-item"><a class="nav-link fa fa-home" href="/" title="<%=$SH("_home")%>"></a></li>
                    </ul>
                </div>
                <div class="menu-area">
                    <nav class="navbar navbar-expand-lg navbar-light">
                        <div class="collapse navbar-collapse" id="navbarSupportedContent">
                            <ul class="navbar-nav mr-auto">
                                <% if (GlobalRight.hasGlobalApplicationEditRight(rdata.getLoginUser())){%>
                                <li class="nav-item">
                                    <a class="nav-link"
                                       href="/ctrl/admin/openSystemAdministration"><%=$SH("_systemAdministration")%>
                                    </a>
                                </li>
                                <%}%>
                                <% if (GlobalRight.hasGlobalUserEditRight(rdata.getLoginUser())){%>
                                <li class="nav-item">
                                    <a class="nav-link"
                                       href="/ctrl/admin/openPersonAdministration"><%=$SH("_personAdministration")%>
                                    </a>
                                </li>
                                <%}%>
                                <% if (GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser())){%>
                                <li class="nav-item">
                                    <a class="nav-link" href="/ctrl/admin/openContentAdministration?contentId=<%=ContentData.ID_ROOT%>"><%=$SH("_contentAdministration")%>
                                    </a>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link" href="/ctrl/admin/openContentLog"><%=$SH("_contentLog")%>
                                    </a>
                                </li>
                                <%}%>
                            </ul>
                        </div>
                    </nav>
                </div>
            </div>
        </div>
        <div class="bc row">
            <section class="col-12">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="/"><%=$SH("_home")%>
                    </a></li>
                    <li class="breadcrumb-item"><a><%=$H(title)%>
                    </a></li>
                </ol>
            </section>
        </div>
    </header>
    <main id="main" role="main">
        <div id="pageContainer">
            <jsp:include page="<%=includeUrl%>" flush="true"/>
        </div>
    </main>
</div>
<div class="container fixed-bottom">
    <footer>
        <div><%=LocalizedSystemStrings.getInstance().html("copyright")%>
        </div>
    </footer>
</div>
<div class="modal" id="modalDialog" tabindex="-1" role="dialog">
</div>
<script type="text/javascript">
    function confirmDelete() {
        return confirm('<%=$SJ("_confirmDelete")%>');
    }

    function confirmExecute() {
        return confirm('<%=$SJ("_confirmExecute")%>');
    }
</script>

</body>
</html>

