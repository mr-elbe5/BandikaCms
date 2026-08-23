/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.servlet;

public class ResponseException extends RuntimeException{

    private final int responseCode;

    public ResponseException(int responseCode){
        this.responseCode = responseCode;
    }

    public ResponseException(int responseCode, String message){
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
