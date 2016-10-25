package com.juja.pairs.controller;

import com.juja.pairs.model.ConnectionParameters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class MySQLMetadataReader extends DbMetadataReader implements MetadataReader {

    public MySQLMetadataReader(ConnectionParameters parameters) {
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
}
