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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A transition that refers to another FSM.
 * Entering this transition starts the underlying sub-FSM.
 * To leave the transition, the sub-FSM must end.
 * @param <E> The type of events the FSM processes.
 */
public class SubFSMTransition<E> extends Transition<E> {
	private final FSM<E> subFSM;
	private final FSMHandler subFSMHandler;

	public SubFSMTransition(final OutputState<E> srcState, final InputState<E> tgtState, final FSM<E> fsm) {
		super(srcState, tgtState);

		if(fsm == null) {
			throw new IllegalArgumentException("sub fsm cannot be null");
		}

		subFSM = fsm;
		subFSM.setInner(true);
		subFSMHandler = new FSMHandler() {
			@Override
			public void fsmStarts() throws CancelFSMException {
				src.exit();
			}

			@Override
			public void fsmUpdates() throws CancelFSMException {
				src.getFSM().setCurrentState(subFSM.getCurrentState());
				src.getFSM().onUpdating();
			}

			@Override
			public void fsmStops() throws CancelFSMException {
				action(null);
				subFSM.removeHandler(subFSMHandler);
				src.getFSM().currentSubFSM = null;
				if(tgt instanceof TerminalState) {
					tgt.enter();
					return;
				}
				if(tgt instanceof CancellingState) {
					fsmCancels();
					return;
				}
				if(tgt instanceof OutputState) {
					src.getFSM().setCurrentState((OutputState<E>) tgt);
					tgt.enter();
				}
			}

			@Override
			public void fsmCancels() {
				subFSM.removeHandler(subFSMHandler);
				src.getFSM().currentSubFSM = null;
				src.getFSM().onCancelling();
			}
		};
	}

	@Override
	public Optional<InputState<E>> execute(final E event) {
		final Optional<Transition<E>> transition = findTransition(event);

		if(transition.isPresent()) {
			src.getFSM().stopCurrentTimeout();
			subFSM.addHandler(subFSMHandler);
			src.getFSM().currentSubFSM = subFSM;
			subFSM.process(event);
			return Optional.of(transition.get().tgt);
		}

		return Optional.empty();
	}

	@Override
	protected boolean accept(final E event) {
		return findTransition(event).isPresent();
	}

	@Override
	protected boolean isGuardOK(final E event) {
		return findTransition(event).filter(tr -> tr.isGuardOK(event)).isPresent();
	}

	private Optional<Transition<E>> findTransition(final E event) {
		return subFSM.initState.transitions
			.stream()
			.filter(tr -> tr.accept(event))
			.findFirst();
	}

	@Override
	public Set<Object> getAcceptedEvents() {
		return subFSM.initState
			.getTransitions()
			.stream()
			.map(tr -> tr.getAcceptedEvents())
			.flatMap(s -> s.stream())
			.collect(Collectors.toSet());
	}

	@Override
	public void uninstall() {
		subFSM.uninstall();
	}
}
