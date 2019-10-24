/*
 * Interacto
 * Copyright (C) 2019 Arnaud Blouin
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
package io.github.interacto.binding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestMustBeUndoableCmdException {
	@Test
	public void testToStringNotNullOnNull() {
		final MustBeUndoableCmdException ex = new MustBeUndoableCmdException(null);
		assertNotNull(ex.toString());
	}

	@Test
	public void testToStringNotNullOnClass() {
		final MustBeUndoableCmdException ex = new MustBeUndoableCmdException(Class.class);
		assertNotNull(ex.toString());
	}
}
