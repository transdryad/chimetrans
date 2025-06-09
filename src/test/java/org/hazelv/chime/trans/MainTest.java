// Copyright 2025 Hazel Viswanath <viswanath.hazel@gmail.com>.

// This file is part of Chime-Trans.

// Chime-Trans is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
// License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
// any later version.

// Chime-Trans is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
// the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See LICENSE in the project root for more details.

// You should have received a copy of the GNU General Public License along with Chime-Trans. If not, see <https://www.gnu.org/licenses/>.
package org.hazelv.chime.trans;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private final PrintStream standardOut = System.out;
    private final InputStream standardIn = System.in;
    private final PrintStream standardErr = System.err;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final ByteArrayInputStream inputStreamCaptor = new ByteArrayInputStream(new byte[]{(byte)'H', (byte)'e',
            (byte)'l', (byte)'l', (byte)'o', (byte)' ', (byte)'W', (byte)'o', (byte)'r', (byte)'l', (byte)'d', (byte)'!'});

    @BeforeEach
    public void setUp() {
        System.out.println(System.getProperty("user.dir"));
        System.setOut(new PrintStream(outputStreamCaptor));
        System.setErr(new PrintStream(outputStreamCaptor));
        System.setIn(inputStreamCaptor);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
        System.setIn(standardIn);
        System.setErr(standardErr);
    }

    @Test
    @DisplayName("First")
    public void firstTest() {
        Main.main(new String[]{"--now", "test.chl"});
        assertEquals("Hello\n", outputStreamCaptor.toString());
    }
}