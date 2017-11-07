package jrfeng.player.utils.mp3;


class MP3MergeInfo implements MP3Info {
    private ID3V1Info mID3V1Info;
    private ID3V2Info mID3V2Info;
    private long mLengthMesc;

    MP3MergeInfo(ID3V1Info id3V1Info, ID3V2Info id3V2Info, long lengthMesc) {
        if (id3V1Info != null) {
            mID3V1Info = id3V1Info;
        }
        if (id3V2Info != null) {
            mID3V2Info = id3V2Info;
        }
        mLengthMesc = lengthMesc;
    }

    @Override
    public String getSongName() {
        if (mID3V2Info != null && !mID3V2Info.getSongName().equals("未知")) {
            return mID3V2Info.getSongName();
        }

        if (mID3V1Info != null) {
            return mID3V1Info.getSongName();
        }

        return "未知";
    }

    @Override
    public String getArtist() {
        if (mID3V2Info != null && !mID3V2Info.getArtist().equals("未知")) {
            return mID3V2Info.getArtist();
        }

        if (mID3V1Info != null) {
            return mID3V1Info.getArtist();
        }

        return "未知";
    }

    @Override
    public String getAlbum() {
        if (mID3V2Info != null && !mID3V2Info.getAlbum().equals("未知")) {
            return mID3V2Info.getAlbum();
        }

        if (mID3V1Info != null) {
            return mID3V1Info.getAlbum();
        }

        return "未知";
    }

    @Override
    public String getYear() {
        if (mID3V2Info != null && !mID3V2Info.getYear().equals("未知")) {
            return mID3V2Info.getYear();
        }

        if (mID3V1Info != null) {
            return mID3V1Info.getYear();
        }

        return "未知";
    }

    @Override
    public String getComment() {
        if (mID3V2Info != null && !mID3V2Info.getComment().equals("未知")) {
            return mID3V2Info.getComment();
        }

        if (mID3V1Info != null) {
            return mID3V1Info.getComment();
        }

        return "未知";
    }

    @Override
    public long getLengthMesc() {
        return mLengthMesc;
    }

    @Override
    public boolean hasImage() {
        return mID3V2Info != null && mID3V2Info.hasImage();
    }

    @Override
    public byte[] getImage() {
        if (mID3V2Info != null) {
            return mID3V2Info.getImage();
        } else {
            return null;
        }
    }
}
