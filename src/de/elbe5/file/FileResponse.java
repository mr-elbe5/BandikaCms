/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.

 This file is based on net/balusc/webapp/FileServlet.java of BalusC, Copyright (C) 2009 BalusC, but modernized and adds creating files (as a cache) from the database
 */
package de.elbe5.file;

import de.elbe5.base.StringHelper;
import de.elbe5.request.RequestData;
import de.elbe5.response.IResponse;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;

public class FileResponse implements IResponse {

    private static final int DEFAULT_BUFFER_SIZE = 0x4000;

    private final File file;
    private final String fileName;
    private final RangeInfo rangeInfo;

    public FileResponse(File file, String fileName, RangeInfo rangeInfo){
        this.file = file;
        this.fileName = fileName;
        this.rangeInfo = rangeInfo;
    }

    public void processResponse(ServletContext context, RequestData rdata, HttpServletResponse response) {
        if (rangeInfo!=null && !rangeInfo.isValid()){
            response.setHeader("Content-Range", "bytes */" + file.length());
            response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        boolean forceDownload = rdata.getAttributes().getBoolean("download");
        String disposition = (forceDownload ? "attachment" : "inline") + ";filename=\"" + StringHelper.toSafeWebFileName(fileName) + "\"";
        response.setHeader("Content-Disposition", disposition);
        response.setHeader("Accept-Ranges", "bytes");
        String contentType = context.getMimeType(file.getName());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        if (contentType.startsWith("text")) {
            contentType += ";charset=UTF-8";
        }
        response.setContentType(contentType);

        RandomAccessFile rafile = null;
        ServletOutputStream output = null;

        try {
            rafile = new RandomAccessFile(file, "r");
            output = response.getOutputStream();
            if (rangeInfo == null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("Content-Length", String.valueOf(rafile.length()));
                copyFromFileToOutput(rafile, output, 0, rafile.length());
            } else {
                response.setHeader("Content-Range", "bytes " + rangeInfo.getStart() + "-" + rangeInfo.getEnd() + "/" + rangeInfo.getFileSize());
                response.setHeader("Content-Length", String.valueOf(rangeInfo.getLength()));
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                copyFromFileToOutput(rafile, output, rangeInfo.getStart(),rangeInfo.getLength());
            }
            output.flush();
        } catch (IOException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (output!=null) try {output.close();} catch (IOException ignore) { }
            if (rafile!=null) try {rafile.close();} catch (IOException ignore) { }
        }
    }

    private static void copyFromFileToOutput(RandomAccessFile file, OutputStream output, long start, long length) throws IOException
    {
        //Log.log("copy from " + start + " to " + (start+length-1));
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        if (start!=0){
            file.seek(start);
        }
        long toRead = length;
        while (toRead>0){
            if (toRead >= DEFAULT_BUFFER_SIZE) {
                read = file.read(buffer);
            }
            else{
                read = file.read(buffer,0, (int)toRead);
            }
            output.write(buffer, 0, read);
            toRead -= read;
        }
    }

}