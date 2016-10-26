package com.juja.pairs.controller;

import com.juja.pairs.model.ConnectionParameters;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.List;

import static com.juja.pairs.DbMetadataManager.logAppender;

public abstract class SQLMetadataReader implements AutoCloseable{

    private final static Logger logger = Logger.getLogger(SQLMetadataReader.class);
    static {
        logger.addAppender(logAppender);
    }

    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String COLUMN_SEPARATOR = "|";

    private static final String FIELD_SECTION = "field";
    private static final String INDEX_SECTION = "index";
    private static final String FK_SECTION = "fk";
    private static final String QUERY_SECTION = "query create table";

    Connection connection;
    ConnectionParameters parameters;

    public SQLMetadataReader(ConnectionParameters parameters) {
        this.parameters = parameters;
    }

     public String read() {
        StringBuilder result = new StringBuilder(parameters.getDbTableName()).append(LINE_SEPARATOR);
        try {
            result.append(getTableComment()).append(LINE_SEPARATOR);
            result.append(FIELD_SECTION).append(LINE_SEPARATOR);
            for (String line:getTableColumnsWithDescription()
                    ) {
                result.append(line).append(LINE_SEPARATOR);
            }
            result.append(INDEX_SECTION).append(LINE_SEPARATOR);
            for (String line:getTableIndexesWithDescription()
                    ) {
                result.append(line).append(LINE_SEPARATOR);
            }
            result.append(FK_SECTION).append(LINE_SEPARATOR);
            for (String line: getTableForeignKeysWithDescription()
                    ) {
                result.append(line).append(LINE_SEPARATOR);
            }
            result.append(QUERY_SECTION).append(LINE_SEPARATOR);
            result.append(queryCreateTables());

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return result.toString();
    }

    public abstract String getTableComment();

    public abstract List<String> getTableColumnsWithDescription();

    public abstract List<String> getTableIndexesWithDescription();

    public abstract List<String> getTableForeignKeysWithDescription();

    public abstract String queryCreateTables();
}
