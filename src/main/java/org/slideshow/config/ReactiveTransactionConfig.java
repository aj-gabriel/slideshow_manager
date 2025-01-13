package org.slideshow.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class ReactiveTransactionConfig {

  private final ConnectionFactory connectionFactory;

  public ReactiveTransactionConfig(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Bean(name = "reactiveTransactionManager")
  public R2dbcTransactionManager reactiveTransactionManager() {
    return new R2dbcTransactionManager(connectionFactory);
  }

}
