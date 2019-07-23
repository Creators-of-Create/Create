package com.simibubi.create.modules.symmetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.KeyboardHelper;
import com.simibubi.create.modules.symmetry.mirror.CrossPlaneMirror;
import com.simibubi.create.modules.symmetry.mirror.EmptyMirror;
import com.simibubi.create.modules.symmetry.mirror.PlaneMirror;
import com.simibubi.create.modules.symmetry.mirror.SymmetryMirror;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;

public class SymmetryWandItem extends Item {

	public static final String $SYMMETRY = "symmetry";
	private static final String $ENABLE = "enable";

	public SymmetryWandItem(Properties properties) {
		super(properties.maxStackSize(1));
	}
	
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (KeyboardHelper.isKeyDown(KeyboardHelper.LSHIFT)) {
			tooltip.add(new StringTextComponent(TextFormatting.GRAY + "Perfectly mirrors your Block placement"));
			tooltip.add(new StringTextComponent(TextFormatting.GRAY + "across the configured planes."));
			tooltip.add(new StringTextComponent(""));
			tooltip.add(new StringTextComponent(TextFormatting.GRAY + "> [Right-Click] on ground to place mirror"));
			tooltip.add(new StringTextComponent(TextFormatting.GRAY + "> [Right-Click] in air to configure mirror"));
			tooltip.add(new StringTextComponent(TextFormatting.GRAY + "> [Shift-Right-Click] to toggle"));
			tooltip.add(new StringTextComponent(""));
			tooltip.add(new StringTextComponent(TextFormatting.DARK_GRAY + "Active while held in the Hotbar"));
			
		} else 
			tooltip.add(new StringTextComponent(TextFormatting.DARK_GRAY + "< Hold Shift >"));
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		PlayerEntity player = context.getPlayer();
		BlockPos pos = context.getPos();
		player.getCooldownTracker().setCooldown(this, 5);

		if (context.getWorld().isRemote || context.getHand() != Hand.MAIN_HAND)
			return ActionResultType.SUCCESS;

		ItemStack wand = player.getHeldItem(context.getHand());
		checkNBT(wand);
		CompoundNBT compound = wand.getTag().getCompound($SYMMETRY);
		pos = pos.offset(context.getFace());
		SymmetryMirror previousElement = SymmetryMirror.fromNBT(compound);

		if (player.isSneaking()) {
			if (!(previousElement instanceof EmptyMirror))
				wand.getTag().putBoolean($ENABLE, !isEnabled(wand));
			return ActionResultType.SUCCESS;
		}

		wand.getTag().putBoolean($ENABLE, true);
		Vec3d pos3d = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
		SymmetryMirror newElement = new PlaneMirror(pos3d);

		if (previousElement instanceof EmptyMirror) {
			newElement.setOrientation(
					(player.getHorizontalFacing() == Direction.NORTH || player.getHorizontalFacing() == Direction.SOUTH)
							? PlaneMirror.Align.XY.ordinal()
							: PlaneMirror.Align.YZ.ordinal());
			newElement.enable = true;
			player.sendStatusMessage(new StringTextComponent(TextFormatting.GREEN + "New Plane created"), true);
			wand.getTag().putBoolean($ENABLE, true);

		} else {
			previousElement.setPosition(pos3d);

			if (previousElement instanceof PlaneMirror) {
				previousElement.setOrientation((player.getHorizontalFacing() == Direction.NORTH
						|| player.getHorizontalFacing() == Direction.SOUTH) ? PlaneMirror.Align.XY.ordinal()
								: PlaneMirror.Align.YZ.ordinal());
			}

			if (previousElement instanceof CrossPlaneMirror) {
				float rotation = player.getRotationYawHead();
				float abs = Math.abs(rotation % 90);
				boolean diagonal = abs > 22 && abs < 45 + 22;
				previousElement.setOrientation(
						diagonal ? CrossPlaneMirror.Align.D.ordinal() : CrossPlaneMirror.Align.Y.ordinal());
			}

			newElement = previousElement;
		}

		compound = newElement.writeToNbt();
		wand.getTag().put($SYMMETRY, compound);

