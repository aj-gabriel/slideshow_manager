package org.slideshow.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class ReactiveTransactionConfig {

  private final ConnectionFactory connectionFactory;

  public ReactiveTransactionConfig(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Bean
  public R2dbcTransactionManager transactionManager() {
    return new R2dbcTransactionManager(connectionFactory);
  }

  @Bean
  public ReactiveTransactionManager reactiveTransactionManager() {
    return transactionManager();
  }

}
