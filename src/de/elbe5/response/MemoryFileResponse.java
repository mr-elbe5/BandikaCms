/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.response;

import de.elbe5.base.BinaryFile;
import de.elbe5.request.RequestData;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class MemoryFileResponse implements IResponse {

    private final BinaryFile file;
    private boolean forceDownload=false;
    private boolean noCache=true;

    public MemoryFileResponse(BinaryFile file) {
        this.file = file;
    }

    public void setNoCache(boolean noCache) {
        this.noCache = noCache;
    }

    public void setForceDownload(boolean forceDownload) {
        this.forceDownload = forceDownload;
    }

    @Override
    public void processResponse(ServletContext context, RequestData rdata, HttpServletResponse response)  {
        process(context, rdata,response);
    }

    public void process(ServletContext context, RequestData rdata, HttpServletResponse response)  {
        if (file==null){
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isEmpty()) {
            contentType = "*/*";
        }
        response.setContentType(contentType);
        try {
            OutputStream out = response.getOutputStream();
            if (file.getBytes() == null) {
                response.setHeader("Content-Length", "0");
            } else {
                if (noCache) {
                    response.setHeader("Expires", "Tues, 01 Jan 1980 00:00:00 GMT");
                    response.setHeader("Cache-Control", "no-cache");
                    response.setHeader("Pragma", "no-cache");
                }
                StringBuilder sb = new StringBuilder();
                if (forceDownload) {
                    sb.append("attachment;");
                }
                sb.append("filename=\"");
                sb.append(file.getFileName());
                sb.append('"');
                response.setHeader("Content-Disposition", sb.toString());
                response.setHeader("Content-Length", Integer.toString(file.getBytes().length));
                out.write(file.getBytes());
            }
            out.flush();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}
