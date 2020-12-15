package com.simibubi.create.content.contraptions.processing.burner;

import java.util.Random;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Lang;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.conditions.BlockStateProperty;
import net.minecraft.loot.conditions.ILootCondition.IBuilder;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlazeBurnerBlock extends Block implements ITE<BlazeBurnerTileEntity> {

	public static final Property<HeatLevel> HEAT_LEVEL = EnumProperty.create("blaze", HeatLevel.class);

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
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
		if (world.isRemote)
			return;
		TileEntity tileEntity = world.getTileEntity(pos.up());
		if (!(tileEntity instanceof BasinTileEntity))
			return;
		BasinTileEntity basin = (BasinTileEntity) tileEntity;
		basin.notifyChangeOfContents();
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
		ItemStack heldItem = player.getHeldItem(hand);
		boolean dontConsume = player.isCreative();
		boolean forceOverflow = !(player instanceof FakePlayer);

		if (!state.hasTileEntity()) {
			if (heldItem.getItem() instanceof FlintAndSteelItem) {
				world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F,
					world.rand.nextFloat() * 0.4F + 0.8F);
				if (world.isRemote)
					return ActionResultType.SUCCESS;
				heldItem.damageItem(1, player, p -> p.sendBreakAnimation(hand));
				world.setBlockState(pos, AllBlocks.LIT_BLAZE_BURNER.getDefaultState());
				return ActionResultType.SUCCESS;
			}
			return ActionResultType.PASS;
		}

		ActionResult<ItemStack> res = tryInsert(state, world, pos, dontConsume ? heldItem.copy() : heldItem, forceOverflow, false);
		ItemStack leftover = res.getResult();
		if (!world.isRemote && !dontConsume && !leftover.isEmpty()) {
			if (heldItem.isEmpty()) {
				player.setHeldItem(hand, leftover);
			} else if (!player.inventory.addItemStackToInventory(leftover)) {
				player.dropItem(leftover, false);
			}
		}

		return res.getType() == ActionResultType.SUCCESS ? res.getType() : ActionResultType.PASS;
	}

	public static ActionResult<ItemStack> tryInsert(BlockState state, World world, BlockPos pos, ItemStack stack, boolean forceOverflow,
		boolean simulate) {
		if (!state.hasTileEntity())
			return ActionResult.fail(ItemStack.EMPTY);

		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof BlazeBurnerTileEntity))
			return ActionResult.fail(ItemStack.EMPTY);
		BlazeBurnerTileEntity burnerTE = (BlazeBurnerTileEntity) te;

		if (!burnerTE.tryUpdateFuel(stack, forceOverflow, simulate))
			return ActionResult.fail(ItemStack.EMPTY);
		
		ItemStack container = stack.getContainerItem();
		if (!simulate && !world.isRemote) {
			world.playSound(null, pos, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS,
				.125f + world.rand.nextFloat() * .125f, .75f - world.rand.nextFloat() * .25f);
			stack.shrink(1);
		}
		if (!container.isEmpty()) {
			return ActionResult.success(container);
		}
		return ActionResult.success(ItemStack.EMPTY);
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

	public static HeatLevel getHeatLevelOf(BlockState blockState) {
		return blockState.contains(BlazeBurnerBlock.HEAT_LEVEL) ? blockState.get(BlazeBurnerBlock.HEAT_LEVEL)
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
					.properties(StatePropertiesPredicate.Builder.create()
						.exactMatch(HEAT_LEVEL, level))));
		}
		builder.addLootPool(poolBuilder.rolls(ConstantRange.of(1)));
		return builder;
	}

	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
		if (random.nextInt(10) != 0)
			return;
		if (!state.get(HEAT_LEVEL)
			.isAtLeast(HeatLevel.SMOULDERING))
			return;
		world.playSound((double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F),
			(double) ((float) pos.getZ() + 0.5F), SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS,
			0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
	}

	public enum HeatLevel implements IStringSerializable {
		NONE, SMOULDERING, FADING, KINDLED, SEETHING,;

		public static HeatLevel byIndex(int index) {
			return values()[index];
		}

		@Override
		public String getString() {
			return Lang.asId(name());
		}

		public boolean isAtLeast(HeatLevel heatLevel) {
			return this.ordinal() >= heatLevel.ordinal();
		}
	}
}
