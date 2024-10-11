package com.simibubi.create.content.equipment.symmetryWand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.mounted.CartAssemblerBlock;
import com.simibubi.create.content.equipment.symmetryWand.mirror.CrossPlaneMirror;
import com.simibubi.create.content.equipment.symmetryWand.mirror.EmptyMirror;
import com.simibubi.create.content.equipment.symmetryWand.mirror.PlaneMirror;
import com.simibubi.create.content.equipment.symmetryWand.mirror.SymmetryMirror;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.PacketDistributor;

public class SymmetryWandItem extends Item {

	public static final String SYMMETRY = "symmetry";
	private static final String ENABLE = "enable";

	public SymmetryWandItem(Properties properties) {
		super(properties);
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		BlockPos pos = context.getClickedPos();
		if (player == null)
			return InteractionResult.PASS;
		player.getCooldowns()
			.addCooldown(this, 5);
		ItemStack wand = player.getItemInHand(context.getHand());
		checkNBT(wand);

		// Shift -> open GUI
		if (player.isShiftKeyDown()) {
			if (player.level().isClientSide) {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
					openWandGUI(wand, context.getHand());
				});
				player.getCooldowns()
					.addCooldown(this, 5);
			}
			return InteractionResult.SUCCESS;
		}

		if (context.getLevel().isClientSide || context.getHand() != InteractionHand.MAIN_HAND)
			return InteractionResult.SUCCESS;

		CompoundTag compound = wand.getTag()
			.getCompound(SYMMETRY);
		pos = pos.relative(context.getClickedFace());
		SymmetryMirror previousElement = SymmetryMirror.fromNBT(compound);

		// No Shift -> Make / Move Mirror
		wand.getTag()
			.putBoolean(ENABLE, true);
		Vec3 pos3d = new Vec3(pos.getX(), pos.getY(), pos.getZ());
		SymmetryMirror newElement = new PlaneMirror(pos3d);

		if (previousElement instanceof EmptyMirror) {
			newElement.setOrientation(
				(player.getDirection() == Direction.NORTH || player.getDirection() == Direction.SOUTH)
					? PlaneMirror.Align.XY.ordinal()
					: PlaneMirror.Align.YZ.ordinal());
			newElement.enable = true;
			wand.getTag()
				.putBoolean(ENABLE, true);

		} else {
			previousElement.setPosition(pos3d);

			if (previousElement instanceof PlaneMirror) {
				previousElement.setOrientation(
					(player.getDirection() == Direction.NORTH || player.getDirection() == Direction.SOUTH)
						? PlaneMirror.Align.XY.ordinal()
						: PlaneMirror.Align.YZ.ordinal());
			}

			if (previousElement instanceof CrossPlaneMirror) {
				float rotation = player.getYHeadRot();
				float abs = Math.abs(rotation % 90);
				boolean diagonal = abs > 22 && abs < 45 + 22;
				previousElement
					.setOrientation(diagonal ? CrossPlaneMirror.Align.D.ordinal() : CrossPlaneMirror.Align.Y.ordinal());
			}

			newElement = previousElement;
		}

		compound = newElement.writeToNbt();
		wand.getTag()
			.put(SYMMETRY, compound);

		player.setItemInHand(context.getHand(), wand);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack wand = playerIn.getItemInHand(handIn);
		checkNBT(wand);

		// Shift -> Open GUI
		if (playerIn.isShiftKeyDown()) {
			if (worldIn.isClientSide) {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
					openWandGUI(playerIn.getItemInHand(handIn), handIn);
				});
				playerIn.getCooldowns()
					.addCooldown(this, 5);
			}
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, wand);
		}

		// No Shift -> Clear Mirror
		wand.getTag()
			.putBoolean(ENABLE, false);
		return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, wand);
	}

	@OnlyIn(Dist.CLIENT)
	private void openWandGUI(ItemStack wand, InteractionHand hand) {
		ScreenOpener.open(new SymmetryWandScreen(wand, hand));
	}

	private static void checkNBT(ItemStack wand) {
		if (!wand.hasTag() || !wand.getTag()
			.contains(SYMMETRY)) {
			wand.setTag(new CompoundTag());
			wand.getTag()
				.put(SYMMETRY, new EmptyMirror(new Vec3(0, 0, 0)).writeToNbt());
			wand.getTag()
				.putBoolean(ENABLE, false);
		}
	}

	public static boolean isEnabled(ItemStack stack) {
		checkNBT(stack);
		CompoundTag tag = stack.getTag();
		return tag.getBoolean(ENABLE) && !tag.getBoolean("Simulate");
	}

	public static SymmetryMirror getMirror(ItemStack stack) {
		checkNBT(stack);
		return SymmetryMirror.fromNBT(stack.getTag()
			.getCompound(SYMMETRY));
	}

	public static void configureSettings(ItemStack stack, SymmetryMirror mirror) {
		checkNBT(stack);
		stack.getTag().put(SYMMETRY, mirror.writeToNbt());
	}

	public static void apply(Level world, ItemStack wand, Player player, BlockPos pos, BlockState block) {
		checkNBT(wand);
		if (!isEnabled(wand))
			return;
		if (!BlockItem.BY_BLOCK.containsKey(block.getBlock()))
			return;

		Map<BlockPos, BlockState> blockSet = new HashMap<>();
		blockSet.put(pos, block);
		SymmetryMirror symmetry = SymmetryMirror.fromNBT((CompoundTag) wand.getTag()
			.getCompound(SYMMETRY));

		Vec3 mirrorPos = symmetry.getPosition();
		if (mirrorPos.distanceTo(Vec3.atLowerCornerOf(pos)) > AllConfigs.server().equipment.maxSymmetryWandRange.get())
			return;
		if (!player.isCreative() && isHoldingBlock(player, block)
			&& BlockHelper.findAndRemoveInInventory(block, player, 1) == 0)
			return;

		symmetry.process(blockSet);
		BlockPos to = BlockPos.containing(mirrorPos);
		List<BlockPos> targets = new ArrayList<>();
		targets.add(pos);

		for (BlockPos position : blockSet.keySet()) {
			if (position.equals(pos))
				continue;

			if (world.isUnobstructed(block, position, CollisionContext.of(player))) {
				BlockState blockState = blockSet.get(position);
				for (Direction face : Iterate.directions)
					blockState = blockState.updateShape(face, world.getBlockState(position.relative(face)), world,
						position, position.relative(face));

				if (player.isCreative()) {
					world.setBlockAndUpdate(position, blockState);
					targets.add(position);
					continue;
				}

				BlockState toReplace = world.getBlockState(position);
				if (!toReplace.canBeReplaced())
					continue;
				if (toReplace.getDestroySpeed(world, position) == -1)
					continue;

				if (AllBlocks.CART_ASSEMBLER.has(blockState)) {
					BlockState railBlock = CartAssemblerBlock.getRailBlock(blockState);
					if (BlockHelper.findAndRemoveInInventory(railBlock, player, 1) == 0)
						continue;
					if (BlockHelper.findAndRemoveInInventory(blockState, player, 1) == 0)
						blockState = railBlock;
				} else {
					if (BlockHelper.findAndRemoveInInventory(blockState, player, 1) == 0)
						continue;
				}

				BlockSnapshot blocksnapshot = BlockSnapshot.create(world.dimension(), world, position);
				FluidState ifluidstate = world.getFluidState(position);
				world.setBlock(position, ifluidstate.createLegacyBlock(), Block.UPDATE_KNOWN_SHAPE);
				world.setBlockAndUpdate(position, blockState);

				CompoundTag wandNbt = wand.getOrCreateTag();
				wandNbt.putBoolean("Simulate", true);
				boolean placeInterrupted = ForgeEventFactory.onBlockPlace(player, blocksnapshot, Direction.UP);
				wandNbt.putBoolean("Simulate", false);

				if (placeInterrupted) {
					blocksnapshot.restore(true, false);
					continue;
				}
				targets.add(position);
			}
		}

		AllPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
			new SymmetryEffectPacket(to, targets));
	}

	private static boolean isHoldingBlock(Player player, BlockState block) {
		ItemStack itemBlock = BlockHelper.getRequiredItem(block);
		return player.isHolding(itemBlock.getItem());
	}

	public static void remove(Level world, ItemStack wand, Player player, BlockPos pos) {
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockState ogBlock = world.getBlockState(pos);
		checkNBT(wand);
		if (!isEnabled(wand))
			return;

		Map<BlockPos, BlockState> blockSet = new HashMap<>();
		blockSet.put(pos, air);
		SymmetryMirror symmetry = SymmetryMirror.fromNBT((CompoundTag) wand.getTag()
			.getCompound(SYMMETRY));

		Vec3 mirrorPos = symmetry.getPosition();
		if (mirrorPos.distanceTo(Vec3.atLowerCornerOf(pos)) > AllConfigs.server().equipment.maxSymmetryWandRange.get())
			return;

		symmetry.process(blockSet);

		BlockPos to = BlockPos.containing(mirrorPos);
		List<BlockPos> targets = new ArrayList<>();

		targets.add(pos);
		for (BlockPos position : blockSet.keySet()) {
			if (!player.isCreative() && ogBlock.getBlock() != world.getBlockState(position)
				.getBlock())
				continue;
			if (position.equals(pos))
				continue;

			BlockState blockstate = world.getBlockState(position);
			if (!blockstate.isAir()) {
				targets.add(position);
				world.levelEvent(2001, position, Block.getId(blockstate));
				world.setBlock(position, air, 3);

				if (!player.isCreative()) {
					if (!player.getMainHandItem()
						.isEmpty())
						player.getMainHandItem()
							.mineBlock(world, blockstate, position, player);
					BlockEntity blockEntity = blockstate.hasBlockEntity() ? world.getBlockEntity(position) : null;
					Block.dropResources(blockstate, world, pos, blockEntity, player, player.getMainHandItem()); // Add fortune, silk touch and other loot modifiers
				}
			}
		}

		AllPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
			new SymmetryEffectPacket(to, targets));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(SimpleCustomRenderer.create(this, new SymmetryWandItemRenderer()));
	}
	
	public static boolean presentInHotbar(Player player) {
		Inventory inv = player.getInventory();
		for (int i = 0; i < Inventory.getSelectionSize(); i++)
			if (AllItems.WAND_OF_SYMMETRY.isIn(inv.getItem(i)))
				return true;
		return false;
	}

}
