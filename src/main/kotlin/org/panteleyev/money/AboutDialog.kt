/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money

import javafx.geometry.Insets
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import org.panteleyev.utilities.fx.BaseDialog
import java.util.ResourceBundle

class AboutDialog : BaseDialog<Any>(MainWindowController.DIALOGS_CSS) {
    companion object {
        const val APP_TITLE = "Money Manager"
        const val COPYRIGHT = "Copyright (c) 2016, 2017, Petr Panteleyev"
        private val BUILD_INFO = "org.panteleyev.money.res.buildInfo"
    }

    init {
        title = APP_TITLE

        val icon = ImageView(Images.APP_ICON).apply {
            fitWidth = 48.0
            fitHeight = 48.0
        }

        val rb: ResourceBundle = ResourceBundle.getBundle(BUILD_INFO)

        val grid = GridPane().apply {
            styleClass.add(Styles.GRID_PANE)
            addRow(0, Label("Version:"), Label(rb.getString("version")))
            addRow(1, Label("Build:"), Label(rb.getString("timestamp")))
        }

        val vBox = VBox(10.0,
                Label(APP_TITLE).apply { styleClass.add(Styles.ABOUT_APP_TITLE_LABEL) },
                Label(COPYRIGHT).apply { styleClass.add(Styles.ABOUT_LABEL) },
                grid)

        val pane = BorderPane().apply {
            left = icon
            center = vBox
        }

        BorderPane.setMargin(vBox, Insets(0.0, 0.0, 0.0, 10.0))

        dialogPane.content = pane
        dialogPane.buttonTypes.add(ButtonType.OK)
    }
}
