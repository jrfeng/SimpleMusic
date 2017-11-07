package jrfeng.player.utils.mp3;

import java.io.UnsupportedEncodingException;

class ID3V1Info implements MP3Info {
    private String mSongName;
    private String mArtist;
    private String mAlbum;
    private String mYear;
    private String mComment;

    private long mLengthMesc;

    private String mCharset;

    ID3V1Info(byte[] data, long lengthMesc, String charset) {
        mLengthMesc = lengthMesc;
        if (charset != null && charset.length() > 1) {
            mCharset = charset;
        } else {
            mCharset = "UTF-8";
        }

        mSongName = decodeFrame(data, 3, 30);
        mArtist = decodeFrame(data, 33, 30);
        mAlbum = decodeFrame(data, 63, 30);
        mYear = decodeFrame(data, 93, 4);
        mComment = decodeFrame(data, 97, 28);
    }

    @Override
    public String getSongName() {
        return mSongName;
    }

    @Override
    public String getArtist() {
        return mArtist;
    }

    @Override
    public String getAlbum() {
        return mAlbum;
    }

    @Override
    public String getYear() {
        return mYear;
    }

    @Override
    public String getComment() {
        return mComment;
    }

    @Override
    public long getLengthMesc() {
        return mLengthMesc;
    }

    @Override
    public boolean hasImage() {
        return false;
    }

    @Override
    public byte[] getImage() {
        return null;
    }

    private String decodeFrame(byte[] data, int offset, int length) {
        String msg;
        try {
            msg = new String(data, offset, length, mCharset).trim();
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.toString());
            return "未知";
        }

        //排除多余字符
        int i = msg.indexOf("\0");
        if (i != -1) {
            msg = msg.substring(0, i);
        }

        //排除乱码
        int j = msg.indexOf(String.valueOf((char) 65533));
        if (j != -1) {
            msg = msg.substring(0, j);
        }

        if (msg.equals("")) {
            msg = "未知";
        }
        return msg;
    }
}
