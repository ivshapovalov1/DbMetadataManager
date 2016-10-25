package com.juja.pairs.controller;

import com.juja.pairs.model.ConnectionParameters;
import com.mysql.jdbc.Driver;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MySQLMetadataReader extends DbMetadataReader implements MetadataReader {

    private static final String FIELD_SECTION = "field";
    private static final String INDEX_SECTION = "index";
    private static final String FK_SECTION = "fk";


    static {
        try {
            Class.forName("org.mysql.Driver");
        } catch (ClassNotFoundException e) {
            try {
                DriverManager.registerDriver(new Driver());
            } catch (SQLException e1) {
                try {
                    throw new SQLException("Couldn't register driver in case -", e1);
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public MySQLMetadataReader(ConnectionParameters parameters) {
        super(parameters);
        //TODO Connection
        closeOpenedConnection(connection);

        String url = String.format("jdbc:mysql://%s:%s/%s", parameters.getIpHost(), parameters.getIpPort(), parameters.getDbName());
        try {
            connection = DriverManager.getConnection(url, parameters.getDbUser(), parameters.getDbPassword());
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Unable to connect to database '%s', user '%s', password '%s'",
                    parameters.getDbName(), parameters.getDbUser(), parameters.getDbPassword()),
                    e);
        }
    }

    private void closeOpenedConnection(final Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Unable to close connection", e);
            }
        }
    }

    @Override
    public String read() {
        StringBuilder result = new StringBuilder(parameters.getDbTableName()).append(LINE_SEPARATOR);
        try {
            result.append(getTableComment()).append(LINE_SEPARATOR);
            result.append(FIELD_SECTION).append(LINE_SEPARATOR);
            result.append(getTableColumnsWithDescription());
            result.append(INDEX_SECTION).append(LINE_SEPARATOR);
            result.append(getTableIndexesWithDescription());
            result.append(FK_SECTION).append(LINE_SEPARATOR);
            result.append(getTableForeignKeyWithDescription());

            result.append("query create table");
            result.append(fillDB());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private String fillDB() {

        return "";
    }

    private String getTableComment() {

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(String.format("SELECT TABLE_COMMENT FROM INFORMATION_SCHEMA.Tables " +
                             "WHERE table_name = '%s' AND table_schema = '%s'",
                     parameters.getDbTableName(), parameters.getDbName()))) {
            if (rs.next()) {
                return rs.getString("TABLE_COMMENT");
            }
            return "";
        } catch (SQLException e) {
            throw new RuntimeException("It is not possible to obtain the comment of table", e);
        }

    }

    private StringBuilder getTableColumnsWithDescription() {
        StringBuilder columnDescription = new StringBuilder();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(String.format("Select COLUMN_NAME,COLUMN_COMMENT,COLUMN_TYPE,IS_NULLABLE,COLUMN_DEFAULT " +
                             "FROM INFORMATION_SCHEMA.Columns " +
                             "WHERE table_name = '%s' AND table_schema = '%s'",
                     parameters.getDbTableName(), parameters.getDbName()))) {
            while (rs.next()) {
                columnDescription.append(rs.getString("COLUMN_NAME")).append(COLUMN_SEPARATOR);
                columnDescription.append(rs.getString("COLUMN_COMMENT")).append(COLUMN_SEPARATOR);
                String columnFullType = rs.getString("COLUMN_TYPE");
                String columnType = columnFullType.substring(0, columnFullType.indexOf("("));
                String columnSize = columnFullType.substring(columnFullType.indexOf("(") + 1, columnFullType.length() - 1);
                columnDescription.append(columnType).append(COLUMN_SEPARATOR);
                columnDescription.append(columnSize).append(COLUMN_SEPARATOR);
                String nullable = "";
                if (rs.getString("IS_NULLABLE").equalsIgnoreCase("not null")) {
                    nullable = "not null";
                } else {
                    nullable = "null";
                }
                columnDescription.append(nullable).append(COLUMN_SEPARATOR);
                columnDescription.append(rs.getString("COLUMN_DEFAULT"));
                columnDescription.append(LINE_SEPARATOR);
            }
            return columnDescription;
        } catch (SQLException e) {
            throw new RuntimeException("It is not possible to obtain the description of table column", e);
        }

    }

    private StringBuilder getTableIndexesWithDescription() {
        StringBuilder indexDescription = new StringBuilder();
        Map<String, String> indexContent = new HashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(String.format("select INDEX_NAME,INDEX_TYPE,COLUMN_NAME,SEQ_IN_INDEX FROM INFORMATION_SCHEMA.STATISTICS " +
                             "WHERE table_name = '%s' AND table_schema = '%s' order by INDEX_NAME ASC, SEQ_IN_INDEX asc",
                     parameters.getDbTableName(), parameters.getDbName()))) {

            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                String indexField = rs.getString("COLUMN_NAME");

                if (indexContent.containsKey(indexName)) {
                    indexContent.put(indexName, indexContent.get(indexName).concat(",").concat(indexField));
                } else {
                    indexContent.put(indexName, indexField);
                }

            }
            rs.beforeFirst();
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (!indexContent.containsKey(indexName)) {
                    continue;
                }
                indexDescription.append(indexName).append(COLUMN_SEPARATOR);
                indexDescription.append(rs.getString("INDEX_TYPE")).append(COLUMN_SEPARATOR);
                indexDescription.append(indexContent.get(indexName)).append(COLUMN_SEPARATOR);
                indexDescription.append(LINE_SEPARATOR);

                indexContent.remove(indexName);

            }
            return indexDescription;
        } catch (SQLException e) {
            throw new RuntimeException("It is not possible to obtain the description of table column", e);
        }

    }

    private StringBuilder getTableForeignKeyWithDescription() {
        StringBuilder fkDescription = new StringBuilder();
        Map<String, String> foreignKeyContent = new HashMap<>();
        Map<String, String> foreignKeyReferencedContent = new HashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(String.format("" +
                             "SELECT CONSTRAINT_NAME,COLUMN_NAME," +
                             "REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME  FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                             "WHERE table_name = '%s' AND table_schema = '%s' AND CONSTRAINT_NAME<>'PRIMARY'" +
                             "order by CONSTRAINT_NAME,POSITION_IN_UNIQUE_CONSTRAINT",
                     parameters.getDbTableName(), parameters.getDbName()))) {

            while (rs.next()) {
                String fkName = rs.getString("CONSTRAINT_NAME");
                String fkField = rs.getString("COLUMN_NAME");
                String fkReferencedField = rs.getString("REFERENCED_COLUMN_NAME");

                if (foreignKeyContent.containsKey(fkName)) {
                    foreignKeyContent.put(fkName, foreignKeyContent.get(fkName).concat(",").concat(fkField));
                } else {
                    foreignKeyContent.put(fkName, fkField);
                }
                if (foreignKeyReferencedContent.containsKey(fkName)) {
                    foreignKeyReferencedContent.put(fkName, foreignKeyReferencedContent.get(fkName).concat(",").concat(fkReferencedField));
                } else {
                    foreignKeyReferencedContent.put(fkName, fkReferencedField);
                }
            }
            rs.beforeFirst();
            while (rs.next()) {
                String fkName = rs.getString("CONSTRAINT_NAME");
                if (!foreignKeyContent.containsKey(fkName)) {
                    continue;
                }
                fkDescription.append(fkName).append(COLUMN_SEPARATOR);
                fkDescription.append(foreignKeyContent.get(fkName)).append(COLUMN_SEPARATOR);
                fkDescription.append(rs.getString("REFERENCED_TABLE_NAME")).append(COLUMN_SEPARATOR);
                fkDescription.append(foreignKeyReferencedContent.get(fkName)).append(COLUMN_SEPARATOR);
                fkDescription.append(LINE_SEPARATOR);

                foreignKeyContent.remove(fkName);
                foreignKeyReferencedContent.remove(fkName);

            }
            return fkDescription;
        } catch (SQLException e) {
            throw new RuntimeException("It is not possible to obtain the description of table column", e);
        }

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
