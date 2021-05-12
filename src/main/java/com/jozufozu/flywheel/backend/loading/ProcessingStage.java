package com.jozufozu.flywheel.backend.loading;

@FunctionalInterface
public interface ProcessingStage {

	void process(Shader shader);
}
