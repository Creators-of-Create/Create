package com.simibubi.create.foundation.model;

import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

/**
 * Temporary Create 26.2 porting bridge.
 * TODO 26.2: Replace with BlockStateModel parts.
 */
@Deprecated(forRemoval = true)
public class SimpleBakedModel implements BakedModel {
	private final List<BakedQuad> unculledFaces;
	private final Map<Direction, List<BakedQuad>> culledFaces;
	private final boolean ambientOcclusion;
	private final boolean blockLight;
	private final boolean gui3d;
	private final TextureAtlasSprite particleIcon;
	private final Object transforms;
	private final ItemOverrides overrides;

	public SimpleBakedModel(List<BakedQuad> unculledFaces, Map<Direction, List<BakedQuad>> culledFaces,
		boolean ambientOcclusion, boolean blockLight, boolean gui3d, TextureAtlasSprite particleIcon,
		Object transforms, ItemOverrides overrides) {
		this.unculledFaces = unculledFaces;
		this.culledFaces = culledFaces;
		this.ambientOcclusion = ambientOcclusion;
		this.blockLight = blockLight;
		this.gui3d = gui3d;
		this.particleIcon = particleIcon;
		this.transforms = transforms;
		this.overrides = overrides;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
		return side == null ? unculledFaces : culledFaces.getOrDefault(side, List.of());
	}

	@Override
	public boolean useAmbientOcclusion() {
		return ambientOcclusion;
	}

	@Override
	public boolean usesBlockLight() {
		return blockLight;
	}

	@Override
	public boolean isGui3d() {
		return gui3d;
	}

	@Override
	public TextureAtlasSprite getParticleIcon(ModelData data) {
		return particleIcon;
	}

	@Override
	public Object getTransforms() {
		return transforms;
	}

	@Override
	public ItemOverrides getOverrides() {
		return overrides;
	}
}
