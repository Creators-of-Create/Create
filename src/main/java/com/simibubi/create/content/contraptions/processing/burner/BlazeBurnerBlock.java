package com.simibubi.create.content.contraptions.processing.burner;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Lang;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.conditions.BlockStateProperty;
import net.minecraft.world.storage.loot.conditions.ILootCondition.IBuilder;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlazeBurnerBlock extends Block implements ITE<BlazeBurnerTileEntity> {

	public static final IProperty<HeatLevel> HEAT_LEVEL = EnumProperty.create("blaze", HeatLevel.class);

	public BlazeBurnerBlock(Properties properties) {
		super(properties);
		setDefaultState(super.getDefaultState().with(HEAT_LEVEL, HeatLevel.NONE));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(HEAT_LEVEL);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return state.get(HEAT_LEVEL)
			.isAtLeast(HeatLevel.SMOULDERING);
	}

	@Override
	public void fillItemGroup(ItemGroup p_149666_1_, NonNullList<ItemStack> p_149666_2_) {
		p_149666_2_.add(AllItems.EMPTY_BLAZE_BURNER.asStack());
		super.fillItemGroup(p_149666_1_, p_149666_2_);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.HEATER.create();
	}

	@Override
	public Class<BlazeBurnerTileEntity> getTileEntityClass() {
		return BlazeBurnerTileEntity.class;
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult blockRayTraceResult) {
		if (!hasTileEntity(state))
			return ActionResultType.PASS;

		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof BlazeBurnerTileEntity))
			return ActionResultType.PASS;

		if (!((BlazeBurnerTileEntity) te).tryUpdateFuel(player.getHeldItem(hand), player))
			return ActionResultType.PASS;

		if (!player.isCreative())
			player.getHeldItem(hand)
				.shrink(1);

		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		ItemStack stack = context.getItem();
		Item item = stack.getItem();
		BlockState defaultState = getDefaultState();
		if (!(item instanceof BlazeBurnerBlockItem))
			return defaultState;
		HeatLevel initialHeat =
			((BlazeBurnerBlockItem) item).hasCapturedBlaze() ? HeatLevel.SMOULDERING : HeatLevel.NONE;
		return defaultState.with(HEAT_LEVEL, initialHeat);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		return AllShapes.HEATER_BLOCK_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState p_220071_1_, IBlockReader p_220071_2_, BlockPos p_220071_3_,
		ISelectionContext p_220071_4_) {
		if (p_220071_4_ == ISelectionContext.dummy())
			return AllShapes.HEATER_BLOCK_SPECIAL_COLLISION_SHAPE;
		return getShape(p_220071_1_, p_220071_2_, p_220071_3_, p_220071_4_);
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		return MathHelper.clamp(state.get(HEAT_LEVEL)
			.ordinal() * 4 - 1, 0, 15);
	}

	static void setBlazeLevel(World world, BlockPos pos, HeatLevel blazeLevel) {
		BlockState blockState = world.getBlockState(pos);
		if (!(blockState.getBlock() instanceof BlazeBurnerBlock))
			return;
		world.setBlockState(pos, blockState.with(HEAT_LEVEL, blazeLevel));
	}

	public static HeatLevel getHeatLevelOf(BlockState blockState) {
		return blockState.has(BlazeBurnerBlock.HEAT_LEVEL) ? blockState.get(BlazeBurnerBlock.HEAT_LEVEL)
			: HeatLevel.NONE;
	}

	public static LootTable.Builder buildLootTable() {
		IBuilder survivesExplosion = SurvivesExplosion.builder();
		BlazeBurnerBlock block = AllBlocks.BLAZE_BURNER.get();

		LootTable.Builder builder = LootTable.builder();
		LootPool.Builder poolBuilder = LootPool.builder();
		for (HeatLevel level : HeatLevel.values()) {
			IItemProvider drop =
				level == HeatLevel.NONE ? AllItems.EMPTY_BLAZE_BURNER.get() : AllBlocks.BLAZE_BURNER.get();
			poolBuilder.addEntry(ItemLootEntry.builder(drop)
				.acceptCondition(survivesExplosion)
				.acceptCondition(BlockStateProperty.builder(block)
					.func_227567_a_(StatePropertiesPredicate.Builder.create()
						.exactMatch(HEAT_LEVEL, level))));
		}
		builder.addLootPool(poolBuilder.rolls(ConstantRange.of(1)));
		return builder;
	}

	public enum HeatLevel implements IStringSerializable {
		NONE, SMOULDERING, FADING, KINDLED, SEETHING,;

		public static HeatLevel byIndex(int index) {
			return values()[index];
		}

		@Override
		public String getName() {
			return Lang.asId(name());
		}

		public boolean isAtLeast(HeatLevel heatLevel) {
			return this.ordinal() >= heatLevel.ordinal();
		}
	}
}
