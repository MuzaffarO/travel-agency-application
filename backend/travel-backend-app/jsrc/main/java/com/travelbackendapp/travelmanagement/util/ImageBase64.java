package com.travelbackendapp.travelmanagement.util;

import java.util.Base64;

public final class ImageBase64 {

    public static final int MAX_BYTES = 5 * 1024 * 1024; // 5 MB

    public static class Result {
        public final byte[] bytes;
        public final String contentType; // image/png, image/jpeg, image/webp
        public final String ext;         // png, jpg, webp
        public Result(byte[] b, String ct, String e){ this.bytes=b; this.contentType=ct; this.ext=e; }
    }

    private ImageBase64(){}

    public static Result decodeAndDetect(String imageBase64){
        if (imageBase64 == null || imageBase64.isBlank()) throw new IllegalArgumentException("imageBase64 is empty");

        // Accept both pure base64 and data URL prefix
        String b64 = imageBase64.trim();
        int comma = b64.indexOf(',');
        if (b64.startsWith("data:") && comma > 0) {
            b64 = b64.substring(comma + 1);
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(b64);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("imageBase64 is not valid base64");
        }

        if (bytes.length == 0) throw new IllegalArgumentException("decoded image is empty");
        if (bytes.length > MAX_BYTES) throw new IllegalArgumentException("image is too large (max 5 MB)");

        // Magic bytes: PNG, JPEG, WEBP
        if (isPng(bytes))   return new Result(bytes, "image/png", "png");
        if (isJpeg(bytes))  return new Result(bytes, "image/jpeg", "jpg");
        if (isWebp(bytes))  return new Result(bytes, "image/webp", "webp");

        throw new IllegalArgumentException("unsupported image type (allow: PNG, JPEG, WEBP)");
    }

    private static boolean isPng(byte[] b){
        if (b.length < 8) return false;
        return (b[0]==(byte)0x89 && b[1]==0x50 && b[2]==0x4E && b[3]==0x47
             && b[4]==0x0D && b[5]==0x0A && b[6]==0x1A && b[7]==0x0A);
    }
    private static boolean isJpeg(byte[] b){
        return b.length >= 3 && (b[0]==(byte)0xFF && b[1]==(byte)0xD8 && b[2]==(byte)0xFF);
    }
    private static boolean isWebp(byte[] b){
        if (b.length < 12) return false;
        return (b[0]=='R' && b[1]=='I' && b[2]=='F' && b[3]=='F'
             && b[8]=='W' && b[9]=='E' && b[10]=='B' && b[11]=='P');
    }
}
