package com.jozufozu.flywheel.backend.loading;

@FunctionalInterface
public interface IProcessingStage {

	void process(Shader shader);
}
