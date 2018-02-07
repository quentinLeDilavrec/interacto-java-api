/*
 * This file is part of Malai.
 * Copyright (c) 2009-2018 Arnaud BLOUIN
 * Malai is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 * Malai is distributed without any warranty; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 */
package org.malai.fsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class OutputStateImpl<E> extends StateImpl<E> implements OutputState<E> {
	protected final List<Transition<E>> transitions;

	protected OutputStateImpl(final FSM<E> stateMachine, final String stateName) {
		super(stateMachine, stateName);
		transitions = new ArrayList<>();
	}


	@Override
	public List<Transition<E>> getTransitions() {
		return Collections.unmodifiableList(transitions);
	}

	@Override
	public void addTransition(final Transition<E> tr) {
		if(tr != null) {
			transitions.add(tr);
		}
	}
}
