package com.simibubi.create.foundation.utility.worldWrappers;

import ca.spottedleaf.starlight.common.light.VariableBlockLightHandler;
import ca.spottedleaf.starlight.common.light.VariableBlockLightHandlerImpl;
import ca.spottedleaf.starlight.common.world.ExtendedWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.lighting.WorldLightManager;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.utility.worldWrappers.chunk.WrappedChunkStarlight;

public class PlacementSimulationWorldStarlight extends PlacementSimulationWorld implements ExtendedWorld {

	VariableBlockLightHandler variableBlockLightHandler = new VariableBlockLightHandlerImpl();

	public PlacementSimulationWorldStarlight(World wrapped) {
		super(wrapped, new WrappedChunkProvider(WrappedChunkStarlight::new));
	}

	@Override
	public Chunk getChunkAtImmediately(int i, int i1) {
		return null;
	}

	@Override
	public IChunk getAnyChunkImmediately(int i, int i1) {
		return null;
	}

	@Override
	public VariableBlockLightHandler getCustomLightHandler() {
		return variableBlockLightHandler;
	}

	@Override
	public void setCustomLightHandler(VariableBlockLightHandler variableBlockLightHandler) {
		this.variableBlockLightHandler = variableBlockLightHandler;
	}

	public static PlacementSimulationWorld setupRenderWorldStarlight(World world, Contraption c) {
		PlacementSimulationWorld renderWorld = new PlacementSimulationWorldStarlight(world);

		renderWorld.setTileEntities(c.presentTileEntities.values());

		for (Template.BlockInfo info : c.getBlocks()
				.values())
			renderWorld.setBlockState(info.pos, info.state);

		WorldLightManager lighter = renderWorld.lighter;

		renderWorld.chunkProvider.getLightSources().forEach((pos) -> lighter.func_215573_a(pos, renderWorld.getLightValue(pos)));

		lighter.tick(Integer.MAX_VALUE, true, false);

		return renderWorld;
	}
}
