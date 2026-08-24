/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import java.util.Comparator;
import java.util.List;

public class PageComparator implements Comparator<PageData> {

    public static PageComparator instance = new PageComparator();

    public void sort(List<PageData> projects){
        projects.sort(this);
    }

    @Override
    public int compare(PageData o1, PageData o2) {
        return o1.getDisplayName().toLowerCase().compareTo(o2.getDisplayName().toLowerCase());
    }

}
