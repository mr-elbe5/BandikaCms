/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.user;

import de.elbe5.base.PBKDF2Encryption;
import de.elbe5.base.BinaryFile;
import de.elbe5.base.Log;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

public class UserSecurity {

    public static final String UPPER_ALPHA_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWER_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    public static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private static char getRandomChar(String chars, Random random) {
        return chars.charAt(random.nextInt(chars.length()));
    }

    public static String generateCaptchaString() {
        Random random = new Random();
        random.setSeed(Instant.now().toEpochMilli());
        char[] chars = new char[5];
        for ( int i=0; i<5; i++){
            chars[i] = getRandomChar(CAPTCHA_CHARS, random);
        }
        return new String(chars);
    }

    public static BinaryFile getCaptcha(String captcha) {
        int width = 300;
        int height = 60;
        int fontSize = 26;
        String fontName = "Courier";
        Color gradiantStartColor = new Color(170, 170, 170);
        Color gradiantEndColor = new Color(225, 225, 225);
        java.util.List<Color> textColors = new ArrayList<>();
        textColors.add(new Color(255, 0, 0));
        textColors.add(new Color(0, 160, 0));
        textColors.add(new Color(0, 0, 255));
        BinaryFile data;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(rh);
        GradientPaint gp = new GradientPaint(0, 0, gradiantStartColor, 0, (float) (height / 2), gradiantEndColor, true);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);
        Random r = new Random();
        for (int i = 0; i < width - 10; i = i + 25) {
            int q = Math.abs(r.nextInt()) % width;
            int colorIndex = Math.abs(r.nextInt()) % 200;
            g2d.setColor(new Color(colorIndex, colorIndex, colorIndex));
            g2d.drawLine(i, q, width, height);
            g2d.drawLine(q, i, i, height);
        }
        int x;
        int y;
        int capCount = captcha.length();
        int capWidth = width / (capCount +1);
        Font font = new Font(fontName, Font.PLAIN, fontSize);
        g2d.setFont(font);
        for (int i = 0; i < capCount; i++) {
            g2d.setColor(textColors.get(i%3));
            x = capWidth/3 + i*capWidth + (Math.abs(r.nextInt()) % (capWidth/2));
            y = height/3 + Math.abs(r.nextInt()) % (height/2);
            g2d.drawChars(captcha.toCharArray(), i, 1, x, y);
        }
        for (int i = 0; i < width; i = i + 20) {
            int q = Math.abs(r.nextInt()) % width;
            int colorIndex = Math.abs(r.nextInt()) % 200;
            g2d.setColor(new Color(colorIndex, colorIndex, colorIndex));
            g2d.drawLine(i, q, width, height);
            g2d.drawLine(q, i, i, height);
        }
        g2d.dispose();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            data = new BinaryFile();
            data.setContentType("image/png");
            ImageIO.write(bufferedImage, "png", out);
            out.close();
            data.setBytes(out.toByteArray());
        } catch (IOException e) {
            return null;
        }
        return data;
    }

    public static String getApprovalString() {
        return getRandomString(8, UPPER_ALPHA_CHARS);
    }

    public static String getRandomString(int count, String sourceChars) {
        Random random = new Random();
        random.setSeed(Instant.now().toEpochMilli());
        char[] chars = new char[count];
        for (int i = 0; i < count; i++) {
            chars[i] = getRandomChar(sourceChars, random);
        }
        return new String(chars);
    }

    public static String generateKey() {
        try {
            return PBKDF2Encryption.generateSaltBase64();
        } catch (Exception e) {
            Log.error("failed to create password key", e);
            return null;
        }
    }

    public static String encryptPassword(String pwd, String key) {
        try {
            return PBKDF2Encryption.getEncryptedPasswordBase64(pwd, key);
        } catch (Exception e) {
            Log.error("failed to encrypt password", e);
            return null;
        }
    }
}
