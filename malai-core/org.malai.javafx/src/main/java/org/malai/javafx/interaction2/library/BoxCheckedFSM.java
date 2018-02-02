/*
 * This file is part of Malai.
 * Copyright (c) 2005-2017 Arnaud BLOUIN
 * Malai is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 * Malai is distributed without any warranty; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 */
package org.malai.javafx.interaction2.library;

import javafx.event.Event;
import javafx.scene.control.CheckBox;
import org.malai.fsm.TerminalState;
import org.malai.javafx.interaction2.JfxBoxCheckedTransition;
import org.malai.javafx.interaction2.JfxFSM;
import org.malai.javafx.interaction2.JfxInteraction;

public class BoxCheckedFSM extends JfxFSM<CheckBox> {
	public BoxCheckedFSM() {
		super();
	}

	@Override
	protected void buildFSM(final JfxInteraction<?, CheckBox> interaction) {
		super.buildFSM(interaction);
		final TerminalState<Event> checked = new TerminalState<>(this, "checked");
		addState(checked);
		new JfxBoxCheckedTransition(interaction, initState, checked);
	}
}
