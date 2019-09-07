package com.simibubi.create.modules.contraptions.receivers.constructs;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.ITooltip;
import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.TooltipHolder;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalPistonBlock.PistonState;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PistonPoleBlock extends ProperDirectionalBlock implements ITooltip {

	private TooltipHolder info;

	public PistonPoleBlock() {
		super(Properties.from(Blocks.PISTON_HEAD));
		setDefaultState(getDefaultState().with(FACING, Direction.UP));
		info = new TooltipHolder(this);
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		info.addInformation(tooltip);
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		Axis axis = state.get(FACING).getAxis();
		Direction direction = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
		BlockPos pistonHead = null;
		BlockPos pistonBase = null;

		for (int modifier : new int[] { 1, -1 }) {
			for (int offset = modifier; modifier * offset < TranslationConstruct.MAX_EXTENSIONS; offset += modifier) {
				BlockPos currentPos = pos.offset(direction, offset);
				BlockState block = worldIn.getBlockState(currentPos);

				if (AllBlocks.PISTON_POLE.typeOf(block) && axis == block.get(FACING).getAxis())
					continue;

				if ((AllBlocks.MECHANICAL_PISTON.typeOf(block) || AllBlocks.STICKY_MECHANICAL_PISTON.typeOf(block))
						&& block.get(BlockStateProperties.FACING).getAxis() == axis) {
					pistonBase = currentPos;
				}

				if (AllBlocks.MECHANICAL_PISTON_HEAD.typeOf(block)
						&& block.get(BlockStateProperties.FACING).getAxis() == axis) {
					pistonHead = currentPos;
				}

				break;
			}
		}

		if (pistonHead != null && pistonBase != null
				&& worldIn.getBlockState(pistonHead).get(BlockStateProperties.FACING) == worldIn
						.getBlockState(pistonBase).get(BlockStateProperties.FACING)) {

			final BlockPos basePos = pistonBase;
			BlockPos.getAllInBox(pistonBase, pistonHead).filter(p -> !p.equals(pos) && !p.equals(basePos))
					.forEach(p -> worldIn.destroyBlock(p, !player.isCreative()));
			worldIn.setBlockState(basePos,
					worldIn.getBlockState(basePos).with(MechanicalPistonBlock.STATE, PistonState.RETRACTED));
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		switch (state.get(FACING).getAxis()) {
		case X:
			return MechanicalPistonHeadBlock.AXIS_SHAPE_X;
		case Y:
			return MechanicalPistonHeadBlock.AXIS_SHAPE_Y;
		case Z:
			return MechanicalPistonHeadBlock.AXIS_SHAPE_Z;
		}

		return VoxelShapes.empty();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getFace().getOpposite());
	}

	@Override
	public ItemDescription getDescription() {
		Palette color = Palette.Red;
		return new ItemDescription(color).withSummary(
				"Attach to the back of a " + h("Mechanical Piston", color) + " to increase its extension length.")
				.createTabs();
	}

}
