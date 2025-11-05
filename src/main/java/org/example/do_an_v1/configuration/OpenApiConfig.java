package org.example.do_an_v1.configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookingHomestayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Booking Homestay API")
                        .description("REST API documentation for Booking Homestay service.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Booking Homestay Dev Team")
                                .email("support@booking-homestay.example"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Guidelines")
                        .url("docs/bussiness_requirements/ba.md"));
    }
}
