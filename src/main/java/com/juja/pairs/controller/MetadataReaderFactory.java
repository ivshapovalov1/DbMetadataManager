package com.juja.pairs.controller;

import com.juja.pairs.model.ConnectionParameters;

public class MetadataReaderFactory {
    public static MetadataReader getReader(ConnectionParameters parameters) {
        //TODO определить тип дб и вернуть нужную
        return new PostgreSQLMetadataReader(parameters);
        //return new MySQLMetadataReader();
    }
}
