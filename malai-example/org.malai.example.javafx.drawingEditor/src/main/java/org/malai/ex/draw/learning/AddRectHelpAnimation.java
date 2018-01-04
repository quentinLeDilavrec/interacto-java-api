package org.malai.ex.draw.learning;

import javafx.animation.Transition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.malai.javafx.interaction.help.DnDHelpAnimation;

public class AddRectHelpAnimation extends DnDHelpAnimation {
	public AddRectHelpAnimation(final Pane learningPane) {
		super(learningPane, 150, 500, 150, 350, "Press (left button) to start drawing a rectangle",
			"Drag", "Release");
	}

	@Override
	public Transition install() {
		final Transition transition = super.install();
		final Rectangle rec = new Rectangle(150, 150, 1d, 1d);
		pane.getChildren().add(rec);
		rec.toBack();
		rec.setFocusTraversable(false);
		rec.setMouseTransparent(true);
		rec.setFill(Color.LIGHTGRAY);
		rec.setStroke(Color.GRAY);
		rec.widthProperty().bind(ell.centerXProperty().subtract(rec.getX()));
		rec.heightProperty().bind(ell.centerYProperty().subtract(rec.getY()));
		rec.visibleProperty().bind(ell.visibleProperty());
		return transition;
	}
}
