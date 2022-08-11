/*
 Copyright © 2021 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "database")
@ConstructorBinding
public record DatabaseProperties(String userName, String password, String host, int port, String name, String schema) {
}
