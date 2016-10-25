package com.juja.pairs.model;

public class ConnectionParameters {

    public static ConnectionParameters parseFromFile(String fileName) {
        //TODO parse from file
        return new ConnectionParameters(new Builder());
    }


    private ConnectionParameters(Builder builder) {

//        this.DbName = builder.DbName;
//
//        this.DbUser = builder.DbUser;

    }

    //TODO builder
    public static class Builder {

        private String DbName;

        private String DBUser;

        public Builder() {


        }

        public Builder addDBName(String DbName) {

            this.DbName = DbName;

            return this;

        }

        public Builder addDBUser(String DBUser) {

            this.DBUser = DBUser;

            return this;

        }

        public ConnectionParameters build() {

            ConnectionParameters connectionParameters = new ConnectionParameters(this);

            return connectionParameters;

        }
    }
}