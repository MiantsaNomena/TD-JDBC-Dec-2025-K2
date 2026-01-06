package org.example;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        DBConnection db = new DBConnection();
        Connection conn = db.getConnection();

        if (conn != null) {
            System.out.println("réussie");
        } else {
            System.out.println("Échec");
        }
    }
}