/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.application;

import de.elbe5.administration.AdminController;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.base.LocalizedSystemStrings;
import de.elbe5.base.Log;
import de.elbe5.ckeditor.CkEditorController;
import de.elbe5.database.DbConnector;
import de.elbe5.layout.LocalizedLayoutNames;
import de.elbe5.file.*;
import de.elbe5.layout.LayoutCache;
import de.elbe5.page.*;
import de.elbe5.servlet.InitServlet;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserController;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public class BandikaCmsInitServlet extends InitServlet {

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        System.out.println("initializing Bandika Application...");
        ServletContext context=servletConfig.getServletContext();
        Configuration.initialize(context);
        ApplicationPath.initializePath(ApplicationPath.getCatalinaAppDir(context), ApplicationPath.getCatalinaAppROOTDir(context));
        Log.initLog(ApplicationPath.getAppName());
        if (!DbConnector.getInstance().initialize())
            return;
        LocalizedStrings.getInstance().addBundle("bandika", Configuration.getLocale());
        LocalizedSystemStrings.getInstance().addBundle("systemStrings", Configuration.getLocale());
        LocalizedLayoutNames.getInstance().addBundle("layoutNames", Configuration.getLocale());
        AdminController.register(new AdminController());
        PageController.register(new PageController());
        DocumentController.register(new DocumentController());
        ImageController.register(new ImageController());
        MediaController.register(new MediaController());
        CkEditorController.register(new CkEditorController());
        UserController.register(new UserController());
        LayoutCache.addType(PagePartData.LAYOUT_TYPE);

        PageCache.load();
        UserCache.load();
        LayoutCache.load();
        if (!FileBean.getInstance().assertFileDirectory()){
            Log.error("could not create file directory");
        }
        Log.log("Bandika initialized");

        /*try {
            String salt = PBKDF2Encryption.generateSaltBase64();
            Log.info(salt);
            String pwd = PBKDF2Encryption.getEncryptedPasswordBase64("pass", salt);
            Log.info(pwd);
        }
        catch (Exception ignore){
        }*/
    }

}
