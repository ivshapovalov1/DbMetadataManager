package com.juja.pairs.controller;

import com.juja.pairs.model.ConnectionParameters;

import java.sql.Connection;

public class DbMetadataReader {

    Connection connection;
    //TODO fields & connection
    ConnectionParameters parameters;

    public DbMetadataReader(ConnectionParameters parameters) {
        this.parameters = parameters;
    }

}
