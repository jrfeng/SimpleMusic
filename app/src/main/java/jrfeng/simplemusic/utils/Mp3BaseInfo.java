package jrfeng.simplemusic.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mp3BaseInfo {
    private String charset;
    private byte[] buf;
    private boolean hasTag;
    private String songName;

    public void load(String mp3file) throws IOException {
        load(mp3file, "UTF-8");
    }

    public void load(File mp3file) throws IOException {
        load(mp3file, "UTF-8");
    }

    public void load(String mp3file, String charset) throws IOException {
        load(new File(mp3file), charset);
    }

    public void load(File mp3file, String charset) throws IOException {
        songName = mp3file.getName().substring(0, mp3file.getName().lastIndexOf("."));

        RandomAccessFile raf = new RandomAccessFile(mp3file, "r");
        raf.seek(raf.length() - 128);
        this.charset = charset;
        buf = new byte[128];
        raf.read(buf);
        raf.close();
        hasTag = "TAG".equals(new String(buf, 0, 3));
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean hasTag() {
        return hasTag;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtist() {
        String msg = decode(33, 30);
        if (msg.equals("未知")) {
            return msg + "歌手";
        } else {
            return msg;
        }
    }

    public String getAlbum() {
        String msg = decode(33, 30);
        if (msg.equals("未知")) {
            return msg + "专辑";
        } else {
            return msg;
        }
    }

    public String getYear() {
        String msg = decode(33, 30);
        if (msg.equals("未知")) {
            return msg + "年代";
        } else {
            return msg;
        }
    }

    public String getComment() {
        return decode(97, 28);
    }

    //***********************private***********************

    private String decode(int offset, int length) {
        if (!hasTag()) {
            return "未知";
        }

        String msg;
        try {
            msg = new String(buf, offset, length, charset).trim();
        } catch (UnsupportedEncodingException e) {
            msg = new String(buf, offset, length).trim();
            e.printStackTrace();
        }

        for (int i = 0; i < msg.length(); i++) {//删除中文乱码
            if (isMessyCode(msg.substring(i, i + 1))) {
                msg = msg.substring(0, i).trim();
                break;
            }
        }

        if (msg.equals("")) {
            msg = "未知";
        }

        return msg;
    }

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    public boolean isMessyCode(String strName) {
        Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
        Matcher m = p.matcher(strName);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
        float chLength = 0;
        float count = 0;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!Character.isLetterOrDigit(c)) {
                if (!isChinese(c)) {
                    count = count + 1;
                }
                chLength++;
            }
        }
        float result = count / chLength;
        if (result > 0.4) {
            return true;
        } else {
            return false;
        }
    }
}
