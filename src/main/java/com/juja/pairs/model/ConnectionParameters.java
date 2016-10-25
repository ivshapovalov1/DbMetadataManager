package com.juja.pairs.model;

public class ConnectionParameters {
    private String dbType;
    private String ipHost;
    private String ipPort;
    private String dbName;
    private String dbUser;
    private String dbPassword;
    private String dbTableName;


    public static ConnectionParameters parseFromFile(String fileName) {
        String dbType = "";
        String ipHost = "";
        String ipPort = "";
        String dbName = "";
        String dbUser = "";
        String dbPassword = "";
        String dbTableName = "";
        //TODO parse from file
        return new ConnectionParameters.Builder()
                .addDbType(dbType)
                .addIpHost(ipHost)
                .addIpPort(ipPort)
                .addDbName(dbName)
                .addDbUser(dbUser)
                .addDbPassword(dbPassword)
                .addDbTableName(dbTableName)
                .build();
    }

    private ConnectionParameters(Builder builder) {
        this.dbType = builder.dbType;
        this.ipHost = builder.ipHost;
        this.ipPort = builder.ipPort;
        this.dbName = builder.dbName;
        this.dbUser = builder.dbUser;
        this.dbPassword = builder.dbPassword;
        this.dbTableName = builder.dbTableName;
    }

    public static class Builder {
        private String dbType;
        private String ipHost;
        private String ipPort;
        private String dbName;
        private String dbUser;
        private String dbPassword;
        private String dbTableName;

        public Builder() {

        }

        public Builder addDbType(String dbType) {
            this.dbType = dbType;
            return this;
        }

        public Builder addIpHost(String ipHost) {
            this.ipHost = ipHost;
            return this;
        }

        public Builder addIpPort(String ipPort) {
            this.ipPort = ipPort;
            return this;
        }

        public Builder addDbName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        public Builder addDbUser(String dbUser) {
            this.dbUser = dbUser;
            return this;
        }

        public Builder addDbPassword(String dbPassword) {
            this.dbPassword = dbPassword;
            return this;
        }

        public Builder addDbTableName(String dbTableName) {
            this.dbTableName = dbTableName;
            return this;
        }

        public ConnectionParameters build() {
            ConnectionParameters connectionParameters = new ConnectionParameters(this);
            return connectionParameters;
        }
    }
}