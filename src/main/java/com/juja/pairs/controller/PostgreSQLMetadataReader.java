package com.juja.pairs.controller;

import com.juja.pairs.model.ConnectionParameters;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgreSQLMetadataReader extends SQLMetadataReader {
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex){
            ex.printStackTrace();
        }
    }

    public PostgreSQLMetadataReader(ConnectionParameters parameters) {
        super(parameters);
        String dbType = parameters.getDbType().toLowerCase();
        String host = parameters.getIpHost();
        String port = parameters.getIpPort();
        String dbName = parameters.getDbName();
        String url = String.format("jdbc:%s://%s:%s/%s",dbType,host,port,dbName);
        String user = parameters.getDbUser();
        String password = parameters.getDbPassword();

        try {
            connection = DriverManager.getConnection(url,user,password);
        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public String read() {
        return null;
    }

    @Override
    public void close() throws IOException {
        try {
            if (connection!=null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getTableColumnsWithDescription() {
        String SQLquerry = "SELECT * FROM information_schema.columns WHERE" +
                " table_schema = 'public' AND table_name = ?";
        List<String> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQLquerry)){
            statement.setString(1,parameters.getDbTableName());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                StringBuilder builder = new StringBuilder();
                String columnName = resultSet.getString("column_name");
                builder.append(columnName).append(COLUMN_SEPARATOR);
                Map<String,String> map = columnsComments();
                String columnComment = map.get(columnName);
                builder.append(columnComment).append(COLUMN_SEPARATOR);
                result.add(builder.toString());
            }
        }catch (SQLException ex){
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public String getTableComment() {
        String SQLquerry = String
                .format("select obj_description('public.%s'::regclass)",parameters.getDbTableName());
        StringBuilder builder = new StringBuilder();
        try(Statement statement = connection.createStatement())
            {
            ResultSet resultSet = statement.executeQuery(SQLquerry);

            while (resultSet.next()) {
                builder.append(resultSet.getString("obj_description"));
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        return builder.toString();
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

    public Map columnsComments(){
        String SQLquerry = "SELECT c.table_schema,c.table_name,c.column_name,pgd.description\n" +
                "FROM pg_catalog.pg_statio_all_tables as st\n" +
                "  inner join pg_catalog.pg_description pgd on (pgd.objoid=st.relid)\n" +
                "  inner join information_schema.columns c on (pgd.objsubid=c.ordinal_position\n" +
                "    and  c.table_schema=st.schemaname and c.table_name=st.relname)";
        Map <String,String> map = new HashMap<>();
        try (Statement statement = connection.createStatement()){
            ResultSet set = statement.executeQuery(SQLquerry);
            while (set.next()){
                String columnName = set.getString("column_name");
                String columnComment = set.getString("description");
                map.put(columnName,columnComment);
            }
        }catch (SQLException ex){
            ex.printStackTrace();
        }
        return map;
    }
}
