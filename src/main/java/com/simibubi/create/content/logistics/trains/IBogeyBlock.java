package com.simibubi.create.content.logistics.trains;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.MaterialManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.logistics.trains.entity.BogeyInstance;
import com.simibubi.create.content.logistics.trains.entity.CarriageBogey;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public interface IBogeyBlock extends IWrenchable {

	static final List<ResourceLocation> BOGEYS = new ArrayList<>();

	public static void register(ResourceLocation block) {
		BOGEYS.add(block);
	}

	public EnumSet<Direction> getStickySurfaces(BlockGetter world, BlockPos pos, BlockState state);

	public double getWheelPointSpacing();

	public double getWheelRadius();

	public boolean allowsSingleBogeyCarriage();

	public Vec3 getConnectorAnchorOffset();

	@OnlyIn(Dist.CLIENT)
	public void render(@Nullable BlockState state, float wheelAngle, PoseStack ms, float partialTicks,
		MultiBufferSource buffers, int light, int overlay);

	@OnlyIn(Dist.CLIENT)
	public BogeyInstance createInstance(MaterialManager materialManager, CarriageBogey bogey);

	public default Direction getBogeyUpDirection() {
		return Direction.UP;
	}

	public boolean isTrackAxisAlongFirstCoordinate(BlockState state);

	@Nullable
	public BlockState getMatchingBogey(Direction upDirection, boolean axisAlongFirst);

	@Override
	default BlockState getRotatedBlockState(BlockState state, Direction targetedFace) {
		Block block = state.getBlock();
		int indexOf = BOGEYS.indexOf(RegisteredObjects.getKeyOrThrow(block));
		if (indexOf == -1)
			return state;

		int index = (indexOf + 1) % BOGEYS.size();
		Direction bogeyUpDirection = getBogeyUpDirection();
		boolean trackAxisAlongFirstCoordinate = isTrackAxisAlongFirstCoordinate(state);

		while (index != indexOf) {
			ResourceLocation id = BOGEYS.get(index);
			Block newBlock = ForgeRegistries.BLOCKS.getValue(id);
			if (newBlock instanceof IBogeyBlock bogey) {
				BlockState matchingBogey = bogey.getMatchingBogey(bogeyUpDirection, trackAxisAlongFirstCoordinate);
				if (matchingBogey != null)
					return matchingBogey.hasProperty(WATERLOGGED)
						? matchingBogey.setValue(WATERLOGGED, state.getValue(WATERLOGGED))
						: matchingBogey;
			}
			index = (index + 1) % BOGEYS.size();
		}

		return state;
	}

}
