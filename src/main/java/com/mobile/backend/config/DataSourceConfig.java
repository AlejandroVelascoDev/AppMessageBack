package com.mobile.backend.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

  @Bean
  @Primary
  public DataSource dataSource(Environment environment) {
    String jdbcUrl = environment.getProperty("SPRING_DATASOURCE_URL");
    String username = environment.getProperty("SPRING_DATASOURCE_USERNAME");
    String password = environment.getProperty("SPRING_DATASOURCE_PASSWORD");
    String databaseUrl = environment.getProperty("DATABASE_URL");

    if (isBlank(jdbcUrl) && !isBlank(databaseUrl)) {
      ConnectionInfo info = ConnectionInfo.from(databaseUrl);
      jdbcUrl = info.jdbcUrl();
      if (isBlank(username)) {
        username = info.username();
      }
      if (isBlank(password)) {
        password = info.password();
      }
    }

    if (isBlank(jdbcUrl)) {
      jdbcUrl = "jdbc:postgresql://localhost:5432/mobile_app_db";
    }
    if (isBlank(username)) {
      username = "postgres";
    }
    if (isBlank(password)) {
      password = "postgres";
    }

    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(jdbcUrl);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.setMaximumPoolSize(5);
    return dataSource;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private record ConnectionInfo(String jdbcUrl, String username, String password) {

    private static ConnectionInfo from(String databaseUrl) {
      URI uri = URI.create(databaseUrl);

      String host = uri.getHost();
      String database = uri.getPath() == null ? "" : uri.getPath();
      if (database.startsWith("/")) {
        database = database.substring(1);
      }

      StringBuilder jdbc = new StringBuilder("jdbc:postgresql://").append(host);
      if (uri.getPort() > 0) {
        jdbc.append(':').append(uri.getPort());
      }
      jdbc.append('/').append(database);
      if (uri.getRawQuery() != null) {
        jdbc.append('?').append(uri.getRawQuery());
      }

      String username = null;
      String password = null;
      String userInfo = uri.getRawUserInfo();
      if (userInfo != null) {
        int separator = userInfo.indexOf(':');
        if (separator >= 0) {
          username = URLDecoder.decode(userInfo.substring(0, separator), StandardCharsets.UTF_8);
          password = URLDecoder.decode(userInfo.substring(separator + 1), StandardCharsets.UTF_8);
        } else {
          username = URLDecoder.decode(userInfo, StandardCharsets.UTF_8);
        }
      }

      return new ConnectionInfo(jdbc.toString(), username, password);
    }
  }
}
