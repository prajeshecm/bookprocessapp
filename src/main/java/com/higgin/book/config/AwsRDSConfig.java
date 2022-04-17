package com.higgin.book.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class AwsRDSConfig {


    protected static final Logger LOGGER = LoggerFactory.getLogger(AwsRDSConfig.class);

    protected String driverUrl;
    protected String userName;
    protected String password;
    protected String ddl_auto;

    @Value("$spring.profiles.active")
    protected String activeProfiles;

    AwsRDSConfig(@Value("${spring.datasource.url}") String driverUrl,
                         @Value("${spring.datasource.username}") String userName,
                         @Value("${spring.datasource.password}") String password,
             @Value("${spring.jpa.hibernate.ddl-auto}") String ddl_auto) {
        this.driverUrl = driverUrl;
        this.userName = userName;
        this.password = password;
        this.ddl_auto = ddl_auto;
        toString();
    }


    public String getDriverUrl() {
        return driverUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getDdl_auto() {
        return ddl_auto;
    }

    @Override
    public String toString() {
        return "AwsRDSConfig{" +
                ", driverUrl='" + driverUrl + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", ddl_auto='" + ddl_auto + '\'' +
                ", activeProfiles='" + activeProfiles + '\'' +
                '}';
    }
}
