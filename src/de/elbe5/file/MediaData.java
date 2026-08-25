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

public class MediaData extends FileData {

    public MediaData() {
    }

    public String getControllerKey() {
        return MediaController.KEY;
    }

    public String getIconStyle(){
        return "fa-film";
    }

    public String getEditURL(){
        return "/WEB-INF/_jsp/file/editMedia.ajax.jsp";
    }

    // multiple data

    @Override
    public void readRequestData(RequestData rdata) {
        super.readRequestData(rdata);
        if (!isNew()){
            return;
        }
        BinaryFile file = rdata.getAttributes().getFile("file");
        createFromBinaryFile(file);
        if (getDisplayName().isEmpty()) {
            setDisplayName(file.getFileNameWithoutExtension());
        }
        else{
            adjustFileNameToDisplayName();
        }
    }

}
