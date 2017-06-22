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

import java.util.prefs.Preferences

object Options {
    private const val DEFAULT_WIDTH = 1024.0
    private const val DEFAULT_HEIGHT = 768.0
    private const val AUTO_COMPLETE_LENGTH = 3

    private enum class Option (private val s: String) {
        DB_USER("dbUser"),
        DB_PASSWORD("dbPassword"),
        DB_HOST("dbHost"),
        DB_PORT("dbPort"),
        DB_NAME("dbName"),
        DB_AUTO("dbAuto"),

        SHOW_DEACTIVATED_ACCOUNTS("showDeactivatedAccounts"),
        MAIN_WINDOW_WIDTH("mainWindowWidth"),
        MAIN_WINDOW_HEIGHT("mainWindowHeight"),
        AUTO_COMPLETE_LENGTH("autoCompleteLength");

        override fun toString(): String {
            return s
        }
    }

    private val PREFS: Preferences = Preferences.userNodeForPackage(MoneyApplication::class.java)

    // Cached values
    private var autoCompleteLength = AUTO_COMPLETE_LENGTH

    var showDeactivatedAccounts: Boolean
        get() = PREFS.getBoolean(Option.SHOW_DEACTIVATED_ACCOUNTS.toString(), false)
        set(show) = PREFS.putBoolean(Option.SHOW_DEACTIVATED_ACCOUNTS.toString(), show)

    var mainWindowWidth: Double
        get() = PREFS.getDouble(Option.MAIN_WINDOW_WIDTH.toString(), DEFAULT_WIDTH)
        set(x) = PREFS.putDouble(Option.MAIN_WINDOW_WIDTH.toString(), x)

    var mainWindowHeight: Double
        get() = PREFS.getDouble(Option.MAIN_WINDOW_HEIGHT.toString(), DEFAULT_HEIGHT)
        set(x) = PREFS.putDouble(Option.MAIN_WINDOW_HEIGHT.toString(), x)

    fun getAutoCompleteLength(): Int {
        return autoCompleteLength
    }

    fun setAutoCompleteLength(x: Int) {
        autoCompleteLength = x
        PREFS.putInt(Option.AUTO_COMPLETE_LENGTH.toString(), x)
    }

    // DB

    var databaseHost: String
        get() = PREFS.get(Option.DB_HOST.toString(), "localhost")
        set(x) = PREFS.put(Option.DB_HOST.toString(), x)

    var databasePort: Int
        get() = PREFS.getInt(Option.DB_PORT.toString(), 3306)
        set(x) = PREFS.putInt(Option.DB_PORT.toString(), x)

    var databaseUser: String
        get() = PREFS.get(Option.DB_USER.toString(), "")
        set(x) = PREFS.put(Option.DB_USER.toString(), x)

    var databasePassword: String
        get() = PREFS.get(Option.DB_PASSWORD.toString(), "")
        set(x) = PREFS.put(Option.DB_PASSWORD.toString(), x)

    var databaseName: String
        get() = PREFS.get(Option.DB_NAME.toString(), "")
        set(x) = PREFS.put(Option.DB_NAME.toString(), x)

    var autoConnect: Boolean
        get() = PREFS.getBoolean(Option.DB_AUTO.toString(), false)
        set(x) = PREFS.putBoolean(Option.DB_AUTO.toString(), x)

    init {

        // Load values into cache
        autoCompleteLength = PREFS.getInt(Option.AUTO_COMPLETE_LENGTH.toString(), AUTO_COMPLETE_LENGTH)
    }
}
