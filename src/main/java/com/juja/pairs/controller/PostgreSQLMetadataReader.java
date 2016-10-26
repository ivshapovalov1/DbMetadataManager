package com.juja.pairs.controller;

import com.juja.pairs.model.ConnectionParameters;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PostgreSQLMetadataReader extends SQLMetadataReader {

    //TODO Все методы
    public PostgreSQLMetadataReader(ConnectionParameters parameters) {
        super(parameters);
    }

    @Override
    public String read() {
        return null;
    }

    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getTableColumnsWithDescription() {
        return null;
    }

    @Override
    public String getTableComment() {
        return null;
    }

    @Override
    public List<String> getTableIndexesWithDescription() {
        return null;
    }

    @Override
    public List<String> getTableForeignKeysWithDescription() {
        return null;
    }

    @Override
    public String queryCreateTables() {
        return null;
    }
}
