/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import static org.panteleyev.money.backend.WebmoneyApplication.VERSION_ROOT;

@Controller
public class CommonController {
    private static record Version(String name, String version) {
    }


    @Value("${spring.application.version}")
    private String appVersion;
    @Value("${spring.application.name}")
    private String appName;

    @GetMapping(VERSION_ROOT)
    public ResponseEntity<Version> getVersion() {
        return ResponseEntity.ok(new Version(appName, appVersion));
    }
}
