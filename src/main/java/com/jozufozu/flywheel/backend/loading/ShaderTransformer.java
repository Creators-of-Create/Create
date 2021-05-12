package com.jozufozu.flywheel.backend.loading;

import java.util.LinkedList;

public class ShaderTransformer {

	private final LinkedList<ProcessingStage> stages = new LinkedList<>();

	public ShaderTransformer() {
	}

	public ShaderTransformer pushStage(ProcessingStage stage) {
		if (stage != null) {
			stages.addLast(stage);
		}
		return this;
	}

	public ShaderTransformer prependStage(ProcessingStage stage) {
		if (stage != null) {
			stages.addFirst(stage);
		}
		return this;
	}

	public void transformSource(Shader shader) {

		for (ProcessingStage stage : this.stages) {
			stage.process(shader);
		}
	}

}
