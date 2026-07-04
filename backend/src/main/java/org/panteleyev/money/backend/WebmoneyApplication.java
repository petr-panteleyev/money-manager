// Copyright © 2021-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebmoneyApplication {
	public static final String CONTEXT_ROOT = "/money";
	public static final String API_ROOT = "/api/v1";
	public static final String VERSION_ROOT = "/version";
	public static final String UI_ROOT = "/ui";
	public static final String ACCOUNT_ROOT = API_ROOT + "/accounts";
	public static final String CARD_ROOT = API_ROOT + "/cards";
	public static final String CATEGORY_ROOT = API_ROOT + "/categories";
	public static final String CURRENCY_ROOT = API_ROOT + "/currencies";
	public static final String CONTACT_ROOT = API_ROOT + "/contacts";
	public static final String TRANSACTION_ROOT = API_ROOT + "/transactions";
	public static final String ICON_ROOT = API_ROOT + "/icons";
    public static final String EXCHANGE_SECURITY_ROOT = API_ROOT + "/exchange-securities";
    public static final String EXCHANGE_SECURITY_SPLIT_ROOT = API_ROOT + "/exchange-security-splits";
    public static final String INVESTMENT_DEAL_ROOT = API_ROOT + "/investment-deals";

	public static void main(String[] args) {
		SpringApplication.run(WebmoneyApplication.class, args);
	}

}
