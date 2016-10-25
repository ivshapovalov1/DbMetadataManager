package com.juja.pairs.controller;

import com.juja.pairs.model.ConnectionParameters;

import java.sql.Connection;

public class DbMetadataReader {

    public static final char LINE_SEPARATOR = '\n';
    public static final char COLUMN_SEPARATOR = '|';
    Connection connection;
    //TODO fields & connection
    ConnectionParameters parameters;

    public DbMetadataReader(ConnectionParameters parameters) {
        this.parameters = parameters;
    }

}
