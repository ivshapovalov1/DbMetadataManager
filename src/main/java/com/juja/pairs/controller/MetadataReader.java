package com.juja.pairs.controller;

import java.util.List;

public interface MetadataReader extends AutoCloseable {

    String read();

    String getTableComment();

    List<String> getTableColumnsWithDescription();

    List<String> getTableIndexesWithDescription();

    List<String> getTableForeignKeyWithDescription();

    String queryCreateTables();

}
