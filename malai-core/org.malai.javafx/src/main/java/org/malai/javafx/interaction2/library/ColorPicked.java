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

import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import org.malai.javafx.interaction2.JfxInteraction;

/**
 * A user interaction for colour pickers.
 * @author Arnaud BLOUIN
 */
public class ColorPicked extends JfxInteraction<ColorPickedFSM, ColorPicker> {
	/**
	 * Creates the interaction.
	 */
	public ColorPicked() {
		super(new ColorPickedFSM());
		fsm.buildFSM(this);
	}

	@Override
	public void processColorPickerData(final Object colorpicker) {
		if(colorpicker instanceof ColorPicker) {
			widget = (ColorPicker) colorpicker;
		}
	}

	@Override
	protected void onNewNodeRegistered(final Node node) {
		if(node instanceof ColorPicker) {
			registerActionHandler(node);
		}
	}

	@Override
	protected void onNodeUnregistered(final Node node) {
		if(node instanceof ColorPicker) {
			unregisterActionHandler(node);
		}
	}
}
