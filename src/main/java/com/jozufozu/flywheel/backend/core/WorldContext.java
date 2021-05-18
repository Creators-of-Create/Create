package com.jozufozu.flywheel.backend.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.ResourceUtil;
import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.ShaderLoader;
import com.jozufozu.flywheel.backend.gl.shader.FogSensitiveProgram;
import com.jozufozu.flywheel.backend.gl.shader.IMultiProgram;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;
import com.jozufozu.flywheel.backend.gl.shader.ShaderSpecLoader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.instancing.MaterialSpec;
import com.jozufozu.flywheel.backend.loading.InstancedArraysTemplate;
import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.backend.loading.ProgramTemplate;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.backend.loading.ShaderTransformer;

import net.minecraft.util.ResourceLocation;

public class WorldContext<P extends BasicProgram> extends ShaderContext<P> {

	private static final String declaration = "#flwbuiltins";
	private static final Pattern builtinPattern = Pattern.compile(declaration);

	public static final WorldContext<BasicProgram> INSTANCE = new WorldContext<>(new ResourceLocation("create", "context/world"), new FogSensitiveProgram.SpecLoader<>(BasicProgram::new));
	public static final WorldContext<CrumblingProgram> CRUMBLING = new WorldContext<>(new ResourceLocation("create", "context/crumbling"), new FogSensitiveProgram.SpecLoader<>(CrumblingProgram::new));

	final Map<ShaderType, ResourceLocation> builtins;
	final Map<ShaderType, String> builtinSources;

	protected ProgramTemplate template;
	protected final Supplier<Stream<ProgramSpec>> specStream;
	protected final TemplateFactory templateFactory;

	public WorldContext(ResourceLocation root, ShaderSpecLoader<P> loader) {
		this(root, loader, () -> Backend.allMaterials()
				.stream()
				.map(MaterialSpec::getProgramSpec), InstancedArraysTemplate::new);
	}

	public WorldContext(ResourceLocation root, ShaderSpecLoader<P> loader, Supplier<Stream<ProgramSpec>> specStream, TemplateFactory templateFactory) {
		super(root, loader);
		this.specStream = specStream;
		this.templateFactory = templateFactory;
		builtins = new EnumMap<>(ShaderType.class);
		builtinSources = new EnumMap<>(ShaderType.class);
		builtins.put(ShaderType.FRAGMENT, ResourceUtil.subPath(root, "/builtin.frag"));
		builtins.put(ShaderType.VERTEX, ResourceUtil.subPath(root, "/builtin.vert"));
	}

	@Override
	public void load(ShaderLoader loader) {
		programs.values().forEach(IMultiProgram::delete);
		programs.clear();

		builtins.forEach((type, resourceLocation) -> builtinSources.put(type, loader.getShaderSource(resourceLocation)));

		template = templateFactory.create(loader);
		transformer = new ShaderTransformer()
				.pushStage(this::injectBuiltins)
				.pushStage(loader::processIncludes)
				.pushStage(Shader::parseStructs)
				.pushStage(template)
				.pushStage(loader::processIncludes);

		specStream.get().forEach(spec -> loadProgramFromSpec(loader, spec));
	}

	@Override
	protected void preLink(Program program) {
		template.attachAttributes(program);
	}

	/**
	 * Replace #flwbuiltins with whatever expansion this context provides for the given shader.
	 */
	public void injectBuiltins(Shader shader) {
		Matcher matcher = builtinPattern.matcher(shader.getSource());

		if (matcher.find())
			shader.setSource(matcher.replaceFirst(builtinSources.get(shader.type)));
		else
			throw new RuntimeException(String.format("%s shader '%s' is missing %s, cannot use in World Context", shader.type.name, shader.name, declaration));
	}

	public interface TemplateFactory {
		ProgramTemplate create(ShaderLoader loader);
	}
}
