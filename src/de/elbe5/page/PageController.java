/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.application.Configuration;
import de.elbe5.application.MailHelper;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.base.Log;
import de.elbe5.content.*;
import de.elbe5.file.ImageBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.*;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PageController extends ContentLogController {

    public static final String KEY = "page";

    private static PageController instance = null;

    public static void setInstance(PageController instance) {
        PageController.instance = instance;
    }

    public static PageController getInstance() {
        return instance;
    }

    public static void register(PageController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    };

    @Override
    public IResponse openEditFrontendContent(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        PageData data = ContentBean.getInstance().getContent(contentId,PageData.class);
        checkRights(data.hasUserEditRight(rdata));
        data.setUpdateValues(ContentCache.getContent(data.getId()), rdata);
        data.startEditing();
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT, data);
        return data.getDefaultView();
    }

    @Override
    public IResponse showEditFrontendContent(RequestData rdata) {
        assertSessionCall(rdata);
        PageData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, PageData.class);
        checkRights(data.hasUserEditRight(rdata));
        return data.getDefaultView();
    }

    @Override
    public IResponse saveFrontendContent(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        PageData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, PageData.class);
        checkRights(data.hasUserEditRight(rdata));
        data.readFrontendRequestData(rdata);
        data.setChangerId(rdata.getUserId());
        if (!ContentBean.getInstance().saveContent(data)) {
            setSaveError(rdata);
            return data.getDefaultView();
        }
        data.setViewType(ContentViewType.SHOW);
        rdata.removeSessionObject(ContentRequestKeys.KEY_CONTENT);
        ContentCache.setDirty();
        return show(rdata);
    }

    @Override
    public IResponse cancelEditFrontendContent(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        PageData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, PageData.class);
        assert data.getId() == contentId;
        checkRights(data.hasUserEditRight(rdata));
        data.stopEditing();
        return data.getDefaultView();
    }

    public IResponse showDraft(RequestData rdata){
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        ContentData data = ContentCache.getContent(contentId);
        checkRights(data.hasUserReadRight(rdata));
        data.setViewType(ContentViewType.SHOW);
        return data.getDefaultView();
    }

    public IResponse showPublished(RequestData rdata){
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        ContentData data = ContentCache.getContent(contentId);
        checkRights(data.hasUserReadRight(rdata));
        data.setViewType(ContentViewType.PUBLISHED);
        return data.getDefaultView();
    }

    //frontend
    public IResponse publishPage(RequestData rdata){
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        Log.log("Publishing page" + contentId);
        PageData data=ContentBean.getInstance().getContent(contentId,PageData.class);
        checkRights(data.hasUserApproveRight(rdata));
        data.setViewType(ContentViewType.PUBLISH);
        data.setPublishDate(PageBean.getInstance().getServerTime());
        return data.getDefaultView();
    }

    public IResponse openLinkBrowser(RequestData rdata) {
        assertSessionCall(rdata);
        ContentData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        checkRights(data.hasUserEditRight(rdata));
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/browseLinks.jsp");
    }

    public IResponse openImageBrowser(RequestData rdata) {
        assertSessionCall(rdata);
        ContentData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        checkRights(data.hasUserEditRight(rdata));
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/browseImages.jsp");
    }

    public IResponse addImage(RequestData rdata) {
        assertSessionCall(rdata);
        ContentData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        checkRights(data.hasUserEditRight(rdata));
        ImageData image=new ImageData();
        image.setCreateValues(data,rdata);
        image.readRequestData(rdata);
        ImageBean.getInstance().saveFile(image,true);
        ContentCache.setDirty();
        rdata.getAttributes().put("imageId", Integer.toString(image.getId()));
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/addImage.ajax.jsp");
    }

    public IResponse republishPage(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();

        PageData page = ContentCache.getContent(contentId, PageData.class);
        if (page != null){
            String url = rdata.getRequest().getRequestURL().toString();
            String uri = rdata.getRequest().getRequestURI();
            int idx = url.lastIndexOf(uri);
            url = url.substring(0, idx);
            url +="/ctrl/page/publishPage/"+contentId;
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofMinutes(2))
                        .build();
                HttpClient client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(20))
                        .build();
                client.send(request, HttpResponse.BodyHandlers.ofString());
            }
            catch (IOException | InterruptedException e){
                Log.error("could not send publishing request", e);
            }
        }
        return new ForwardResponse("/ctrl/admin/openContentAdministration?contentId=contentId");
    }

    public IResponse addPart(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        PageData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, PageData.class);
        checkRights(data.hasUserEditRight(rdata));
        int fromPartId = rdata.getAttributes().getInt("fromPartId", -1);
        String partType = rdata.getAttributes().getString("partType");
        PagePartData pdata = PageBean.getInstance().getNewPagePartData(partType);
        pdata.setCreateValues(rdata);
        data.addPart(pdata, fromPartId, true);
        rdata.getAttributes().put(PagePartData.KEY_PART, pdata);
        return new ForwardResponse("/WEB-INF/_jsp/page/newPart.ajax.jsp");
    }

    public IResponse sendContact(RequestData rdata) {
        assertSessionCall(rdata);
        String captcha = rdata.getAttributes().getString("captcha");
        String sessionCaptcha = rdata.getSessionObject(RequestKeys.KEY_CAPTCHA, String.class);
        if (!captcha.equals(sessionCaptcha)){
            rdata.addFormField("captcha");
            rdata.addFormError(LocalizedStrings.string("_captchaError"));
            return show(rdata);
        }
        String name = rdata.getAttributes().getString("contactName");
        String email = rdata.getAttributes().getString("contactEmail");
        String message = rdata.getAttributes().getString("contactMessage");
        if (name.isEmpty()) {
            rdata.addIncompleteField("contactName");
        }
        if (email.isEmpty()) {
            rdata.addIncompleteField("contactEmail");
        }
        if (message.isEmpty()) {
            rdata.addIncompleteField("contactMessage");
        }
        if (!rdata.checkFormErrors()){
            return show(rdata);
        }
        message = String.format(LocalizedStrings.html("_contactRequestText"),name,email) + message;
        if (!MailHelper.sendPlainMail(Configuration.getMailReceiver(), LocalizedStrings.string("_contactRequest"), message)) {
            rdata.setMessage(LocalizedStrings.string("_contactRequestError"), RequestKeys.MESSAGE_TYPE_ERROR);
            return show(rdata);
        }
        rdata.setMessage(LocalizedStrings.string("_contactRequestSent"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        rdata.removeSessionObject(RequestKeys.KEY_CAPTCHA);
        return show(rdata);
    }

}
