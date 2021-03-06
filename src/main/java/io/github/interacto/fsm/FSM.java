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
package io.github.interacto.fsm;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A finite state machine that defines the behavior of a user interaction.
 * @param <E> The type of events the FSM processes.
 */
public class FSM<E> {
	protected Logger logger;
	protected boolean inner;
	/**
	 * By default an FSM triggers its 'start' event when it leaves its initial state.
	 * In some cases, this is not the case. For example, a double-click interaction is an FSM that must trigger
	 * its start event when the FSM reaches... its terminal state. Similarly, a DnD must trigger its start event
	 * on the first move, not on the first press.
	 * The goal of this attribute is to identify the state of the FSM that must trigger the start event.
	 * By default, this attribute is set with the initial state of the FSM.
	 */
	protected State<E> startingState;
	/** Goes with 'startingState'. It permits to know whether the FSM has started, ie whether the 'starting state' has been reached. */
	protected boolean started;
	protected final InitState<E> initState;
	protected OutputState<E> currentState;
	protected final PublishSubject<Map.Entry<OutputState<E>, OutputState<E>>> currentStatePublisher;
	/** The states that compose the finite state machine. */
	protected final Set<State<E>> states;
	/** The handler that want to be notified when the state machine of the interaction changed. */
	protected final Set<FSMHandler> handlers;
	/**
	 * The events still in process. For example when the user press key ctrl and scroll one time using the wheel of the mouse, the interaction scrolling is
	 * finished but the event keyPressed 'ctrl' is still in process. At the end of the interaction, these events are re-introduced into the
	 * state machine of the interaction for processing.
	 */
	protected final List<E> eventsToProcess;
	/** The current timeout in progress. */
	protected TimeoutTransition<E> currentTimeout;
	protected FSM<E> currentSubFSM;


	public FSM() {
		super();
		eventsToProcess = new ArrayList<>();
		started = false;
		initState = new InitState<>(this, "init");
		states = new HashSet<>();
		states.add(initState);
		startingState = initState;
		currentState = initState;
		currentStatePublisher = PublishSubject.create();
		inner = false;
		handlers = new HashSet<>(2);
	}

	public OutputState<E> getCurrentState() {
		return currentState;
	}

	public Observable<Map.Entry<OutputState<E>, OutputState<E>>> currentState() {
		return currentStatePublisher;
	}

	public void setInner(final boolean inner) {
		this.inner = inner;
	}

	public boolean isInner() {
		return inner;
	}

	public boolean process(final E event) {
		if(event == null) {
			return false;
		}
		if(currentSubFSM != null) {
			return currentSubFSM.process(event);
		}
		return currentState.process(event);
	}

	protected void enterStdState(final StdState<E> state) throws CancelFSMException {
		setCurrentState(state);
		checkTimeoutTransition();
		if(started) {
			onUpdating();
		}
	}

	public boolean isStarted() {
		return started;
	}

	protected void setCurrentState(final OutputState<E> state) {
		final var old = currentState;
		currentState = state;
		currentStatePublisher.onNext(Map.entry(old, currentState));
	}

	/**
	 * At the end of the FSM execution, the events still (eg keyPress) in process must be recycled to be reused in the FSM.
	 */
	protected void processRemainingEvents() {
		synchronized(eventsToProcess) {
			// All the events must be processed but the list stillProcessingEvents can be modified
			// during the process. So, a clone of the list must be created.
			final List<E> list = new ArrayList<>(eventsToProcess);

			// All the events must be processed.
			while(!list.isEmpty()) {
				final E event = list.remove(0);
				// Do not forget to remove the event from its original list.
				eventsToProcess.remove(0);

				if(logger != null) {
					logger.log(Level.INFO, "Recycling event: " + event);
				}

				process(event);
			}
		}
	}

	protected void addRemaningEventsToProcess(final E event) {
		if(event != null) {
			synchronized(eventsToProcess) {
				eventsToProcess.add(event);
			}
		}
	}

	/**
	 * Terminates the state machine.
	 * @throws CancelFSMException If the interaction is cancelled by a handler during the stopping step.
	 */
	protected void onTerminating() throws CancelFSMException {
		if(logger != null) {
			logger.log(Level.INFO, "FSM ended");
		}

		if(started) {
			notifyHandlerOnStop();
		}
		reinit();
		processRemainingEvents();
	}

	/**
	 * Cancels the state machine.
	 * */
	protected void onCancelling() {
		if(logger != null) {
			logger.log(Level.INFO, "FSM cancelled");
		}

		if(started) {
			notifyHandlerOnCancel();
		}
		// When an interaction is aborted, the events in progress must not be reused.
		fullReinit();
	}

	/**
	 * Starts the state machine.
	 * @throws CancelFSMException If the interaction is cancelled by a handler during the starting step.
	 */
	public void onStarting() throws CancelFSMException {
		if(logger != null) {
			logger.log(Level.INFO, "FSM started");
		}

		started = true;
		notifyHandlerOnStart();
	}

