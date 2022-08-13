/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.panteleyev.money.backend.WebmoneyApplication.UI_ROOT;

@Controller
@RequestMapping(UI_ROOT)
public class UiController {
}
