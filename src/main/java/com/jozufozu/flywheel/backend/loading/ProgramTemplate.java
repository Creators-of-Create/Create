package com.jozufozu.flywheel.backend.loading;

import java.util.EnumMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.ShaderLoader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

public class ProgramTemplate implements ProcessingStage {

	protected final ShaderLoader loader;
	protected Map<ShaderType, ShaderTemplate> templates = new EnumMap<>(ShaderType.class);

	public ProgramTemplate(ShaderLoader loader) {
		this.loader = loader;
	}

	@Override
	public void process(Shader shader) {
		ShaderTemplate template = templates.get(shader.type);

		if (template == null) return;

		ParsedShader parsedShader = new ParsedShader(shader.name, shader.getSource());

		shader.setSource(template.apply(parsedShader));
	}
}
