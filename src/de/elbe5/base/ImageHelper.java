/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2018 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.base;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class ImageHelper {

    public static BufferedImage createImage(byte[] bytes, String contentType) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(contentType);
        if (readers.hasNext() && bytes != null) {
            ImageReader reader = readers.next();
            BufferedImage image;
            boolean singleImage = !contentType.endsWith("gif");
            ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
            reader.setInput(iis, singleImage);
            image = reader.read(0);
            return image;
        }
        return null;
    }

    public static BufferedImage createResizedImage(byte[] bytes, String contentType, int maxSize) throws IOException {
        BufferedImage source = createImage(bytes, contentType);
        if (source == null) {
            return null;
        }
        float factor = getResizeFactor(source, maxSize);
        if (factor == 1) {
            return source;
        }
        return copyImage(source, factor);
    }

    public static BufferedImage createScaledImage(byte[] bytes, String contentType, int scalePercent) throws IOException {
        BufferedImage source = createImage(bytes, contentType);
        if (source == null) {
            return null;
        }
        float factor = ((float)scalePercent)/100;
        if (factor == 1) {
            return source;
        }
        return copyImage(source, factor);
    }

    public static float getResizeFactor(BufferedImage source, int maxSize) {
        if (source == null || maxSize == 0) {
            return 1;
        }
        float wfactor = ((float) maxSize) / source.getWidth();
        float hfactor = ((float) maxSize) / source.getHeight();
        float factor = 1;
        if (wfactor != 0)
            factor=wfactor;
        if (hfactor != 0)
            factor=Math.min(factor, hfactor);
        if (factor>1)
            factor = 1;
        return factor;
    }

    public static BufferedImage copyImage(BufferedImage source, float factor) {
        BufferedImage bi = new BufferedImage((int) (source.getWidth() * factor), (int) (source.getHeight() * factor), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        AffineTransform at = AffineTransform.getScaleInstance(factor, factor);
        g.drawRenderedImage(source, at);
        return bi;
    }

    public static BufferedImage rotateImageFromOrientation(BufferedImage src, int orientation){
        assert(src != null);
        int width = src.getWidth();
        int height = src.getHeight();
        int w;
        int h;
        double angle;
        switch(orientation){
            case 3 -> {
                w = width;
                h = height;
                angle = Math.PI;
            }
            case 6 -> {
                w = height;
                h = width;
                angle = Math.PI / 2;
            }
            case 8 -> {
                w = height;
                h = width;
                angle = Math.PI * 3 / 2;
            }
            default -> {
                return src;
            }
        }
        BufferedImage output = new BufferedImage(w, h, src.getType());
        Graphics2D g2d = (Graphics2D)output.getGraphics();
        g2d.translate(height/2., width/2.);
        g2d.rotate(angle);
        g2d.translate(-width/2., -height/2.);
        g2d.drawImage(src, 0,0,width, height,null);
        return output;
    }

    public static byte[] writeImage(ImageWriter writer, BufferedImage image) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(bout);
        writer.setOutput(ios);
        writer.write(image);
        bout.flush();
        bout.close();
        return bout.toByteArray();
    }
}
