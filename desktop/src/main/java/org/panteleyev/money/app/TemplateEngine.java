/*
 Copyright © 2020-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;

import java.io.Writer;
import java.util.Map;

public final class TemplateEngine {
    public enum Template {
        INCOMES_AND_EXPENSES("IncomesAndExpenses.ftl"),
        MAIN_CSS("mainCss.ftl"),
        DIALOG_CSS("dialogCss.ftl"),
        ABOUT_DIALOG_CSS("aboutDialogCss.ftl");

        private final String fileName;

        Template(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    private static final String TEMPLATE_PATH = "/org/panteleyev/money/templates";
    private static final TemplateEngine ENGINE = new TemplateEngine();

    private final Configuration configuration;

    private TemplateEngine() {
        configuration = new Configuration(Configuration.VERSION_2_3_29);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateLoader(new ClassTemplateLoader(getClass(), TEMPLATE_PATH));
    }

    public static TemplateEngine templateEngine() {
        return ENGINE;
    }

    public void process(Template template, Map<String, ?> model, Writer out) {
        try {
            configuration.getTemplate(template.getFileName()).process(model, out);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
