/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.

 This file is based on net/balusc/webapp/FileServlet.java of BalusC, Copyright (C) 2009 BalusC, but modernized and adds creating files (as a cache) from the database
 */
package de.elbe5.file;

public class RangeInfo {

    long start;
    long end;
    long length;
    long fileSize;

    boolean valid = true;

    public RangeInfo(String rangeHeader, long fileSize){
        if (!rangeHeader.matches("^bytes=\\d*-\\d*")) {
            valid = false;
            return;
        }
        String rangeString = rangeHeader.substring(6);
        int splitPos = rangeString.indexOf("-");
        String subString = rangeString.substring(0,splitPos);
        long start = subString.isEmpty() ? -1 : Long.parseLong(subString);
        subString = rangeString.substring(splitPos+1);
        long end = subString.isEmpty() ? -1 : Long.parseLong(subString);
        if (start == -1) {
            start = fileSize - end;
            end = fileSize - 1;
        } else if (end == -1 || end > fileSize - 1) {
            end = fileSize - 1;
        }
        if (start > end) {
            valid = false;
        }
        this.start = start;
        this.end = end;
        this.length = end - start + 1;
        this.fileSize = fileSize;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getLength() {
        return length;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isValid() {
        return valid;
    }
}