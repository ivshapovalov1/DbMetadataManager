package com.juja.pairs.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class PostgreSQLMetadataReader implements MetadataReader {

    Connection connection;
    //TODO fields & connection

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
