/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.servlet;

import de.elbe5.base.StringHelper;
import de.elbe5.application.Configuration;
import de.elbe5.request.RequestContext;
import de.elbe5.request.RequestData;
import de.elbe5.response.IResponse;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 100, maxFileSize = 1024 * 1024 * 200, maxRequestSize = 1024 * 1024 * 200 * 5)
public class ControllerServlet extends WebServlet {

    protected void processRequest(String method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(Configuration.ENCODING);
        String uri = request.getRequestURI();
        RequestContext requestType = RequestContext.any;
        if (uri.startsWith("/ctrl/")){
            requestType = RequestContext.session;
            uri = uri.substring(6);
        }
        else if (uri.startsWith("/api/")){
            requestType = RequestContext.api;
            uri = uri.substring(5);
        }
        RequestData rdata = new RequestData(method, requestType, request);
        StringTokenizer stk = new StringTokenizer(uri, "/", false);
        String methodName = "";
        Controller controller = null;
        if (stk.hasMoreTokens()) {
            String controllerName = stk.nextToken();
            if (stk.hasMoreTokens()) {
                methodName = stk.nextToken();
                if (stk.hasMoreTokens()) {
                    rdata.setId(StringHelper.toInt(stk.nextToken()));
                }
            }
            controller = ControllerCache.getController(controllerName);
        }
        rdata.init();
        try {
            IResponse result = getResponse(controller, methodName, rdata);
            result.processResponse(getServletContext(), rdata, response);
        } catch (ResponseException ce) {
            handleException(request, response, ce.getResponseCode());
        } catch (Exception | AssertionError e) {
            handleException(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public IResponse getResponse(Controller controller, String methodName, RequestData rdata) {
        if (controller==null)
            throw new ResponseException(HttpServletResponse.SC_BAD_REQUEST);
        try {
            Method controllerMethod = controller.getClass().getMethod(methodName, RequestData.class);
            Object result = controllerMethod.invoke(controller, rdata);
            if (result instanceof IResponse)
                return (IResponse) result;
            throw new ResponseException(HttpServletResponse.SC_BAD_REQUEST);
        } catch (NoSuchMethodException | InvocationTargetException e){
            throw new ResponseException(HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (IllegalAccessException e) {
            throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
