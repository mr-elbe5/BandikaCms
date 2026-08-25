/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import de.elbe5.base.BaseData;
import de.elbe5.base.BinaryFile;
import de.elbe5.base.FileHelper;
import de.elbe5.base.StringHelper;
import de.elbe5.page.PageData;
import de.elbe5.request.RequestData;

public abstract class FileData extends BaseData {

    private String fileName = "";
    private String extension = "";
    private String displayName = "";
    private String description = "";
    protected String contentType = null;
    protected int fileSize = 0;
    protected byte[] bytes = null;

    protected int parentId = 0;
    protected PageData parent = null;

    public FileData() {
    }

    public String getType() {
        return getClass().getName();
    }

    public String getControllerKey() {
        return FileController.KEY;
    }

    public FileBean getBean() {
        return FileBean.getInstance();
    }

    public String getIconStyle(){
        return "fa-file-o";
    }

    public void adjustFileNameToDisplayName(){
        if (getFileName().isEmpty() || getDisplayName().isEmpty())
            return;
        int pos= getFileName().lastIndexOf('.');
        if (pos==-1)
            return;
        setFileName(StringHelper.toSafeWebFileName(getDisplayName())+ getFileName().substring(pos));
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        extension = FileHelper.getExtension(fileName);
    }

    public String getExtension() {
        return extension;
    }

    public String getStaticFileName() {
        return getId() + extension;
    }

    public String getStaticURL(){
        return "/files/"+ getStaticFileName();
    }

    public String getEditURL(){
        return "/WEB-INF/_jsp/file/editFile.ajax.jsp";
    }

    public String getDisplayName() {
        if (displayName.isEmpty() )
            return FileHelper.getFileNameWithoutExtension(getFileName());
        return displayName;
    }

    public String getDisplayFileName(){
        return getDisplayName() + extension;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isImage() {
        return false;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public PageData getParent() {
        return parent;
    }

    public void setParent(PageData parent) {
        this.parent = parent;
    }

    // multiple data

    @Override
    public void setNewId(){
        setId(FileBean.getInstance().getNextId());
    }

    public void setParentValues(PageData parent){
        setParentId(parent.getId());
        setParent(parent);
    }

    public void setUpdateValues(RequestData rdata){
        super.setUpdateValues(rdata);
    }

    // helper

    public void readRequestData(RequestData rdata){
        setDescription(rdata.getAttributes().getString("description"));
        if (isNew()){
            BinaryFile file = rdata.getAttributes().getFile("file");
            createFromBinaryFile(file);
            if (getDisplayName().isEmpty()) {
                setDisplayName(file.getFileNameWithoutExtension());
            }
            else{
                adjustFileNameToDisplayName();
            }
        }
    }

    public boolean createFromBinaryFile(BinaryFile file) {
        if (file != null && file.getBytes() != null && !file.getFileName().isEmpty() && !StringHelper.isNullOrEmpty(file.getContentType())) {
            setFileName(file.getFileName());
            setBytes(file.getBytes());
            setFileSize(file.getBytes().length);
            setContentType(file.getContentType());
            return true;
        }
        return false;
    }

}
