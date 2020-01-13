package com.simibubi.create.modules.palettes;

import java.util.Arrays;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.IHaveConnectedTextures;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class LayeredCTBlock extends Block implements IHaveConnectedTextures {

	private LayeredCTBehaviour behaviour;

	public LayeredCTBlock(Properties properties, CTSpriteShiftEntry layerShift) {
		this(properties, layerShift, null);
	}

	public LayeredCTBlock(Properties properties, CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
		super(properties);
		behaviour = new LayeredCTBehaviour(layerShift, topShift);
	}

	@Override
	public ConnectedTextureBehaviour getBehaviour() {
		return behaviour;
	}

	private static class LayeredCTBehaviour extends ConnectedTextureBehaviour {

		CTSpriteShiftEntry topShift;
		CTSpriteShiftEntry layerShift;

		public LayeredCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
			this.layerShift = layerShift;
			this.topShift = topShift;
		}

		@Override
		public CTSpriteShiftEntry get(BlockState state, Direction direction) {
			return direction.getAxis().isHorizontal() ? layerShift : topShift;
		}

		@Override
		public Iterable<CTSpriteShiftEntry> getAllCTShifts() {
			if (topShift == null)
				return Arrays.asList(layerShift);
			return Arrays.asList(layerShift, topShift);
		}

	}

}
