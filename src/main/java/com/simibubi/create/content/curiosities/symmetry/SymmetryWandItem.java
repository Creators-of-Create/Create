package com.simibubi.create.content.curiosities.symmetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.content.curiosities.symmetry.mirror.CrossPlaneMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.EmptyMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.PlaneMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.SymmetryMirror;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;

public class SymmetryWandItem extends Item {

	public static final String SYMMETRY = "symmetry";
	private static final String ENABLE = "enable";

	public SymmetryWandItem(Properties properties) {
		super(properties.maxStackSize(1)
			.rarity(Rarity.UNCOMMON));
	}

	@Nonnull
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		PlayerEntity player = context.getPlayer();
		BlockPos pos = context.getPos();
		if (player == null)
			return ActionResultType.PASS;
		player.getCooldownTracker()
			.setCooldown(this, 5);
		ItemStack wand = player.getHeldItem(context.getHand());
		checkNBT(wand);

		// Shift -> open GUI
		if (player.isSneaking()) {
			if (player.world.isRemote) {
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
					openWandGUI(wand, context.getHand());
				});
				player.getCooldownTracker()
					.setCooldown(this, 5);
			}
			return ActionResultType.SUCCESS;
		}

		if (context.getWorld().isRemote || context.getHand() != Hand.MAIN_HAND)
			return ActionResultType.SUCCESS;

		CompoundNBT compound = wand.getTag()
			.getCompound(SYMMETRY);
		pos = pos.offset(context.getFace());
		SymmetryMirror previousElement = SymmetryMirror.fromNBT(compound);

		// No Shift -> Make / Move Mirror
		wand.getTag()
			.putBoolean(ENABLE, true);
		Vec3d pos3d = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
		SymmetryMirror newElement = new PlaneMirror(pos3d);

		if (previousElement instanceof EmptyMirror) {
			newElement.setOrientation(
				(player.getHorizontalFacing() == Direction.NORTH || player.getHorizontalFacing() == Direction.SOUTH)
					? PlaneMirror.Align.XY.ordinal()
					: PlaneMirror.Align.YZ.ordinal());
			newElement.enable = true;
			wand.getTag()
				.putBoolean(ENABLE, true);

		} else {
			previousElement.setPosition(pos3d);

			if (previousElement instanceof PlaneMirror) {
				previousElement.setOrientation(
					(player.getHorizontalFacing() == Direction.NORTH || player.getHorizontalFacing() == Direction.SOUTH)
						? PlaneMirror.Align.XY.ordinal()
						: PlaneMirror.Align.YZ.ordinal());
			}

			if (previousElement instanceof CrossPlaneMirror) {
				float rotation = player.getRotationYawHead();
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

		player.setHeldItem(context.getHand(), wand);
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack wand = playerIn.getHeldItem(handIn);
		checkNBT(wand);

		// Shift -> Open GUI
		if (playerIn.isSneaking()) {
			if (worldIn.isRemote) {
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
					openWandGUI(playerIn.getHeldItem(handIn), handIn);
				});
				playerIn.getCooldownTracker()
					.setCooldown(this, 5);
			}
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, wand);
		}

		// No Shift -> Clear Mirror
		wand.getTag()
			.putBoolean(ENABLE, false);
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, wand);
	}

	@OnlyIn(Dist.CLIENT)
	private void openWandGUI(ItemStack wand, Hand hand) {
		ScreenOpener.open(new SymmetryWandScreen(wand, hand));
	}

	private static void checkNBT(ItemStack wand) {
		if (!wand.hasTag() || !wand.getTag()
			.contains(SYMMETRY)) {
			wand.setTag(new CompoundNBT());
			wand.getTag()
				.put(SYMMETRY, new EmptyMirror(new Vec3d(0, 0, 0)).writeToNbt());
			wand.getTag()
				.putBoolean(ENABLE, false);
		}
	}

	public static boolean isEnabled(ItemStack stack) {
		checkNBT(stack);
		return stack.getTag()
			.getBoolean(ENABLE);
	}

	public static SymmetryMirror getMirror(ItemStack stack) {
		checkNBT(stack);
		return SymmetryMirror.fromNBT((CompoundNBT) stack.getTag()
			.getCompound(SYMMETRY));
	}

	public static void apply(World world, ItemStack wand, PlayerEntity player, BlockPos pos, BlockState block) {
		checkNBT(wand);
		if (!isEnabled(wand))
			return;
		if (!BlockItem.BLOCK_TO_ITEM.containsKey(block.getBlock()))
			return;

		Map<BlockPos, BlockState> blockSet = new HashMap<>();
		blockSet.put(pos, block);
		SymmetryMirror symmetry = SymmetryMirror.fromNBT((CompoundNBT) wand.getTag()
			.getCompound(SYMMETRY));

		Vec3d mirrorPos = symmetry.getPosition();
		if (mirrorPos.distanceTo(new Vec3d(pos)) > AllConfigs.SERVER.curiosities.maxSymmetryWandRange.get())
			return;
		if (!player.isCreative() && isHoldingBlock(player, block)
			&& BlockHelper.findAndRemoveInInventory(block, player, 1) == 0)
			return;

		symmetry.process(blockSet);
		BlockPos to = new BlockPos(mirrorPos);
		List<BlockPos> targets = new ArrayList<>();
		targets.add(pos);

		for (BlockPos position : blockSet.keySet()) {
			if (position.equals(pos))
				continue;

			if (world.canPlace(block, position, ISelectionContext.forEntity(player))) {
				BlockState blockState = blockSet.get(position);
				for (Direction face : Direction.values())
					blockState = blockState.updatePostPlacement(face, world.getBlockState(position.offset(face)), world,
						position, position.offset(face));

				if (player.isCreative()) {
					world.setBlockState(position, blockState);
					targets.add(position);
					continue;
				}

				BlockState toReplace = world.getBlockState(position);
				if (!toReplace.getMaterial()
					.isReplaceable())
					continue;
				if (toReplace.getBlockHardness(world, position) == -1)
					continue;
				if (BlockHelper.findAndRemoveInInventory(blockState, player, 1) == 0)
					continue;

				world.setBlockState(position, blockState);
				targets.add(position);
			}
		}

		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
			new SymmetryEffectPacket(to, targets));
	}

	private static boolean isHoldingBlock(PlayerEntity player, BlockState block) {
		ItemStack itemBlock = BlockHelper.getRequiredItem(block);
		return player.getHeldItemMainhand()
			.isItemEqual(itemBlock)
			|| player.getHeldItemOffhand()
				.isItemEqual(itemBlock);
	}

	public static void remove(World world, ItemStack wand, PlayerEntity player, BlockPos pos) {
		BlockState air = Blocks.AIR.getDefaultState();
		BlockState ogBlock = world.getBlockState(pos);
		checkNBT(wand);
		if (!isEnabled(wand))
			return;

		Map<BlockPos, BlockState> blockSet = new HashMap<>();
		blockSet.put(pos, air);
		SymmetryMirror symmetry = SymmetryMirror.fromNBT((CompoundNBT) wand.getTag()
			.getCompound(SYMMETRY));

		Vec3d mirrorPos = symmetry.getPosition();
		if (mirrorPos.distanceTo(new Vec3d(pos)) > AllConfigs.SERVER.curiosities.maxSymmetryWandRange.get())
			return;

		symmetry.process(blockSet);

		BlockPos to = new BlockPos(mirrorPos);
		List<BlockPos> targets = new ArrayList<>();

		targets.add(pos);
		for (BlockPos position : blockSet.keySet()) {
			if (!player.isCreative() && ogBlock.getBlock() != world.getBlockState(position)
				.getBlock())
				continue;
			if (position.equals(pos))
				continue;

			BlockState blockstate = world.getBlockState(position);
			if (!blockstate.isAir(world, position)) {
				targets.add(position);
				world.playEvent(2001, position, Block.getStateId(blockstate));
				world.setBlockState(position, air, 3);

				if (!player.isCreative()) {
					if (!player.getHeldItemMainhand()
						.isEmpty())
						player.getHeldItemMainhand()
							.onBlockDestroyed(world, blockstate, position, player);
					TileEntity tileentity = blockstate.hasTileEntity() ? world.getTileEntity(position) : null;
					Block.spawnDrops(blockstate, world, pos, tileentity, player, player.getHeldItemMainhand()); // Add fortune, silk touch and other loot modifiers
				}
			}
		}

		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
			new SymmetryEffectPacket(to, targets));
	}

}
