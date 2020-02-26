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

import io.github.interacto.command.CmdStub;
import io.github.interacto.command.Command;
import io.github.interacto.command.CommandsRegistry;
import io.github.interacto.error.ErrorCatcher;
import io.github.interacto.fsm.CancelFSMException;
import io.github.interacto.interaction.InteractionData;
import io.github.interacto.interaction.InteractionStub;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class TestWidgetBinding {
	protected WidgetBindingStub binding;
	Disposable errorStream;

	@BeforeEach
	public void setUp() {
		binding = new WidgetBindingStub(false, CmdStub::new, new InteractionStub());
		binding.setActivated(true);
		errorStream = ErrorCatcher.getInstance().getErrors().subscribe(exception -> fail(exception.toString()));
	}

	@AfterEach
	void tearDown() {
		CommandsRegistry.getInstance().clear();
		errorStream.dispose();
	}

	@Test
	void testConstructorInteractionNull() {
		assertThrows(IllegalArgumentException.class, () -> new WidgetBindingStub(false, CmdStub::new, null));
	}

	@Test
	void testConstructorCreatedInteractionNotNull() {
		assertNotNull(binding.getInteraction());
	}

	@Test
	void testConstructorCreatedActionIsNull() {
		assertNull(binding.getCommand());
	}

	@Test
	void testLinkDeActivation() {
		binding.setActivated(true);
		binding.setActivated(false);
		Assertions.assertFalse(binding.isActivated());
	}

	@Test
	void testLinkActivation() {
		binding.setActivated(false);
		binding.setActivated(true);
		Assertions.assertTrue(binding.isActivated());
	}

	@Test
	void testExecuteNope() {
		Assertions.assertFalse(binding.isContinuousCmdExec());
	}

	@Test
	void testExecuteOK() {
		binding = new WidgetBindingStub(true, CmdStub::new, new InteractionStub());
		Assertions.assertTrue(binding.isContinuousCmdExec());
	}

	@Test
	void testExecuteCrash() {
		errorStream.dispose();
		final List<Throwable> errors = new ArrayList<>();
		final IllegalArgumentException ex = new IllegalArgumentException();
		errorStream = ErrorCatcher.getInstance().getErrors().subscribe(errors::add);
		final Supplier<CmdStub> supplier = () -> {
			throw ex;
		};

		binding = new WidgetBindingStub(true, supplier, new InteractionStub());
		assertNull(binding.createCommand());
		assertEquals(1, errors.size());
		assertSame(ex, errors.get(0));
	}

	@Test
	void testIsInteractionMustBeCancelled() {
		assertFalse(binding.isStrictStart());
	}

	@Test
	void testNotRunning() {
		Assertions.assertFalse(binding.isRunning());
	}

	@Test
	void testInteractionCancelsWhenNotStarted() {
		binding.fsmCancels();
	}

	@Test
	void testInteractionUpdatesWhenNotStarted() {
		binding.fsmUpdates();
	}

	@Test
	void testInteractionStopsWhenNotStarted() {
		binding.fsmStops();
	}

	@Test
	void testInteractionStartsWhenNoCorrectInteractionNotActivated() throws CancelFSMException {
		binding.mustCancel = false;
		binding.setActivated(false);
		binding.fsmStarts();
		assertNull(binding.getCommand());
	}

	@Test
	void testInteractionStartsWhenNoCorrectInteractionActivated() throws CancelFSMException {
		binding.mustCancel = false;
		binding.conditionRespected = false;
		binding.fsmStarts();
		assertNull(binding.getCommand());
	}

	@Test
	void testInteractionStartsThrowMustCancelStateMachineException() {
		binding.mustCancel = true;
		assertThrows(CancelFSMException.class, () -> binding.fsmStarts());
	}

	@Test
	void testInteractionStartsOk() throws CancelFSMException {
		binding.conditionRespected = true;
		binding.fsmStarts();
		assertNotNull(binding.getCommand());
	}

	@Test
	void testCounters() {
		assertEquals(0, binding.getTimesEnded());
		assertEquals(0, binding.getTimesCancelled());
	}

	@Test
	void testCounterEndedOnce() throws CancelFSMException {
		binding.conditionRespected = true;
		binding.fsmStarts();
		binding.fsmStops();
		assertEquals(1, binding.getTimesEnded());
		assertEquals(0, binding.getTimesCancelled());
	}

	@Test
	void testCounterEndedTwice() throws CancelFSMException {
		binding.conditionRespected = true;
		binding.fsmStarts();
		binding.fsmStops();
		binding.fsmStarts();
		binding.fsmStops();
		assertEquals(2, binding.getTimesEnded());
		assertEquals(0, binding.getTimesCancelled());
	}

	@Test
	void testCounterCancelledOnce() throws CancelFSMException {
		binding.conditionRespected = true;
		binding.fsmStarts();
		binding.fsmCancels();
		assertEquals(1, binding.getTimesCancelled());
		assertEquals(0, binding.getTimesEnded());
	}

	@Test
	void testCounterCancelledTwice() throws CancelFSMException {
		binding.conditionRespected = true;
		binding.fsmStarts();
		binding.fsmCancels();
		binding.fsmStarts();
		binding.fsmCancels();
		assertEquals(2, binding.getTimesCancelled());
		assertEquals(0, binding.getTimesEnded());
	}

	static class WidgetBindingStub extends WidgetBindingImpl<CmdStub, InteractionStub, InteractionData> {
		public boolean conditionRespected;
		public boolean mustCancel;

		WidgetBindingStub(final boolean continuous, final Supplier<CmdStub> cmdCreation, final InteractionStub interaction) {
			this(continuous, i -> cmdCreation.get(), interaction);
		}

		WidgetBindingStub(final boolean continuous, final Function<InteractionData, CmdStub> cmdCreation, final InteractionStub interaction) {
			super(continuous, cmdCreation, interaction);
			conditionRespected = false;
			mustCancel = false;
		}

		@Override
		public boolean when() {
			return conditionRespected;
		}

		@Override
		public boolean isStrictStart() {
			return mustCancel;
		}

		@Override
		protected void unbindCmdAttributes() {
		}

		@Override
		protected void executeCmdAsync(final Command cmd) {
		}
	}
}

