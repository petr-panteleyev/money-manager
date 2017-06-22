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

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.stage.Stage
import java.util.logging.Level

class MoneyApplication : Application() {
    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        MoneyApplication.application = this

        Thread.setDefaultUncaughtExceptionHandler({ _, e -> uncaughtException(e) })

        MainWindowController(primaryStage)

        primaryStage.show()
    }

    companion object {
        var application : MoneyApplication? = null

        fun uncaughtException(e: Throwable) {
            Logging.logger.log(Level.SEVERE, "Uncaught exception", e)
            Platform.runLater {
                val alert = Alert(Alert.AlertType.ERROR, e.toString())
                alert.showAndWait()
            }
        }
    }
}

fun main(args: Array<String>) {
    Application.launch(MoneyApplication::class.java, *args)
}