package com.project.artconnect.config;


public final class DatabaseConfig {

    
    public static final String URL =
            "jdbc:mysql://localhost:3306/artconnect_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    public static final String USER     = "root";
    public static final String PASSWORD = "lolo123"; // 

   
    public static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private DatabaseConfig() {}
}
