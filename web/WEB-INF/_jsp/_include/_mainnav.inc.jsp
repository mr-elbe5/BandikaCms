<%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="de.elbe5.page.PageData" %>
<%@ page import="de.elbe5.page.PageCache" %>
<%@ page import="java.util.*" %>
<%@ page import="de.elbe5.request.RequestKeys" %>
<%@ page import="de.elbe5.link.LinkData" %>
<%@ page import="de.elbe5.page.PageData" %>
<%@ page import="de.elbe5.page.PageCache" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    PageData home = PageCache.getContentRoot();
    PageData currentContent = rdata.getCurrentDataInRequestOrSession(RequestKeys.KEY_PAGE, PageData.class);
    if (currentContent == null)
        currentContent = home;
    if (home != null) {
        Set<Integer> pathIds = new HashSet<>();
        currentContent.collectParentIds(pathIds);
        pathIds.add(currentContent.getId());%>
<% for (PageData contentData : home.getChildren(LinkData.class)) {
    if (contentData.isActive() && contentData.isInHeaderNav() && contentData.hasUserReadRight(rdata.getLoginUser())) {%>
<a class="<%=pathIds.contains(contentData.getId())? "active" : ""%>" href="<%=contentData.getUrl()%>"><%=contentData.getNavDisplay()%></a>&nbsp;&nbsp;
<%}
}%>
<a class="menu-link mainLink" data-toggle="dropdown" title="<%=$SH("_content")%>"><span class="fa fa-bars ">&nbsp;</span><%=$SH("_content")%></a>
<div class="dropdown-menu">
    <a class="dropdown-item level0 <%=pathIds.contains(home.getId())? "active" : ""%>" href="<%=home.getUrl()%>"><%=home.getNavDisplay()%></a>
    <% for (PageData contentData : home.getChildren()) {
            if (contentData.isActive() && contentData.isInHeaderNav() && contentData.hasUserReadRight(rdata.getLoginUser()) && !(contentData instanceof LinkData)) {%>
    <a class="dropdown-item level1 <%=pathIds.contains(contentData.getId())? "active" : ""%>" href="<%=contentData.getUrl()%>"><%=contentData.getNavDisplay()%>
    </a>
                <%List<PageData> children = new ArrayList<>();
                for (PageData child : contentData.getChildren()) {
                    if (child.isInHeaderNav() && child.hasUserReadRight(rdata.getLoginUser()))
                        children.add(child);
                }
                if (!children.isEmpty()) {
                    for (PageData child : children){
                %>
    <a class="dropdown-item level2 <%=pathIds.contains(child.getId())? "active" : ""%>" href="<%=child.getUrl()%>"><%=child.getNavDisplay()%></a>
                    <%
                        for (PageData subchild: child.getChildren()){%>
    <a class="dropdown-item level3 <%=pathIds.contains(subchild.getId())? "active" : ""%>" href="<%=subchild.getUrl()%>"><%=subchild.getNavDisplay()%></a>
    <%
                            for (PageData subsubchild: subchild.getChildren()){%>
    <a class="dropdown-item level4 <%=pathIds.contains(subchild.getId())? "active" : ""%>" href="<%=subsubchild.getUrl()%>"><%=subsubchild.getNavDisplay()%></a>
                    <%
                            }
                        }
                    }%>
<%
                }
            }
        }%>
</div>
    <%}%>
