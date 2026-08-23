/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import de.elbe5.application.Configuration;
import de.elbe5.base.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

public class ImageData extends FileData {

    public static int DEFAULT_PREVIEW_SIZE = 200;

    protected int width = 0;
    protected int height = 0;
    protected byte[] previewBytes = null;
    protected boolean hasPreview = false;
    public int maxSize = 0;
    public int previewSize = DEFAULT_PREVIEW_SIZE;

    public ImageData() {
    }

    public boolean isImage() {
        return true;
    }

    public FileBean getBean() {
        return ImageBean.getInstance();
    }

    public String getIconStyle(){
        return "fa-image";
    }

    public String getControllerKey() {
        return ImageController.KEY;
    }

    public String getEditURL(){
        return "/WEB-INF/_jsp/file/editImage.ajax.jsp";
    }

    // base data

    public String getPreviewURL(){
        return "/ctrl/image/showPreview/"+getId();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte[] getPreviewBytes() {
        return previewBytes;
    }

    public void setPreviewBytes(byte[] previewBytes) {
        this.previewBytes = previewBytes;
    }

    public boolean hasPreview() {
        return hasPreview;
    }

    public void setHasPreview(boolean hasPreview) {
        this.hasPreview = hasPreview;
    }

    public String getPreviewName(){
        return "preview_" + getId() + ".jpg";
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getPreviewSize() {
        return previewSize;
    }

    public void setPreviewSize(int previewSize) {
        this.previewSize = previewSize;
    }

    public boolean resizeImage(){
        try {
            return createResizedImage(Configuration.getMaxImageSize());
        }
        catch (Exception e){
            Log.error("could not resize image", e);
            return false;
        }
    }

    // multiple data

    @Override
    public boolean createFromBinaryFile(BinaryFile file) {
        if (super.createFromBinaryFile(file) && file.isImage()){
            correctImageByExif();
            int resizeSize = maxSize!=0 ? maxSize : Configuration.getMaxImageSize();
            if (resizeSize != 0)
                try {
                    createResizedImage(resizeSize);
                    createPreview(getPreviewSize());
                    return true;
                } catch (IOException e) {
                    Log.warn("could not create buffered image");
                }
            else {
                try {
                    createPreview(getPreviewSize());
                    return true;
                } catch (IOException e) {
                    Log.warn("could not create preview");
                }
            }
        }
        return false;
    }

    //helper

    protected boolean createResizedImage(int maxSize) throws IOException {
        BufferedImage bi = ImageHelper.createResizedImage(getBytes(), getContentType(), maxSize);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(getContentType());
        if (writers.hasNext()) {
            setContentType(getContentType());
        } else {
            writers = ImageIO.getImageWritersBySuffix(FileHelper.getExtension(getFileName()));
            if (writers.hasNext()) {
                setContentType("");
            } else {
                setFileName(FileHelper.getFileNameWithoutExtension(getFileName()) + ".jpg");
                writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
                setContentType("image/jpeg");
            }
        }
        ImageWriter writer = writers.next();
        setBytes(ImageHelper.writeImage(writer, bi));
        setFileSize(getBytes().length);
        assert bi != null;
        setWidth(bi.getWidth());
        setHeight(bi.getHeight());
        return true;
    }

    public void createPreview(int previewSize) throws IOException{
        if (!isImage())
            return;
        BufferedImage source = ImageHelper.createImage(getBytes(), getContentType());
        if (source != null) {
            setWidth(source.getWidth());
            setHeight(source.getHeight());
            float factor = ImageHelper.getResizeFactor(source, previewSize);
            BufferedImage image = ImageHelper.copyImage(source, factor);
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
            ImageWriter writer = writers.next();
            setPreviewBytes(ImageHelper.writeImage(writer, image));
        }
    }

    public void correctImageByExif() {
        int orientation = getOrientation();
        if (orientation != 1) {
            try {
                BufferedImage source = ImageHelper.createImage(getBytes(), getContentType());
                assert (source != null);
                Log.info("correcting image with orientation " + orientation);
                BufferedImage output = ImageHelper.rotateImageFromOrientation(source, orientation);
                Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
                ImageWriter writer = writers.next();
                setBytes(ImageHelper.writeImage(writer, output));
            }
            catch(IOException ignore){
                Log.error("could not correct image");
            }
        }
    }

    public int getOrientation(){
        int orientation = 1;
        try {
            InputStream input = new ByteArrayInputStream(bytes);
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
        }
        catch (Exception ignore){
        }
        return orientation;
    }

}
