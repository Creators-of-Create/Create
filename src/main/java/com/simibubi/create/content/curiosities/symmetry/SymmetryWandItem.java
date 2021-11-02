package com.simibubi.create.content.curiosities.symmetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock;
import com.simibubi.create.content.curiosities.symmetry.mirror.CrossPlaneMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.EmptyMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.PlaneMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.SymmetryMirror;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
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
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;

public class SymmetryWandItem extends Item {

	public static final String SYMMETRY = "symmetry";
	private static final String ENABLE = "enable";

	public SymmetryWandItem(Properties properties) {
		super(properties.stacksTo(1)
			.rarity(Rarity.UNCOMMON));
	}

	@Nonnull
	@Override
	public ActionResultType useOn(ItemUseContext context) {
		PlayerEntity player = context.getPlayer();
		BlockPos pos = context.getClickedPos();
		if (player == null)
			return ActionResultType.PASS;
		player.getCooldowns()
			.addCooldown(this, 5);
		ItemStack wand = player.getItemInHand(context.getHand());
		checkNBT(wand);

		// Shift -> open GUI
		if (player.isShiftKeyDown()) {
			if (player.level.isClientSide) {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
					openWandGUI(wand, context.getHand());
				});
				player.getCooldowns()
					.addCooldown(this, 5);
			}
			return ActionResultType.SUCCESS;
		}

		if (context.getLevel().isClientSide || context.getHand() != Hand.MAIN_HAND)
			return ActionResultType.SUCCESS;

		CompoundNBT compound = wand.getTag()
			.getCompound(SYMMETRY);
		pos = pos.relative(context.getClickedFace());
		SymmetryMirror previousElement = SymmetryMirror.fromNBT(compound);

		// No Shift -> Make / Move Mirror
		wand.getTag()
			.putBoolean(ENABLE, true);
		Vector3d pos3d = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
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
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
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
				.put(SYMMETRY, new EmptyMirror(new Vector3d(0, 0, 0)).writeToNbt());
			wand.getTag()
				.putBoolean(ENABLE, false);
		}
	}

	public static boolean isEnabled(ItemStack stack) {
		checkNBT(stack);
		CompoundNBT tag = stack.getTag();
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

	public static void apply(World world, ItemStack wand, PlayerEntity player, BlockPos pos, BlockState block) {
		checkNBT(wand);
		if (!isEnabled(wand))
			return;
		if (!BlockItem.BY_BLOCK.containsKey(block.getBlock()))
			return;

		Map<BlockPos, BlockState> blockSet = new HashMap<>();
		blockSet.put(pos, block);
		SymmetryMirror symmetry = SymmetryMirror.fromNBT((CompoundNBT) wand.getTag()
			.getCompound(SYMMETRY));

		Vector3d mirrorPos = symmetry.getPosition();
		if (mirrorPos.distanceTo(Vector3d.atLowerCornerOf(pos)) > AllConfigs.SERVER.curiosities.maxSymmetryWandRange.get())
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

			if (world.isUnobstructed(block, position, ISelectionContext.of(player))) {
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
				if (!toReplace.getMaterial()
						.isReplaceable())
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
				world.setBlock(position, ifluidstate.createLegacyBlock(), BlockFlags.UPDATE_NEIGHBORS);
				world.setBlockAndUpdate(position, blockState);

				CompoundNBT wandNbt = wand.getOrCreateTag();
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

		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
			new SymmetryEffectPacket(to, targets));
	}

	private static boolean isHoldingBlock(PlayerEntity player, BlockState block) {
		ItemStack itemBlock = BlockHelper.getRequiredItem(block);
		return player.getMainHandItem()
			.sameItem(itemBlock)
			|| player.getOffhandItem()
				.sameItem(itemBlock);
	}

	public static void remove(World world, ItemStack wand, PlayerEntity player, BlockPos pos) {
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockState ogBlock = world.getBlockState(pos);
		checkNBT(wand);
		if (!isEnabled(wand))
			return;

		Map<BlockPos, BlockState> blockSet = new HashMap<>();
		blockSet.put(pos, air);
		SymmetryMirror symmetry = SymmetryMirror.fromNBT((CompoundNBT) wand.getTag()
			.getCompound(SYMMETRY));

		Vector3d mirrorPos = symmetry.getPosition();
		if (mirrorPos.distanceTo(Vector3d.atLowerCornerOf(pos)) > AllConfigs.SERVER.curiosities.maxSymmetryWandRange.get())
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
			if (blockstate.getMaterial() != Material.AIR) {
				targets.add(position);
				world.levelEvent(2001, position, Block.getId(blockstate));
				world.setBlock(position, air, 3);

				if (!player.isCreative()) {
					if (!player.getMainHandItem()
						.isEmpty())
						player.getMainHandItem()
							.mineBlock(world, blockstate, position, player);
					TileEntity tileentity = blockstate.hasTileEntity() ? world.getBlockEntity(position) : null;
					Block.dropResources(blockstate, world, pos, tileentity, player, player.getMainHandItem()); // Add fortune, silk touch and other loot modifiers
				}
			}
		}

		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
			new SymmetryEffectPacket(to, targets));
	}

}
