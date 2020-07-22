package com.simibubi.create.content.contraptions.processing;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;

import com.simibubi.create.foundation.utility.Lang;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HeaterBlock extends Block implements ITE<HeaterTileEntity> {

	//public static Property<Integer> BLAZE_LEVEL = IntegerProperty.create("blaze_level", 0, 4);
	public static Property<HeatLevel> BLAZE_LEVEL = EnumProperty.create("blaze", HeatLevel.class);

	public HeaterBlock(Properties properties) {
		super(properties);
		setDefaultState(super.getDefaultState().with(BLAZE_LEVEL, HeatLevel.NONE));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(BLAZE_LEVEL);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return state.get(BLAZE_LEVEL).min(HeatLevel.SMOULDERING);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.HEATER.create();
	}

	@Override
	public Class<HeaterTileEntity> getTileEntityClass() {
		return HeaterTileEntity.class;
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
		if (!hasTileEntity(state))
			return ActionResultType.PASS;

		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof HeaterTileEntity))
			return ActionResultType.PASS;

		if (!((HeaterTileEntity) te).tryUpdateFuel(player.getHeldItem(hand), player))
			return ActionResultType.PASS;

		if (!player.isCreative())
			player.getHeldItem(hand).shrink(1);

		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		if (!context.getItem().hasTag())
			return getDefaultState();

		CompoundNBT tag = context.getItem().getTag();
		if (!tag.contains("has_blaze"))
			return getDefaultState();

		if (tag.getBoolean("has_blaze"))
			return getDefaultState().with(BLAZE_LEVEL, HeatLevel.SMOULDERING);

		return getDefaultState();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		return AllShapes.HEATER_BLOCK_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState p_220071_1_, IBlockReader p_220071_2_, BlockPos p_220071_3_, ISelectionContext p_220071_4_) {
		if (p_220071_4_ == ISelectionContext.dummy())
			return AllShapes.HEATER_BLOCK_SPECIAL_COLLISION_SHAPE;

		return super.getShape(p_220071_1_, p_220071_2_, p_220071_3_, p_220071_4_);
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		return MathHelper.clamp(state.get(BLAZE_LEVEL).ordinal() * 4 - 1, 0, 15);
	}

	static void setBlazeLevel(@Nullable World world, BlockPos pos, HeatLevel blazeLevel) {
		if (world != null)
			world.setBlockState(pos, world.getBlockState(pos).with(BLAZE_LEVEL, blazeLevel));
	}

	public static HeatLevel getHeaterLevel(BlockState blockState) {
		return blockState.has(HeaterBlock.BLAZE_LEVEL) ? blockState.get(HeaterBlock.BLAZE_LEVEL) : HeatLevel.NONE;
	}

	public enum HeatLevel implements IStringSerializable {
		NONE,
		SMOULDERING,
		FADING,
		KINDLED,
		SEETHING,
		//if you think you have better names let me know :)
		;

		@Override
		public String getString() {
			return Lang.asId(name());
		}

		public boolean min(HeatLevel heatLevel) {
			return this.ordinal() >= heatLevel.ordinal();
		}
	}
}
