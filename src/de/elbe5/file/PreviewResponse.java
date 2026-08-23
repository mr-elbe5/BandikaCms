/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import de.elbe5.base.BinaryFile;
import de.elbe5.request.RequestData;
import de.elbe5.response.IResponse;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class PreviewResponse implements IResponse {

    private final int id;

    public PreviewResponse(int id) {
        this.id = id;
    }

    @Override
    public void processResponse(ServletContext context, RequestData rdata, HttpServletResponse response) {
        process(context,rdata,response);
    }

    protected void process(ServletContext context, RequestData rdata, HttpServletResponse response) {
        BinaryFile file= PreviewCache.get(id);
        if (file==null){
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        response.setContentType("image/jpeg");
        try {
            OutputStream out = response.getOutputStream();
            if (file.getBytes() == null) {
                response.setHeader("Content-Length", "0");
            } else {
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Pragma", "no-cache");
                String sb = "filename=\"" + file.getFileName() + '"';
                response.setHeader("Content-Disposition", sb);
                response.setHeader("Content-Length", Integer.toString(file.getBytes().length));
                out.write(file.getBytes());
            }
            out.flush();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}
