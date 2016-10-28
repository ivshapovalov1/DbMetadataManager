package com.juja.pairs.controller;

import com.juja.pairs.model.ConnectionParameters;

import java.io.IOException;
import java.sql.*;
import java.util.*;

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
                builder.append(resultSet.getString("data_type")).append(COLUMN_SEPARATOR);
                String fieldLength = resultSet.getString("character_maximum_length");
                if ("null".equals(fieldLength)){
                    builder.append(fieldLength).append(COLUMN_SEPARATOR);
                }
                String nullAble = resultSet.getString("is_nullable");
                if ("yes".equalsIgnoreCase(nullAble)){
                    nullAble = "null";
                } else {
                    nullAble = "not null";
                }
                builder.append(nullAble).append(COLUMN_SEPARATOR);
                builder.append(resultSet.getString("column_default")).append(COLUMN_SEPARATOR);
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
        List<String> result = new ArrayList();

        String SQLquerry = "select t.relname as table_name, i.relname as index_name, " +
                "a.attname as column_name from pg_class t, pg_class i, pg_index ix," +
                " pg_attribute a where t.oid = ix.indrelid and i.oid = ix.indexrelid" +
                " and a.attrelid = t.oid and a.attnum = ANY(ix.indkey)" +
                " and t.relkind = 'r' and t.relname = ?" +
                " order by t.relname, i.relname";
        Map<String,HashSet<String>> index = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(SQLquerry)){
            statement.setString(1,parameters.getDbTableName());
            ResultSet set = statement.executeQuery();
            while (set.next()){
                String indexName = set.getString("index_name");
                String columnName = set.getString("column_name");
                HashSet <String> localSet = index.get(indexName);
                if (localSet == null){
                    localSet = new HashSet<>();
                }
                localSet.add(columnName);
                index.put(indexName,localSet);
            }
            for(Map.Entry mapEntry:index.entrySet()){
                StringBuilder builder = new StringBuilder();
                builder.append(mapEntry.getKey()).append(COLUMN_SEPARATOR);
                builder.append("index type").append(COLUMN_SEPARATOR);
                builder.append(mapEntry.getValue().toString().replaceAll("[\\[\\] ]",""));
                result.add(builder.toString());
            }
        }catch (SQLException ex){
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public List<String> getTableForeignKeysWithDescription() {
        return null;
    }

    @Override
    public String queryCreateTables() {
        return null;
    }

    private Map columnsComments(){
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
