package com.jozufozu.flywheel.backend.loading;

import java.util.LinkedList;

public class ShaderTransformer {

	private final LinkedList<IProcessingStage> stages = new LinkedList<>();

	public ShaderTransformer() {
	}

	public ShaderTransformer pushStage(IProcessingStage stage) {
		if (stage != null) {
			stages.addLast(stage);
		}
		return this;
	}

	public ShaderTransformer popStage() {
		stages.removeLast();
		return this;
	}

	public ShaderTransformer prependStage(IProcessingStage stage) {
		if (stage != null) {
			stages.addFirst(stage);
		}
		return this;
	}

	public void transformSource(Shader shader) {

		for (IProcessingStage stage : this.stages) {
			stage.process(shader);
		}
	}

}
