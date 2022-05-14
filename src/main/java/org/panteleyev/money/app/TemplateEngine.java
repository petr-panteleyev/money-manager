/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
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

    private static final String TEMPLATE_PATH  = "/org/panteleyev/money/templates";
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
