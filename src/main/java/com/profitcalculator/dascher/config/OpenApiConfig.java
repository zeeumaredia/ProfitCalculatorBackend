package com.profitcalculator.dascher.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI config. UI available at <a
 * href="http://localhost:8080/swagger-ui/index.html">Swagger UI</a>
 */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(apiInfo()).servers(List.of(localServer()));
  }

  private Info apiInfo() {
    return new Info()
        .title("DACHSER Profit Calculator API")
        .description(
            "Calculates profit and loss for shipments. "
                + "Full CRUD for shipments with incomes and costs. "
                + "- Seeded IDs: 101, 102, 103, 104. "
                + "- Negative profitOrLoss means loss on that shipment.")
        .version("1.0.0")
        .contact(
            new Contact().name("DACHSER SE — Assessment Project").email("zeeumaredia@gmail.com"))
        .license(new License().name("Private — Interview Assessment Only"));
  }

  private Server localServer() {
    return new Server().url("http://localhost:8080").description("Local development server");
  }
}
