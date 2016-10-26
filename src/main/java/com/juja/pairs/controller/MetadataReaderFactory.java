package com.juja.pairs.controller;

import com.juja.pairs.model.ConnectionParameters;

public class MetadataReaderFactory {
    public static SQLMetadataReader getReader(ConnectionParameters parameters) {

        if (parameters.getDbType().equalsIgnoreCase("MySQL")) {
            return new MySQLMetadataReader(parameters);
        } else if (parameters.getDbType().equalsIgnoreCase("PostgreSQL")) {
            return new PostgreSQLMetadataReader(parameters);
        }
        return null;
    }
}
