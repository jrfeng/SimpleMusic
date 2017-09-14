package jrfeng.musicplayer.utils.mp3info;

import android.util.Log;

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Mp3Info {
    private byte[] id3v1;
    private byte[] id3v2;
    private byte[] id3v2Head;

    private Id3v1Info id3v1Info;
    private Id3v2Info id3v2Info;

    private long lengthMesc;

    public Mp3Info() {
        id3v2Head = new byte[10];
        id3v1 = new byte[128];
    }

    public void load(File file) {
        RandomAccessFile rf = null;
        try {
            rf = new RandomAccessFile(file, "r");
            rf.read(id3v2Head);
            rf.seek(rf.length() - 128);
            rf.read(id3v1);

            if (hasId3v1()) {
                id3v1Info = new Id3v1Info(id3v1);
            }

            if (hasId3v2()) {
                int size = (id3v2Head[6] & 0x7f) * 0x200000
                        + (id3v2Head[7] & 0x7f) * 0x4000
                        + (id3v2Head[8] & 0x7f) * 0x80
                        + (id3v2Head[9] & 0x7f);
                id3v2 = new byte[size];
                rf.seek(10);
                rf.read(id3v2);
                id3v2Info = new Id3v2Info(id3v2);
            }

            lengthMesc = 0; //必须重置为0，不然抛出异常时会沿用上次取得的值
            MP3File mp3File = new MP3File(file);
            lengthMesc = mp3File.getMP3AudioHeader().getTrackLength();
        } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            Log.e("Mp3Info", e.toString());
        } finally {
            try {
                if (rf != null) {
                    rf.close();
                }
            } catch (IOException e) {
                Log.e("Mp3Info", e.toString());
            }
        }
    }

    public boolean hasId3v2() {
        String tag = new String(id3v2Head, 0, 3);
        return tag.equals("ID3");
    }

    public boolean hasId3v1() {
        String tag = new String(id3v1, 0, 3);
        return tag.equals("TAG");
    }

    public long getLengthSeconds() {
        return lengthMesc;
    }

    public Id3v1Info getId3v1Info() {
        return id3v1Info;
    }

    public Id3v2Info getId3v2Info() {
        return id3v2Info;
    }

    public void release() {
        id3v1 = null;
        id3v2 = null;
        id3v2Head = null;
        id3v1Info = null;
        id3v2Info = null;
    }
}
