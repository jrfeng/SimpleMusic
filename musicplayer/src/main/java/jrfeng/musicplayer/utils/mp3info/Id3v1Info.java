package jrfeng.musicplayer.utils.mp3info;

import java.io.UnsupportedEncodingException;

public class Id3v1Info implements BaseInfo{
    private byte[] buf;
    private String charset;

    public Id3v1Info(byte[] data) {
        buf = data;
        charset = "UTF-8";
    }

    @Override
    public String getSongName() {
        return decode(3, 30);
    }

    @Override
    public String getArtist() {
        return decode(33, 30);
    }

    @Override
    public String getAlbum() {
        return decode(63, 30);
    }

    @Override
    public String getYear() {
        return decode(93, 4);
    }

    @Override
    public String getComment() {
        return decode(97, 28);
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    private String decode(int offset, int length) {
        String msg;
        try {
            msg = new String(buf, offset, length, charset).trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "未知";
        }

        if (msg.equals("")) {
            msg = "未知";
        }

        return msg;
    }
}
