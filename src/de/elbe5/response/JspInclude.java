/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.response;

import de.elbe5.request.RequestData;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import java.io.IOException;

public class JspInclude implements IMasterInclude {

    private final String jsp;

    public JspInclude(String jsp) {
        this.jsp=jsp;
    }

    @Override
    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        JspWriter writer = context.getOut();
        writer.write("<div id=\"pageContent\" class=\"viewArea\">");
        context.include(jsp);
        writer.write("</div>");
    }

    @Override
    public void appendContent(StringBuilder sb, RequestData rdata) {
    }
}
