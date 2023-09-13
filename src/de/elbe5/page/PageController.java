/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.page;

import de.elbe5.mail.MailConfiguration;
import de.elbe5.mail.MailHelper;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.base.Log;
import de.elbe5.content.*;
import de.elbe5.file.ImageBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.*;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;

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
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        data.setUpdateValues(ContentCache.getContent(data.getId()), rdata);
        data.setEditMode(true);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT, data);
        return data.getDefaultView();
    }

    @Override
    public IResponse showEditFrontendContent(RequestData rdata) {
        assertSessionCall(rdata);
        PageData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, PageData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        return data.getDefaultView();
    }

    @Override
    public IResponse saveFrontendContent(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        PageData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, PageData.class);
        assert(contentId == data.getId());
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        data.readFrontendRequestData(rdata);
        data.setChangerId(rdata.getUserId());
        if (!ContentBean.getInstance().saveContent(data)) {
            setSaveError(rdata);
            return data.getDefaultView();
        }
        data.setEditMode(false);
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
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        data.setEditMode(false);
        return data.getDefaultView();
    }

    public IResponse showDraft(RequestData rdata){
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        PageData data = ContentCache.getContent(contentId, PageData.class);
        assert(data!=null);
        assertRights(data.hasUserReadRight(rdata.getLoginUser()));
        return data.getDefaultView();
    }

    public IResponse showPublished(RequestData rdata){
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        PageData data = ContentCache.getContent(contentId, PageData.class);
        assert(data!=null);
        assertRights(data.hasUserReadRight(rdata.getLoginUser()));
        data.showPublished(true);
        return data.getDefaultView();
    }

    //frontend
    public IResponse publishPage(RequestData rdata){
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        Log.log("Publishing page" + contentId);
        PageData data=ContentBean.getInstance().getContent(contentId,PageData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        data.setPublishing(true);
        data.setPublishDate(PageBean.getInstance().getServerTime());
        return data.getDefaultView();
    }

    public IResponse openLinkBrowser(RequestData rdata) {
        assertSessionCall(rdata);
        ContentData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/browseLinks.jsp");
    }

    public IResponse openImageBrowser(RequestData rdata) {
        assertSessionCall(rdata);
        ContentData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/browseImages.jsp");
    }

    public IResponse addImage(RequestData rdata) {
        assertSessionCall(rdata);
        ContentData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        ImageData image=new ImageData();
        image.setCreateValues(data,rdata);
        image.readRequestData(rdata);
        ImageBean.getInstance().saveFile(image,true);
        ContentCache.setDirty();
        rdata.getAttributes().put("imageId", Integer.toString(image.getId()));
        return new ForwardResponse("/WEB-INF/_jsp/ckeditor/addImage.ajax.jsp");
    }

    public IResponse addPart(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        PageData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, PageData.class);
        assertRights(data.getId() == contentId && data.hasUserEditRight(rdata.getLoginUser()));
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
            rdata.addFormErrorField("captcha");
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
        if (!MailHelper.sendPlainMail(MailConfiguration.getMailReceiver(), LocalizedStrings.string("_contactRequest"), message)) {
            rdata.setMessage(LocalizedStrings.string("_contactRequestError"), RequestKeys.MESSAGE_TYPE_ERROR);
            return show(rdata);
        }
        rdata.setMessage(LocalizedStrings.string("_contactRequestSent"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        rdata.removeSessionObject(RequestKeys.KEY_CAPTCHA);
        return show(rdata);
    }

}
