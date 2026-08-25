<%--
  Bandika CMS - A Java based modular Content Management System
  Copyright (C) 2009-2021 Michael Roennau

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
--%>
<%@ page import="de.elbe5.base.StringHelper" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="de.elbe5.base.LocalizedStrings" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.LocalTime" %>
<%@ page import="de.elbe5.base.DateHelper" %>
<%!
    public String $D(LocalDate date){
        return DateHelper.toHtmlDate(date);
    }

    public String $T(LocalTime time){
        return DateHelper.toHtmlTime(time);
    }

    public String $DT(LocalDateTime dateTime){
        return DateHelper.toHtmlDateTime(dateTime);
    }

    public String $H(LocalDateTime dateTime){
        return DateHelper.toHtml(dateTime);
    }

    public String $H(String src){
        return StringHelper.toHtml(src);
    }

    public String $JS(String src){
        return StringHelper.toJs(src);
    }

    public String $HML(String src){
        return StringHelper.toHtmlMultiline(src);
    }

    public String $SH(String key){
        return LocalizedStrings.getInstance().html(key);
    }

    public String $SHM(String key){
        return LocalizedStrings.getInstance().htmlMultiline(key);
    }

    public String $SJ(String key){
        return LocalizedStrings.getInstance().js(key);
    }

    public String $I(int i){
        return Integer.toString(i);
    }
%>
