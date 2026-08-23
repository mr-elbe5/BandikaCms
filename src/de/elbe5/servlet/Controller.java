package de.elbe5.servlet;

import de.elbe5.base.LocalizedStrings;
import de.elbe5.request.*;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;

import jakarta.servlet.http.HttpServletResponse;

public abstract class Controller {

    public abstract String getKey();

    protected IResponse showHome() {
        return new ForwardResponse("/");
    }

    protected void assertRights(boolean hasRights){
        if (!hasRights)
            throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
    }

    protected void setSaveError(RequestData rdata) {
        rdata.setMessage($S("_saveError"), RequestKeys.MESSAGE_TYPE_ERROR);
    }

    protected IResponse openAdminPage(RequestData rdata, String jsp, String title) {
        rdata.getAttributes().put(RequestKeys.KEY_JSP, jsp);
        rdata.getAttributes().put(RequestKeys.KEY_TITLE, title);
        return new ForwardResponse("/WEB-INF/_jsp/administration/adminMaster.jsp");
    }

    protected IResponse showSystemAdministration(RequestData rdata) {
        return openAdminPage(rdata, "/WEB-INF/_jsp/administration/systemAdministration.jsp", $S("_systemAdministration"));
    }

    protected IResponse showPersonAdministration(RequestData rdata) {
        return openAdminPage(rdata, "/WEB-INF/_jsp/administration/personAdministration.jsp", $S("_personAdministration"));
    }

    protected void assertSessionCall(RequestData rdata){
        if (rdata.getContext()!=RequestContext.session && rdata.getContext()!=RequestContext.content){
            throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    protected void assertLoggedInSessionCall(RequestData rdata){
        if (!rdata.isLoggedIn() || (rdata.getContext()!=RequestContext.session && rdata.getContext()!=RequestContext.content)){
            throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    protected void assertApiCall(RequestData rdata){
        if (rdata.getContext()!=RequestContext.api){
            throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    protected void assertLoggedInApiCall(RequestData rdata){
        if (!rdata.isLoggedIn() || rdata.getContext()!=RequestContext.api){
            throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    protected void assertLoggedIn(RequestData rdata){
        if (!rdata.isLoggedIn())
            throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
    }

    protected String $S(String key){
        return LocalizedStrings.getInstance().string(key);
    }

    protected String $SH(String key){
        return LocalizedStrings.getInstance().html(key);
    }

    public String $SHM(String key){
        return LocalizedStrings.getInstance().htmlMultiline(key);
    }

}
