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

import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.malai.fsm.FSM;
import org.malai.javafx.interaction2.JfxInteraction;

public abstract class PointInteraction<F extends FSM<Event>, T> extends JfxInteraction<F, T> {
	/** The pressed local position. */
	protected final ObjectProperty<Point3D> srcLocalPoint;
	/** The pressed scene position. */
	protected final ObjectProperty<Point3D> srcScenePoint;

	/** The button used for the pressure. */
	protected MouseButton button;

	/** The object picked at the pressed position. */
	protected final ObjectProperty<Node> srcObject;

	protected boolean altPressed;

	protected boolean ctrlPressed;

	protected boolean shiftPressed;

	protected boolean metaPressed;

	public PointInteraction(final F fsm) {
		super(fsm);
		srcLocalPoint = new SimpleObjectProperty<>();
		srcScenePoint = new SimpleObjectProperty<>();
		srcObject = new SimpleObjectProperty<>();
	}

	@Override
	public void reinitData() {
		super.reinitData();
		srcLocalPoint.set(null);
		srcScenePoint.set(null);
		srcObject.set(null);
		button = null;
		altPressed = false;
		ctrlPressed = false;
		shiftPressed = false;
		metaPressed = false;
	}

	/**
	 * @return True: the alt key is pressed.
	 */
	public boolean isAltPressed() {
		return altPressed;
	}

	/**
	 * @return True: the control key is pressed.
	 */

	public boolean isCtrlPressed() {
		return ctrlPressed;
	}

	/**
	 * @return True: the shift key is pressed.
	 */

	public boolean isShiftPressed() {
		return shiftPressed;
	}

	/**
	 * @return True: the meta key is pressed.
	 */

	public boolean isMetaPressed() {
		return metaPressed;
	}

	/**
	 * @return The pressed local position.
	 */
	public Point3D getSrcLocalPoint() {
		return srcLocalPoint.get();
	}

	/**
	 * @return The pressed scene position.
	 */
	public Point3D getSrcScenePoint() {
		return srcScenePoint.get();
	}

	/**
	 * @return The button used for the pressure.
	 */
	public MouseButton getButton() {
		return button;
	}

	/**
	 * @return The object picked at the pressed position.
	 */
	public Optional<Node> getSrcObject() {
		return Optional.ofNullable(srcObject.get());
	}

	public ReadOnlyObjectProperty<Point3D> srcLocalPointProperty() {
		return srcLocalPoint;
	}

	public ReadOnlyObjectProperty<Point3D> srcScenePointProperty() {
		return srcScenePoint;
	}

	public ReadOnlyObjectProperty<Node> srcObjectProperty() {
		return srcObject;
	}

	protected void setPointData(final MouseEvent event) {
		srcLocalPoint.set(new Point3D(event.getX(), event.getY(), event.getZ()));
		srcScenePoint.set(new Point3D(event.getSceneX(), event.getSceneY(), event.getZ()));
		button = event.getButton();
		srcObject.set(event.getPickResult().getIntersectedNode());
		altPressed = event.isAltDown();
		shiftPressed = event.isShiftDown();
		ctrlPressed = event.isControlDown();
		metaPressed = event.isMetaDown();
	}
}