		player.setHeldItem(context.getHand(), wand);
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (worldIn.isRemote) {
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
				openWandGUI(playerIn.getHeldItem(handIn));
			});
			playerIn.getCooldownTracker().setCooldown(this, 5);
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@OnlyIn(Dist.CLIENT)
	private void openWandGUI(ItemStack wand) {
		ScreenOpener.open(new SymmetryWandScreen(wand));
	}

	private static void checkNBT(ItemStack wand) {
		if (!wand.hasTag() || !wand.getTag().contains($SYMMETRY)) {
			wand.setTag(new CompoundNBT());
			wand.getTag().put($SYMMETRY, new EmptyMirror(new Vec3d(0, 0, 0)).writeToNbt());
			wand.getTag().putBoolean($ENABLE, false);
		}
	}

	public static boolean isEnabled(ItemStack stack) {
		checkNBT(stack);
		return stack.getTag().getBoolean($ENABLE);
	}

	public static SymmetryMirror getMirror(ItemStack stack) {
		checkNBT(stack);
		return SymmetryMirror.fromNBT((CompoundNBT) stack.getTag().getCompound($SYMMETRY));
	}

	public static void apply(World world, ItemStack wand, PlayerEntity player, BlockPos pos, BlockState block) {
		checkNBT(wand);
		if (!isEnabled(wand))
			return;
		if (!BlockItem.BLOCK_TO_ITEM.containsKey(block.getBlock()))
			return;

		Map<BlockPos, BlockState> blockSet = new HashMap<>();
		blockSet.put(pos, block);
		SymmetryMirror symmetry = SymmetryMirror.fromNBT((CompoundNBT) wand.getTag().getCompound($SYMMETRY));

		Vec3d mirrorPos = symmetry.getPosition();
		if (mirrorPos.distanceTo(new Vec3d(pos)) > 50)
			return;

		symmetry.process(blockSet);

		BlockPos to = new BlockPos(mirrorPos);
		List<BlockPos> targets = new ArrayList<>();

		targets.add(pos);
		for (BlockPos position : blockSet.keySet()) {
			if (world.func_217350_a(block, position, ISelectionContext.forEntity(player))) {
				Item required = BlockItem.BLOCK_TO_ITEM.get(block.getBlock());

				if (player.isCreative()) {
					world.setBlockState(position, blockSet.get(position));
					targets.add(position);
					continue;
				}
				
				for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
					ItemStack itemstack = player.inventory.getStackInSlot(i);
					if (itemstack.getItem() == required && itemstack.getCount() > 0) {
						player.inventory.setInventorySlotContents(i,
								new ItemStack(itemstack.getItem(), itemstack.getCount() - 1));
						world.setBlockState(position, blockSet.get(position));
						targets.add(position);
						break;
					}
				}
			}
		}

		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
				new SymmetryEffectPacket(to, targets));
	}

	public static void remove(World world, ItemStack wand, PlayerEntity player, BlockPos pos) {
		BlockState air = Blocks.AIR.getDefaultState();
		BlockState ogBlock = world.getBlockState(pos);
		checkNBT(wand);
		if (!isEnabled(wand))
			return;

		Map<BlockPos, BlockState> blockSet = new HashMap<>();
		blockSet.put(pos, air);
		SymmetryMirror symmetry = SymmetryMirror.fromNBT((CompoundNBT) wand.getTag().getCompound($SYMMETRY));

		Vec3d mirrorPos = symmetry.getPosition();
		if (mirrorPos.distanceTo(new Vec3d(pos)) > 50)
			return;

		symmetry.process(blockSet);

		BlockPos to = new BlockPos(mirrorPos);
		List<BlockPos> targets = new ArrayList<>();

		targets.add(pos);
		for (BlockPos position : blockSet.keySet()) {
			if (!player.isCreative() && ogBlock.getBlock() != world.getBlockState(position).getBlock())
				continue;
			if (position.equals(pos))
				continue;

			BlockState blockstate = world.getBlockState(position);
			if (blockstate.isAir(world, position)) {
				continue;
			} else {
				targets.add(position);
				world.playEvent(2001, pos, Block.getStateId(blockstate));
				world.setBlockState(position, air, 3);
				
				if (!player.isCreative()) {
					if (!player.getHeldItemMainhand().isEmpty())
						player.getHeldItemMainhand().onBlockDestroyed(world, blockstate, position, player);
					TileEntity tileentity = blockstate.hasTileEntity() ? world.getTileEntity(position) : null;
					Block.spawnDrops(blockstate, world, pos, tileentity);
				}
			}
		}

		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
				new SymmetryEffectPacket(to, targets));
	}

}
