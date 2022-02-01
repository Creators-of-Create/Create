package com.simibubi.create.content.logistics.trains;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ITrackBlock {

	public Vec3 getUpNormal(BlockGetter world, BlockPos pos, BlockState state);

	public Vec3 getTrackAxis(BlockGetter world, BlockPos pos, BlockState state);

	public Vec3 getCurveStart(BlockGetter world, BlockPos pos, BlockState state, Vec3 axis);

	public BlockState getBogeyAnchor(BlockGetter world, BlockPos pos, BlockState state); // should be on bogey side
	
	public boolean trackEquals(BlockState state1, BlockState state2);
	
	@OnlyIn(Dist.CLIENT)
	public PartialModel prepareStationOverlay(BlockGetter world, BlockPos pos, BlockState state,
		AxisDirection direction, PoseStack transform);

	@OnlyIn(Dist.CLIENT)
	public PartialModel prepareAssemblyOverlay(BlockGetter world, BlockPos pos, BlockState state, Direction direction,
		PoseStack ms);

}
