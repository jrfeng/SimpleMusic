package jrfeng.player.data;

public class MusicListIndex {
    private String name;
    private String tableName;

    public MusicListIndex(String name, String tableName) {
        this.name = name;
        this.tableName = tableName;

        if (!isLegal(tableName)) {
            throw new IllegalArgumentException("table name must start with a-z or A-Z or _");
        }
    }

    public String getName() {
        return name;
    }

    public String getTableName() {
        return tableName;
    }

    //***********private*********

    private boolean isLegal(String tableName) {
        char start = tableName.charAt(0);
        return (start >= 'a' && start <= 'z') || (start >= 'A' && start <= 'Z') || (start == '_');
    }
}
