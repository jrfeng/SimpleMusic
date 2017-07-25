package jrfeng.simplemusic.utils.mp3info;

import java.io.UnsupportedEncodingException;

public class Id3v2Info implements BaseInfo {
    private byte[] buf;
    private String bufStr;

    public static final String TEXT = "TEXT";   //歌词作者
    public static final String TENC = "TENC";   //编码
    public static final String WXXX = "WXXX";   //URL连接
    public static final String TCOP = "TCOP";   //版权（Copyright）
    public static final String TOPE = "TOPE";   //原艺术家
    public static final String TCOM = "TCOM";   //作曲家
    public static final String TDAT = "TDAT";   //日期
    public static final String TPE1 = "TPE1";   //艺术家（相当于Id3v1的Artist）
    public static final String TPE2 = "TPE2";   //乐队
    public static final String TPE3 = "TPE3";   //指挥者
    public static final String TPE4 = "TPE4";   //翻译（记录员、修改员）
    public static final String TYER = "TYER";   //年份（相当于Id3v1的Year）
    public static final String USLT = "USLT";   //歌词
    public static final String TSIZ = "TSIZ";   //大小
    public static final String TALB = "TALB";   //专辑（相当于Id3v1的Album）
    public static final String TIT1 = "TIT1";   //内容组描述
    public static final String TIT2 = "TIT2";   //歌曲名（相当于Id3v1的Title/SongName）
    public static final String TIT3 = "TIT3";   //副标题
    public static final String TCON = "TCON";   //流派/风格（相当于Id3v1的Genre）
    public static final String AENC = "AENC";   //音频加密技术
    public static final String TBPM = "TBPM";   //每分钟节拍数
    public static final String COMM = "COMM";   //注释（相当于Id3v1的Comment）
    public static final String TDLY = "TDLY";   //播放列表返录
    public static final String TRCK = "TRCK";   //音轨（相当于Id3v1的Track）
    public static final String TFLT = "TFLT";   //文件类型
    public static final String TIME = "TIME";   //时间
    public static final String TKEY = "TKEY";   //最初关键字
    public static final String TLAN = "TLAN";   //语言
    public static final String TLEN = "TLEN";   //长度
    public static final String TMED = "TMED";   //媒体类型
    public static final String TOAL = "TOAL";   //原唱片集
    public static final String TOFN = "TOFN";   //原文件名
    public static final String TOLY = "TOLY";   //原歌词作者
    public static final String TORY = "TORY";   //最初发行年份
    public static final String TOWM = "TOWM";   //文件所有者（许可证者）
    public static final String TPOS = "TPOS";   //作品集部分
    public static final String TPUB = "TPUB";   //发行人
    public static final String TRDA = "TRDA";   //录制日期
    public static final String TRSN = "TRSN";   //Internet电台名称
    public static final String TRSO = "TRSO";   //Internet电台所有者
    public static final String UFID = "UFID";   //唯一的文件标识符
    public static final String TSRC = "TSRC";   //ISRC（国际标准记录码）
    public static final String TSSE = "TSSE";   //编码使用的软件/硬件设置

    public Id3v2Info(byte[] data) {
        buf = data;
        try {
            bufStr = new String(buf, "ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String get(String id) {
        int index = bufStr.indexOf(id);
        if (index == -1) {
            return "未知";
        }

        int a = buf[index + 7];
        int b = buf[index + 6];
        int c = buf[index + 5];
        int d = buf[index + 4];

        a &= 0xFF;
        b &= 0xFF;
        c &= 0xFF;
        d &= 0xFF;

        int a_low = a & 0x0F;
        int a_high = (a & 0xF0) >> 4;

        int b_low = b & 0x0F;
        int b_high = (b & 0xF0) >> 4;

        int c_low = c & 0x0F;
        int c_high = (c & 0xF0) >> 4;

        int d_low = d & 0x0F;
        int d_high = (d & 0xF0) >> 4;

        int size = a_low + a_high * 0x10 +
                b_low * 0x100 + b_high * 0x1000 +
                c_low * 0x10000 + c_high * 0x100000 +
                d_low * 0x1000000 + d_high * 0x10000000;

        int charsetType = buf[index + 10];
        String charset = decodeCharset(charsetType);
        try {
            return new String(buf, index + 11, size - 1, charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "未知";
        }
    }

    @Override
    public String getSongName() {
        return get(TIT2);
    }

    @Override
    public String getArtist() {
        return get(TPE1);
    }

    @Override
    public String getAlbum() {
        return get(TALB);
    }

    @Override
    public String getYear() {
        return get(TYER);
    }

    @Override
    public String getComment() {
        return get(COMM);
    }

    public boolean hasImage() {
        return bufStr.contains("APIC");
    }

    public byte[] getImage() {
        int index = bufStr.indexOf("APIC");
        if (index == -1) {
            return null;
        }

        int imageStart = 0;
        for (int i = index; i < buf.length; i++) {
            if (buf[i] == -1 && buf[i + 1] == -40) {
                imageStart = i;
            }
        }
        int imageEnd = buf.length;
        for (int i = imageStart; i < buf.length; i++) {
            if (buf[i] == -1 && buf[i + 1] == -43) {
                imageStart = i;
            }
        }

        byte[] image = new byte[imageEnd - imageStart];
        System.arraycopy(buf, imageStart, image, 0, image.length);
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
