/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.panteleyev.money.backend.WebmoneyApplication.VERSION_ROOT;

@OpenAPIDefinition(
        info = @Info(
                title = "Money Manager REST API",
                license = @License(
                        name = "BSD-2-Clause License",
                        url = "https://raw.githubusercontent.com/petr-panteleyev/money-manager/master/LICENSE"
                )
        ))
@Tag(name = "Common API")
@Controller
@RequestMapping("/")
public class CommonController {
    private record DatabaseInfo(String url, String schema, String user) {
    }

    private record Version(String name, String version, DatabaseInfo database) {
    }

    private final String appVersion;
    private final String appName;
    private final String databaseUser;
    private final String databaseUrl;
    private final String schema;

    public CommonController(
            @Value("${spring.application.version}")
            String appVersion,
            @Value("${spring.application.name}")
            String appName,
            @Value("${spring.datasource.username}")
            String databaseUser,
            @Value("${spring.datasource.url}")
            String databaseUrl,
            @Value("${spring.datasource.hikari.schema}")
            String schema
    ) {
        this.appVersion = appVersion;
        this.appName = appName;
        this.databaseUser = databaseUser;
        this.databaseUrl = databaseUrl;
        this.schema = schema;
    }

    @GetMapping(
            value = "/",
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> welcomePage() {
        return ResponseEntity.ok("""
                <html>
                <body>
                <h1>Money Manager Backend</h1>
                <p>
                <a href="/money/swagger-ui/index.html">API Documentation</a>
                </p>
                </body>
                </html>
                """);
    }

    @Operation(summary = "Get application version and database information")
    @GetMapping(VERSION_ROOT)
    public ResponseEntity<Version> getVersion() {
        return ResponseEntity.ok(
                new Version(
                        appName,
                        appVersion,
                        new DatabaseInfo(
                                databaseUrl,
                                schema,
                                databaseUser
                        )
                )
        );
    }
}
