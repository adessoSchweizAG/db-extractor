package ch.adesso.dbextractor.core;

import java.util.LinkedList;
import java.util.List;

public class ForeignKey {

    private final String owner;
    private final String name;
    private final String fkTableName;
    private final List<String> fkColumnNames = new LinkedList<>();
    private final String pkTableName;
    private final List<String> pkColumnNames = new LinkedList<>();

    public ForeignKey(String owner, String name, String fkTableName, String pkTableName) {
        this.owner = owner;
        this.name = name;
        this.fkTableName = fkTableName;
        this.pkTableName = pkTableName;
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

    public List<String> getFkColumnNames() {
        return fkColumnNames;
    }

    public String getPkTableName() {
        return pkTableName;
    }

    public List<String> getPkColumnNames() {
        return pkColumnNames;
    }
}
