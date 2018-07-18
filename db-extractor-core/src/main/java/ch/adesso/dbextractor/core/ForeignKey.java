package ch.adesso.dbextractor.core;

public class ForeignKey {

    private final String owner;
    private final String name;
    private final String fkTableName;
    private final String fkColumnName;
    private final String pkTableName;
    private final String pkColumnName;


    public ForeignKey(String owner, String name,
            String fkTableName, String fkColumnName, String pkTableName, String pkColumnName) {
        this.owner = owner;
        this.name = name;
        this.fkTableName = fkTableName;
        this.fkColumnName = fkColumnName;
        this.pkTableName = pkTableName;
        this.pkColumnName = pkColumnName;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getFkTableName() {
        return fkTableName;
    }

    public String getFkColumnName() {
        return fkColumnName;
    }

    public String getPkTableName() {
        return pkTableName;
    }

    public String getPkColumnName() {
        return pkColumnName;
    }

}
