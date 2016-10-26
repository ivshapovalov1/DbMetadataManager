package com.juja.pairs.controller;

import com.juja.pairs.model.ConnectionParameters;
import com.mysql.jdbc.Driver;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySQLMetadataReader extends DbMetadataReader {

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
        String dbType = "MySQL";
        String ipHost = "127.0.0.1";
        String ipPort = "3306";
        String dbName = "test";
        String dbUser = "root";
        String dbPassword = "root";
        String dbTableName = "study";

        parameters=new ConnectionParameters.Builder()
                .addDbType(dbType)
                .addIpHost(ipHost)
                .addIpPort(ipPort)
                .addDbName(dbName)
                .addDbUser(dbUser)
                .addDbPassword(dbPassword)
                .addDbTableName(dbTableName)
                .build();

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

    public String getTableComment() {

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

    public List<String> getTableColumnsWithDescription() {
        List<String> columnsWithDescription = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(String.format("Select COLUMN_NAME,COLUMN_COMMENT,COLUMN_TYPE,IS_NULLABLE,COLUMN_DEFAULT " +
                             "FROM INFORMATION_SCHEMA.Columns " +
                             "WHERE table_name = '%s' AND table_schema = '%s'",
                     parameters.getDbTableName(), parameters.getDbName()))) {
            while (rs.next()) {
                StringBuilder line=new StringBuilder();
                line.append(rs.getString("COLUMN_NAME")).append(COLUMN_SEPARATOR);
                line.append(rs.getString("COLUMN_COMMENT")).append(COLUMN_SEPARATOR);
                String columnFullType = rs.getString("COLUMN_TYPE");
                String columnType = columnFullType.substring(0, columnFullType.indexOf("("));
                String columnSize = columnFullType.substring(columnFullType.indexOf("(") + 1, columnFullType.length() - 1);
                line.append(columnType).append(COLUMN_SEPARATOR);
                line.append(columnSize).append(COLUMN_SEPARATOR);
                String nullable = "";
                if (rs.getString("IS_NULLABLE").equalsIgnoreCase("not null")) {
                    nullable = "not null";
                } else {
                    nullable = "null";
                }
                line.append(nullable).append(COLUMN_SEPARATOR);
                line.append(rs.getString("COLUMN_DEFAULT"));
                columnsWithDescription.add(line.toString());
            }
            return columnsWithDescription;
        } catch (SQLException e) {
            throw new RuntimeException("It is not possible to obtain the description of table column", e);
        }
    }

    public   List<String> getTableIndexesWithDescription() {
        List<String> indexesWithDescription = new ArrayList<>();
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
                StringBuilder line=new StringBuilder();
                String indexName = rs.getString("INDEX_NAME");
                if (!indexContent.containsKey(indexName)) {
                    continue;
                }
                line.append(indexName).append(COLUMN_SEPARATOR);
                line.append(rs.getString("INDEX_TYPE")).append(COLUMN_SEPARATOR);
                line.append(indexContent.get(indexName));
                indexesWithDescription.add(line.toString());

                indexContent.remove(indexName);
            }
            return indexesWithDescription;
        } catch (SQLException e) {
            throw new RuntimeException("It is not possible to obtain the description of table column", e);
        }
    }

    public List<String> getTableForeignKeyWithDescription() {
        List<String> foreignKeysWithDescription = new ArrayList<>();
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
                StringBuilder line=new StringBuilder();
                String fkName = rs.getString("CONSTRAINT_NAME");
                if (!foreignKeyContent.containsKey(fkName)) {
                    continue;
                }
                line.append(fkName).append(COLUMN_SEPARATOR);
                line.append(foreignKeyContent.get(fkName)).append(COLUMN_SEPARATOR);
                line.append(rs.getString("REFERENCED_TABLE_NAME")).append(COLUMN_SEPARATOR);
                line.append(foreignKeyReferencedContent.get(fkName));

                foreignKeysWithDescription.add(line.toString());
                foreignKeyContent.remove(fkName);
                foreignKeyReferencedContent.remove(fkName);
            }
            return foreignKeysWithDescription;
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

    public String queryCreateTables() {

        return "CREATE TABLE `test`.`students` (\n" +
                "  `student_id` INT NOT NULL,\n" +
                "  `student_name` VARCHAR(45) NULL,\n" +
                "  `student age` INT(11) NULL,\n" +
                "  PRIMARY KEY (`student_id`),\n" +
                "  INDEX `student_age` (`student age` DESC),\n" +
                "  INDEX `student_name` (`student_name` ASC),\n" +
                "  INDEX `student_name_age` (`student age` ASC, `student_name` DESC))\n" +
                "COMMENT = 'Table for students';\n" +
                "\n" +
                "CREATE TABLE `test`.`course` (\n" +
                "  `course_id` INT NOT NULL,\n" +
                "  `course_name` VARCHAR(45) NULL,\n" +
                "  `course_duration` INT(11) NULL,\n" +
                "  PRIMARY KEY (`course_id`),\n" +
                "  INDEX `course_name` (`course_name` ASC),\n" +
                "  INDEX `course_name_duration` (`course_name` ASC, `course_duration` DESC))\n" +
                "COMMENT = 'Table for cources';\n" +
                "\n" +
                "\n" +
                "CREATE TABLE `test`.`study` (\n" +
                "  `study_id` INT NOT NULL,\n" +
                "  `student_id` INT(11) NOT NULL DEFAULT 1,\n" +
                "  `course_id` INT(11) NOT NULL DEFAULT 1,\n" +
                "  PRIMARY KEY (`study_id`),\n" +
                "  INDEX `idx_course_student` (`student_id` ASC, `course_id` ASC),\n" +
                "  INDEX `fk_course_id_idx` (`course_id` ASC),\n" +
                "  CONSTRAINT `fk_student_id`\n" +
                "    FOREIGN KEY (`student_id`)\n" +
                "    REFERENCES `test`.`students` (`student_id`)\n" +
                "    ON DELETE NO ACTION\n" +
                "    ON UPDATE NO ACTION,\n" +
                "  CONSTRAINT `fk_course_id`\n" +
                "    FOREIGN KEY (`course_id`)\n" +
                "    REFERENCES `test`.`course` (`course_id`)\n" +
                "    ON DELETE NO ACTION\n" +
                "    ON UPDATE NO ACTION)\n" +
                "COMMENT = 'Table for study';\n";
    }
}
