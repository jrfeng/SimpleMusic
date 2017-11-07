package jrfeng.player.utils.mp3;

import java.io.UnsupportedEncodingException;

class ID3V2Info implements MP3Info {
    private String mSongName;
    private String mArtist;
    private String mAlbum;
    private String mYear;
    private String mComment;

    private byte[] mImage;

    private long mLengthMesc;

    ID3V2Info(byte[] data, long lengthMesc) {
        mSongName = decodeFrame(data, "TIT2");
        mArtist = decodeFrame(data, "TPE1");
        mAlbum = decodeFrame(data, "TALB");
        mYear = decodeFrame(data, "TYER");
        mComment = decodeFrame(data, "COMM");

        mImage = decodeImage(data);
        mLengthMesc = lengthMesc;
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
        return mImage != null;
    }

    @Override
    public byte[] getImage() {
        return mImage;
    }

    private String decodeFrame(byte[] data, String TAG) {
        //检查信息帧是否存在
        int index = MP3Util.frameIndex(data, TAG);
        if (index == -1) {
            return "未知";
        }

        int a = data[index + 7] & 0xFF;
        int b = data[index + 6] & 0xFF;
        int c = data[index + 5] & 0xFF;
        int d = data[index + 4] & 0xFF;

        int size = (int) (d * 0x100000000L + c * 0x10000 + b * 0x100 + a);

        String msg;
        try {
            msg = new String(data, index + 11, size - 1, decodeCharset(data[index + 10])).trim();
        } catch (UnsupportedEncodingException | StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            return "未知";
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

    private byte[] decodeImage(byte[] data) {
        int index = MP3Util.frameIndex(data, "APIC");
        if (index == -1) {
            return null;
        }

        int imageStart = 0;
        for (int i = index; i < data.length; i++) {
            if (data[i] == -1 && data[i + 1] == -40) {
                imageStart = i;
            }
        }
        int imageEnd = data.length;
        for (int i = imageStart; i < data.length; i++) {
            if (data[i] == -1 && data[i + 1] == -39) {
                imageEnd = i + 2; //要加上 2 ，因为末尾的 2 个结束符字节也要读进去
            }
        }

        byte[] image = new byte[imageEnd - imageStart];
        System.arraycopy(data, imageStart, image, 0, image.length);
        return image;
    }

    private String decodeCharset(int type) {
        switch (type) {
            case 0:
                return "GBK";
            case 1:
                return "UTF-16";
            case 2:
                return "UTF-16BE";
            case 3:
                return "UTF-8";
            default:
                return "UTF-8";
        }
    }
}
