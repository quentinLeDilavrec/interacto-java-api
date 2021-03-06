/*
 * Interacto
 * Copyright (C) 2020 Arnaud Blouin
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.interacto.command.library;

import io.github.interacto.instrument.Instrument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestActivateInstrument {
	ActivateInstrument cmd;
	Instrument<?> ins;

	@BeforeEach
	void setUp() {
		ins = Mockito.mock(Instrument.class);
		cmd = new ActivateInstrument(ins);
	}

	@Test
	void testNullCons() {
		cmd = new ActivateInstrument(null);
		assertFalse(cmd.canDo());
	}

	@Test
	void testCanDo() {
		assertTrue(cmd.canDo());
	}

	@Test
	void testDo() {
		cmd.doIt();
		Mockito.verify(ins, Mockito.times(1)).setActivated(true);
		Mockito.verify(ins, Mockito.never()).setActivated(false);
	}

	@Test
	void testHadEffect() {
		cmd.doIt();
		cmd.done();
		assertTrue(cmd.hadEffect());
	}
}
