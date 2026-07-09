package com.simibubi.create.foundation.render;

/**
 * Temporary source bridge for legacy blend/logic enum constants.
 * TODO 26.2: Remove once render state is expressed through RenderPipeline/RenderSetup.
 */
@Deprecated(forRemoval = true)
public class GlStateManager {
	public enum SourceFactor {
		SRC_ALPHA
	}

	public enum DestFactor {
		ONE_MINUS_SRC_ALPHA
	}

	public enum LogicOp {
		OR_REVERSE
	}
}