	/**
	 * Updates the state machine.
	 * @throws CancelFSMException If the interaction is cancelled by a handler during the updating step.
	 */
	public void onUpdating() throws CancelFSMException {
		if(started) {
			if(logger != null) {
				logger.log(Level.INFO, "FSM updated");
			}

			notifyHandlerOnUpdate();
		}
	}

	/**
	 * Adds a state to the state machine.
	 * @param state The state to add. Must not be null.
	 */
	protected void addState(final InputState<E> state) {
		if(state != null) {
			states.add(state);
		}
	}

	public void log(final boolean log) {
		if(log) {
			if(logger == null) {
				logger = Logger.getLogger(getClass().getName());
			}
		}else {
			logger = null;
		}
	}

	public void reinit() {
		if(logger != null) {
			logger.log(Level.INFO, "FSM reinitialised");
		}

		if(currentTimeout != null) {
			currentTimeout.stopTimeout();
		}

		started = false;
		setCurrentState(initState);
		currentTimeout = null;

		if(currentSubFSM != null) {
			currentSubFSM.reinit();
		}
	}

	public void fullReinit() {
		synchronized(eventsToProcess) {
			eventsToProcess.clear();
		}
		reinit();

		if(currentSubFSM != null) {
			currentSubFSM.fullReinit();
		}
	}

	/**
	 * Jobs to do when a timeout transition is executed.
	 * Because the timeout transition is based on a separated thread, the job
	 * done by this method must be executed in the UI thread.
	 * UI Platforms must override this method to do that.
	 */
	protected void onTimeout() {
		if(currentTimeout != null) {
			if(logger != null) {
				logger.log(Level.INFO, "Timeout");
			}

			try {
				currentTimeout
					.execute(null)
					.filter(state -> state instanceof OutputState<?>)
					.ifPresent(nextState -> {
						setCurrentState((OutputState<E>) nextState);
						checkTimeoutTransition();
					});
			}catch(final CancelFSMException ignored) {
				// Already processed
			}
		}
	}

	/**
	 * Stops the current timeout transition.
	 */
	protected void stopCurrentTimeout() {
		if(currentTimeout != null) {
			if(logger != null) {
				logger.log(Level.INFO, "Timeout stopped");
			}

			currentTimeout.stopTimeout();
			currentTimeout = null;
		}
	}

	/**
	 * Checks whether the current state has a timeout transition.
	 * If it is the case, the timeout transition is launched.
	 */
	protected void checkTimeoutTransition() {
		currentState.getTransitions().stream().filter(tr -> tr instanceof TimeoutTransition).findFirst().map(tr -> (TimeoutTransition<E>) tr).ifPresent(tr -> {
			if(logger != null) {
				logger.log(Level.INFO, "Timeout starting");
			}

			currentTimeout = tr;
			currentTimeout.startTimeout();
		});
	}

	public void addHandler(final FSMHandler handler) {
		if(handler != null) {
			handlers.add(handler);
		}
	}

	public void removeHandler(final FSMHandler handler) {
		if(handler != null) {
			handlers.remove(handler);
		}
	}

	/**
	 * Notifies handler that the interaction starts.
	 * @throws CancelFSMException If the interaction is cancelled by a handler during the starting step.
	 */
	protected void notifyHandlerOnStart() throws CancelFSMException {
		try {
			for(final FSMHandler handler : handlers) {
				handler.fsmStarts();
			}
		}catch(final CancelFSMException ex) {
			onCancelling();
			throw ex;
		}
	}

	/**
	 * Notifies handler that the interaction updates.
	 * @throws CancelFSMException If the interaction is cancelled by a handler during the updating step.
	 */
	protected void notifyHandlerOnUpdate() throws CancelFSMException {
		try {
			for(final FSMHandler handler : handlers) {
				handler.fsmUpdates();
			}
		}catch(final CancelFSMException ex) {
			onCancelling();
			throw ex;
		}
	}

	/**
	 * Notifies handler that the interaction stops.
	 * @throws CancelFSMException If the interaction is cancelled by a handler during the stopping step.
	 */
	protected void notifyHandlerOnStop() throws CancelFSMException {
		try {
			for(final FSMHandler handler : new ArrayList<>(handlers)) {
				handler.fsmStops();
			}
		}catch(final CancelFSMException ex) {
			onCancelling();
			throw ex;
		}
	}

	/**
	 * Notifies handler that the interaction is cancelled.
	 */
	protected void notifyHandlerOnCancel() {
		new ArrayList<>(handlers).forEach(handler -> handler.fsmCancels());
	}

	public Set<State<E>> getStates() {
		return Collections.unmodifiableSet(states);
	}

	public void uninstall() {
		fullReinit();
		logger = null;
		currentStatePublisher.onComplete();
		startingState = null;
		currentSubFSM = null;
		states.forEach(state -> state.uninstall());
		states.clear();
	}
}
