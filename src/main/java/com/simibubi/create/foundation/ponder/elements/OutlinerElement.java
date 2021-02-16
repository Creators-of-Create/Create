package com.simibubi.create.foundation.ponder.elements;

import java.util.function.Consumer;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.utility.outliner.Outliner;

public class OutlinerElement extends AnimatedSceneElement {
	
	private Consumer<Outliner> outlinerCall;

	public OutlinerElement(Consumer<Outliner> outlinerCall) {
		this.outlinerCall = outlinerCall;
	}
	
	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (fade.getValue() < 1/16f)
			return;
		if (fade.getValue(0) > fade.getValue(1))
			return;
		outlinerCall.accept(scene.getOutliner());
	}
	
}
