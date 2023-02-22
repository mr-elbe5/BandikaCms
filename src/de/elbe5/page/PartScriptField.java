/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.request.RequestData;

public class PartScriptField extends PartField {

    public static final String FIELDTYPE = "script";

    @Override
    public String getFieldType() {
        return FIELDTYPE;
    }

    protected String code = "";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /******************* HTML part *********************************/

    @Override
    public void readRequestData(RequestData rdata) {
        setCode(rdata.getAttributes().getString(getIdentifier()));
    }

    @Override
    public void readFrontendRequestData(RequestData rdata){
        setCode(rdata.getAttributes().getString(getIdentifier()));
    }


}
