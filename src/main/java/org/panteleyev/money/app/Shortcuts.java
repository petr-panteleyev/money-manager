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

import javafx.scene.input.KeyCodeCombination;

import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.DELETE;
import static javafx.scene.input.KeyCode.DIGIT0;
import static javafx.scene.input.KeyCode.DIGIT1;
import static javafx.scene.input.KeyCode.DIGIT2;
import static javafx.scene.input.KeyCode.DIGIT3;
import static javafx.scene.input.KeyCode.DIGIT4;
import static javafx.scene.input.KeyCode.DIGIT5;
import static javafx.scene.input.KeyCode.DIGIT6;
import static javafx.scene.input.KeyCode.DIGIT7;
import static javafx.scene.input.KeyCode.E;
import static javafx.scene.input.KeyCode.F;
import static javafx.scene.input.KeyCode.H;
import static javafx.scene.input.KeyCode.K;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.N;
import static javafx.scene.input.KeyCode.O;
import static javafx.scene.input.KeyCode.R;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.S;
import static javafx.scene.input.KeyCode.T;
import static javafx.scene.input.KeyCode.U;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
import static javafx.scene.input.KeyCombination.SHORTCUT_DOWN;

public interface Shortcuts {
    KeyCodeCombination SHORTCUT_0 = new KeyCodeCombination(DIGIT0, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_1 = new KeyCodeCombination(DIGIT1, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_2 = new KeyCodeCombination(DIGIT2, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_3 = new KeyCodeCombination(DIGIT3, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_4 = new KeyCodeCombination(DIGIT4, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_5 = new KeyCodeCombination(DIGIT5, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_6 = new KeyCodeCombination(DIGIT6, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_7 = new KeyCodeCombination(DIGIT7, SHORTCUT_DOWN);

    KeyCodeCombination SHORTCUT_ALT_UP = new KeyCodeCombination(UP, SHORTCUT_DOWN, ALT_DOWN);
    KeyCodeCombination SHORTCUT_ALT_RIGHT = new KeyCodeCombination(RIGHT, SHORTCUT_DOWN, ALT_DOWN);
    KeyCodeCombination SHORTCUT_ALT_LEFT = new KeyCodeCombination(LEFT, SHORTCUT_DOWN, ALT_DOWN);
    KeyCodeCombination SHORTCUT_ALT_SHIFT_RIGHT = new KeyCodeCombination(RIGHT, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN);
    KeyCodeCombination SHORTCUT_ALT_SHIFT_LEFT = new KeyCodeCombination(LEFT, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN);

    KeyCodeCombination SHORTCUT_C = new KeyCodeCombination(C, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_E = new KeyCodeCombination(E, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_F = new KeyCodeCombination(F, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_H = new KeyCodeCombination(H, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_K = new KeyCodeCombination(K, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_N = new KeyCodeCombination(N, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_O = new KeyCodeCombination(O, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_R = new KeyCodeCombination(R, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_S = new KeyCodeCombination(S, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_T = new KeyCodeCombination(T, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_U = new KeyCodeCombination(U, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_DELETE = new KeyCodeCombination(DELETE, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_ALT_C = new KeyCodeCombination(C, SHORTCUT_DOWN, ALT_DOWN);
}
