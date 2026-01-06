package org.example;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final Dotenv dotenv ;
    private final String URL ;
    private final String USER ;
    private final String PASSWORD ;

    public DBConnection() {
        this.dotenv = Dotenv.load();
        this.URL = "jdbc:postgresql://"+dotenv.get("DB_HOST")+":"+dotenv.get("DB_PORT")+"/"+dotenv.get("DB_NAME");
        this.USER = dotenv.get("DB_USER");
        this.PASSWORD = dotenv.get("DB_PASSWORD");
    }



    public Connection getConnection() {
        try{
            return DriverManager.getConnection(URL,USER,PASSWORD);
        }catch(SQLException e){
            System.out.println("échec de connection :" +  e.getMessage());
            return null;
        }
    }
}