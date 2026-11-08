package com.pixnetics.wallet_backend;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI walletOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Hybrid Payment Wallet Platform API")
                        .description("REST API documentation for the Wallet Platform")
                        .version("1.0.0")
                )
                .tags(List.of(
                        new Tag()
                                .name("Client Operations")
                                .description("Endpoints used by clients to manage their profile, wallets, and funds"),

                        new Tag()
                                .name("Merchant Operations")
                                .description("Endpoints used by merchants to manage their account, terminals, and settlements"),

                        new Tag()
                                .name("Terminal Operations")
                                .description("Endpoints used by payment terminals to initiate payment requests")
                ));
    }
}
