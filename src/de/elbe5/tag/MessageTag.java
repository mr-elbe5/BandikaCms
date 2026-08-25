/*
  Bandika CMS - A Java based modular Content Management System
  Copyright (C) 2009-2015 Michael Roennau

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.tag;

import de.elbe5.base.Log;
import de.elbe5.base.StringFormatter;
import de.elbe5.base.StringHelper;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;

import jakarta.servlet.jsp.JspException;
import java.io.Writer;

public class MessageTag extends BaseTag {

    String controlHtml = "<div class=\"alert alert-{1} alert-dismissible fade show\" role=\"alert\"> {2}\n  <button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\">\n    <span aria-hidden=\"true\">&times;</span>\n  </button>\n</div>";

    @Override
    public int doStartTag() throws JspException {
        try {
            RequestData rdata = getRequestData();
            if (rdata.hasMessage()) {
                String msg = rdata.getAttributes().getString(RequestKeys.KEY_MESSAGE);
                String msgType = rdata.getAttributes().getString(RequestKeys.KEY_MESSAGETYPE);
                Writer writer = getWriter();
                writer.write(StringFormatter.format(controlHtml, msgType, StringHelper.toHtml(msg)));
            }
        } catch (Exception e) {
            Log.error("error writing message tag", e);
            throw new JspException(e);
        }
        return SKIP_BODY;
    }

}
