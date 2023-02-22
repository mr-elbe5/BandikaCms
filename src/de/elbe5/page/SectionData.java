/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.request.RequestData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SectionData {

    public static final String KEY_SECTION = "sectionData";

    protected String name = "";
    protected int contentId = 0;
    protected String cssClass = "";
    protected List<PagePartData> parts = new ArrayList<>();

    public SectionData() {
    }

    public void copyData(SectionData data) {
        setName(data.getName());
        for (PagePartData srcPart : data.parts) {
            PagePartData part = PagePartFactory.getNewData(srcPart.getClass().getSimpleName());
            if (part == null)
                continue;
            part.setSectionName(getName());
            part.copyData(srcPart);
            parts.add(part);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSectionId(){
        return "section_"+getName();
    }

    public int getContentId() {
        return contentId;
    }

    public void setPageId(int contentId) {
        this.contentId = contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public List<PagePartData> getParts() {
        return parts;
    }

    public void sortParts() {
        Collections.sort(parts);
    }

    public PagePartData getPart(int pid) {
        for (PagePartData pdata : parts) {
            if (pdata.getId() == pid) {
                return pdata;
            }
        }
        return null;
    }

    public<T extends PagePartData> T getPart(int pid, Class<T> cls) {
        try{
            return cls.cast(getPart(pid));
        }
        catch (NullPointerException | ClassCastException e){
            //ignore
        }
        return null;
    }

    public void addPart(PagePartData part, int fromPartId, boolean setRanking) {
        boolean found = false;
        if (fromPartId != -1) {
            for (int i = 0; i < parts.size(); i++) {
                PagePartData ppd = parts.get(i);
                if (ppd.getId() == fromPartId) {
                    parts.add(i + 1, part);
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            parts.add(part);
        }
        if (setRanking) {
            for (int i = 0; i < parts.size(); i++) {
                parts.get(i).setPosition(i + 1);
            }
        }
    }

    public void movePart(int id, int dir) {
        for (int i = 0; i < parts.size(); i++) {
            PagePartData ppd = parts.get(i);
            if (ppd.getId() == id) {
                parts.remove(i);
                int idx = i + dir;
                if (idx > parts.size() - 1) {
                    parts.add(ppd);
                } else if (idx < 0) {
                    parts.add(0, ppd);
                } else {
                    parts.add(idx, ppd);
                }
                break;
            }
        }
        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).setPosition(i + 1);
        }
    }

    public void deletePart(int id) {
        for (int i = 0; i < parts.size(); i++) {
            PagePartData ppd = parts.get(i);
            if (ppd.getId() == id) {
                parts.remove(i);
                return;
            }
        }
    }

    public void prepareCopy() {
        for (PagePartData part : parts) {
            part.prepareCopy();
        }
    }

    public void readFrontendRequestData(RequestData rdata) {
        for (int i=getParts().size()-1;i>=0;i--){
            PagePartData part = getParts().get(i);
            part.readFrontendRequestData(rdata);
            if (part.getPosition()==-1) {
                getParts().remove(i);
            }
        }
    }

}
