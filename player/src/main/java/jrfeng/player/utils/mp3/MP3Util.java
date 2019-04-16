package jrfeng.player.utils.mp3;

import android.media.MediaMetadataRetriever;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MP3Util {

    /**
     * 加载 MP3 文件。默认使用 GBK 编码。
     *
     * @param file MP3 文件
     * @return MP3 文件信息。
     * @throws IOException 如果加载失败，则抛出 IOException 异常。
     */
    public static MP3Info load(File file) throws IOException {
        return load(file, "GBK");
    }

    /**
     * 加载 MP3 文件。
     *
     * @param file    MP3 文件。
     * @param charset 字符编码。
     * @return MP3 文件信息。
     * @throws IOException 如果加载失败，则抛出 IOException 异常。
     */
    public static MP3Info load(File file, String charset) throws IOException {
        byte[] mID3V1Data = new byte[128];
        byte[] mID3V2Head = new byte[10];
        RandomAccessFile rf = null;
        try {
            rf = new RandomAccessFile(file, "r");
            rf.read(mID3V2Head);
            rf.seek(rf.length() - 128);
            rf.read(mID3V1Data);

            //获取歌曲时长
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(file.getAbsolutePath());
            long lengthMesc = Integer.parseInt(
                    metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            ID3V1Info id3V1Info = null;
            ID3V2Info id3V2Info = null;

            //抽取ID3V1信息
            String id3V1TAG = new String(mID3V1Data, 0, 3);
            if (id3V1TAG.equals("TAG")) {
                System.out.println("抽取 ID3V1 信息");
                id3V1Info = new ID3V1Info(mID3V1Data, lengthMesc, charset);
            }

            //抽取ID3V2信息
            String id3V2TAG = new String(mID3V2Head, 0, 3);
            if (id3V2TAG.equals("ID3")) {
                System.out.println("抽取 ID3V2 信息");
                int size = (mID3V2Head[6] & 0x7f) * 0x200000
                        + (mID3V2Head[7] & 0x7f) * 0x4000
                        + (mID3V2Head[8] & 0x7f) * 0x80
                        + (mID3V2Head[9] & 0x7f);
                byte[] mID3V2Data = new byte[size];
                rf.seek(10);
                rf.read(mID3V2Data);
                id3V2Info = new ID3V2Info(mID3V2Data, lengthMesc);
            }

            return new MP3MergeInfo(id3V1Info, id3V2Info, lengthMesc);
        } finally {
            try {
                if (rf != null) {
                    rf.close();
                }
            } catch (IOException e) {
                System.out.println("Error : " + e.toString());
            }
        }
    }

    public static byte[] getMp3Image(File mp3File) {
        byte[] mID3V2Head = new byte[10];
        BufferedInputStream file = null;
        try {
            file = new BufferedInputStream(new FileInputStream(mp3File));
            int count = 0;
            do {
                count += file.read(mID3V2Head, count, mID3V2Head.length);
            } while (count < mID3V2Head.length);

            ID3V2Info id3V2Info;
            //抽取ID3V2信息
            String id3V2TAG = new String(mID3V2Head, 0, 3);
            if (id3V2TAG.equals("ID3")) {
                System.out.println("抽取 ID3V2 信息");
                int size = (mID3V2Head[6] & 0x7f) * 0x200000
                        + (mID3V2Head[7] & 0x7f) * 0x4000
                        + (mID3V2Head[8] & 0x7f) * 0x80
                        + (mID3V2Head[9] & 0x7f);
                byte[] mID3V2Data = new byte[size];
                count = 0;
                do {
                    count += file.read(mID3V2Data, count, mID3V2Data.length);
                } while (count < mID3V2Data.length);
                id3V2Info = new ID3V2Info(mID3V2Data, 0);
                return id3V2Info.getImage();
            } else {
                return null;
            }
        } catch (IOException e) {
            System.err.println(e.toString());
            return null;
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //*********************包可见**********************

    static int frameIndex(byte[] data, String frame) {
        char[] frameChars = frame.toCharArray();
        byte[] buf = new byte[frameChars.length];
        for (int i = 0; i < data.length - buf.length; i++) {
            System.arraycopy(data, i, buf, 0, buf.length);
            if (isEquals(buf, frameChars)) {
                return i;
            }
        }
        return -1;
    }

    //**************************private*************************

    private static boolean isEquals(byte[] data1, char[] data2) {
        for (int i = 0; i < data1.length; i++) {
            if ((data1[i] & 0xFF) != data2[i]) {
                return false;
            }
        }
        return true;
    }
}
