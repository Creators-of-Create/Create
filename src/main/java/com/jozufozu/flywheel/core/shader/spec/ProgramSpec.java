package com.jozufozu.flywheel.core.shader.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ResourceLocation;

public class ProgramSpec {

	// TODO: Block model style inheritance?
	public static final Codec<ProgramSpec> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					ResourceLocation.CODEC.fieldOf("vert")
							.forGetter(ProgramSpec::getVert),
					ResourceLocation.CODEC.fieldOf("frag")
							.forGetter(ProgramSpec::getFrag),
					ProgramState.CODEC.listOf()
							.optionalFieldOf("states", Collections.emptyList())
							.forGetter(ProgramSpec::getStates)
			).apply(instance, ProgramSpec::new));

	public ResourceLocation name;
	public final ResourceLocation vert;
	public final ResourceLocation frag;

	public final List<ProgramState> states;

	public ProgramSpec(ResourceLocation vert, ResourceLocation frag, List<ProgramState> states) {
		this.vert = vert;
		this.frag = frag;
		this.states = states;
	}

	public ProgramSpec(ResourceLocation name, ResourceLocation vert, ResourceLocation frag) {
		this.name = name;
		this.vert = vert;
		this.frag = frag;
		this.states = new ArrayList<>();
	}

	public void setName(ResourceLocation name) {
		this.name = name;
	}

	public ResourceLocation getVert() {
		return vert;
	}

	public ResourceLocation getFrag() {
		return frag;
	}

	public List<ProgramState> getStates() {
		return states;
	}

}
