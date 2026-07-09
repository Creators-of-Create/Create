package com.simibubi.create.content.decoration.palettes;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheredIronWindowCTBehaviour extends ConnectedTextureBehaviour.Base {

	private List<CTSpriteShiftEntry> shifts;

	public WeatheredIronWindowCTBehaviour() {
		this.shifts = List.of(AllSpriteShifts.OLD_FACTORY_WINDOW_1, AllSpriteShifts.OLD_FACTORY_WINDOW_2,
			AllSpriteShifts.OLD_FACTORY_WINDOW_3, AllSpriteShifts.OLD_FACTORY_WINDOW_4);
	}

	@Override
	public @Nullable CTSpriteShiftEntry getShift(BlockState state, RandomSource rand, Direction direction,
		@NotNull TextureAtlasSprite sprite) {
		if (direction.getAxis() == Axis.Y || sprite == null)
			return null;
		CTSpriteShiftEntry entry = shifts.get(rand.nextInt(shifts.size()));
		if (entry.getOriginal() == sprite)
			return entry;
		return super.getShift(state, rand, direction, sprite);
	}

	@Override
	public @Nullable CTSpriteShiftEntry getShift(BlockState state, Direction direction,
		@Nullable TextureAtlasSprite sprite) {
		return null;
	}

	@Override
	public @Nullable CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
		return AllCTTypes.RECTANGLE;
	}

}
