package com.juja.pairs.controller;

import com.juja.pairs.model.ConnectionParameters;

import java.sql.Connection;
import java.util.List;

public abstract class DbMetadataReader implements MetadataReader {

    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String COLUMN_SEPARATOR = "|";

    public static final String FIELD_SECTION = "field";
    public static final String INDEX_SECTION = "index";
    public static final String FK_SECTION = "fk";

    Connection connection;
    ConnectionParameters parameters;

    public DbMetadataReader(ConnectionParameters parameters) {
        this.parameters = parameters;
    }

    @Override
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
            for (String line:getTableForeignKeyWithDescription()
                    ) {
                result.append(line).append(LINE_SEPARATOR);
            }
            result.append("query create table").append(LINE_SEPARATOR);
            result.append(queryCreateTables());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
    @Override
    public abstract String getTableComment();
    @Override
    public abstract List<String> getTableColumnsWithDescription();
    @Override
    public abstract List<String> getTableIndexesWithDescription();
    @Override
    public abstract List<String> getTableForeignKeyWithDescription();
    @Override
    public abstract String queryCreateTables();
}
