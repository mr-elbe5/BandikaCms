<%@ page import="de.elbe5.application.Configuration" %><%--
  Bandika CMS - A Java based modular Content Management System
  Copyright (C) 2009-2021 Michael Roennau

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
--%>
<!DOCTYPE html>
<%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    String title = Configuration.getAppTitle();
%>
<html lang="<%=Configuration.getLocale().getLanguage()%>">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"/>
    <title><%=title%>
    </title>
    <link rel="shortcut icon" href="/favicon.ico"/>
    <link rel="stylesheet" href="/static-content/css/bootstrap.css"/>
    <link rel="stylesheet" href="/static-content/css/bandika.css"/>
    <link rel="stylesheet" href="/static-content/css/layout.css"/>
    <script type="text/javascript" src="/static-content/js/jquery-1.12.4.min.js"></script>
    <script type="text/javascript" src="/static-content/js/bootstrap.bundle.min.js"></script>

</head>
<body class="login">
<main id="main" role="main">
    <div class="container">
        <form:message/>
        <section class="mainSection loginSection text-center">
            <form class="form" action="/ctrl/user/login" method="post" name="loginForm" accept-charset="UTF-8">
                <img class="mb-4" src="/static-content/img/logo.png" alt="<%=Configuration.getAppTitle()%>">
                <label for="login" class="sr-only"><%=$SH("_loginName")%>
                </label>
                <input type="text" id="login" name="login" class="form-control"
                        placeholder="<%=$SH("_loginName")%>" required autofocus>
                <label for="password" class="sr-only"><%=$SH("_password")%>
                </label>
                <input type="password" id="password" name="password" class="form-control"
                        placeholder="<%=$SH("_password")%>" required>
                <button class="btn btn-outline-primary" type="submit"><%=$SH("_login")%>
                </button>
                <button class="btn btn-outline-secondary"
                        onclick="$(location).attr('href','/');"><%=$SH("_cancel")%>
                </button>
            </form>
        </section>
    </div>
</main>
<footer>
</footer>
</body>
</html>
