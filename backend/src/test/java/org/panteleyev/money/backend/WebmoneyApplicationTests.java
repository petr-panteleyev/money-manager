/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.panteleyev.money.backend.Profiles.TEST;

@SpringBootTest
@ActiveProfiles(TEST)
class WebmoneyApplicationTests {

	@Test
	void contextLoads() {
	}

}
