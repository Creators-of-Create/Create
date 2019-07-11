package com.simibubi.create.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.gui.GuiOpener;
import com.simibubi.create.gui.GuiWandSymmetry;
import com.simibubi.create.item.symmetry.SymmetryCrossPlane;
import com.simibubi.create.item.symmetry.SymmetryElement;
import com.simibubi.create.item.symmetry.SymmetryEmptySlot;
import com.simibubi.create.item.symmetry.SymmetryPlane;
import com.simibubi.create.networking.PacketSymmetryEffect;
import com.simibubi.create.networking.Packets;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemWandSymmetry extends Item {

	public static final String $SYMMETRY = "symmetry";
	private static final String $ENABLE = "enable";

	public ItemWandSymmetry(Properties properties) {
		super(properties.maxStackSize(1));
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
		SymmetryElement previousElement = SymmetryElement.fromNBT(compound);

		if (player.isSneaking()) {
			if (!(previousElement instanceof SymmetryEmptySlot))
				wand.getTag().putBoolean($ENABLE, !isEnabled(wand));
			return ActionResultType.SUCCESS;
		}

		wand.getTag().putBoolean($ENABLE, true);
		Vec3d pos3d = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
		SymmetryElement newElement = new SymmetryPlane(pos3d);

		if (previousElement instanceof SymmetryEmptySlot) {
			newElement.setOrientation((player.getHorizontalFacing() == Direction.NORTH
					|| player.getHorizontalFacing() == Direction.SOUTH) ? SymmetryPlane.Align.XY.ordinal()
							: SymmetryPlane.Align.YZ.ordinal());
			newElement.enable = true;
			player.sendStatusMessage(new StringTextComponent(TextFormatting.GREEN + "New Plane created"), true);
			wand.getTag().putBoolean($ENABLE, true);

		} else {
			previousElement.setPosition(pos3d);

			if (previousElement instanceof SymmetryPlane) {
				previousElement.setOrientation((player.getHorizontalFacing() == Direction.NORTH
						|| player.getHorizontalFacing() == Direction.SOUTH) ? SymmetryPlane.Align.XY.ordinal()
								: SymmetryPlane.Align.YZ.ordinal());
			}

			if (previousElement instanceof SymmetryCrossPlane) {
				float rotation = player.getRotationYawHead();
				float abs = Math.abs(rotation % 90);
				boolean diagonal = abs > 22 && abs < 45 + 22;
				previousElement.setOrientation(
						diagonal ? SymmetryCrossPlane.Align.D.ordinal() : SymmetryCrossPlane.Align.Y.ordinal());
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
			GuiOpener.open(new GuiWandSymmetry(playerIn.getHeldItem(handIn)));
			playerIn.getCooldownTracker().setCooldown(this, 5);			
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	private static void checkNBT(ItemStack wand) {
		if (!wand.hasTag() || !wand.getTag().contains($SYMMETRY)) {
			wand.setTag(new CompoundNBT());
			wand.getTag().put($SYMMETRY, new SymmetryEmptySlot(new Vec3d(0, 0, 0)).writeToNbt());
			wand.getTag().putBoolean($ENABLE, false);
		}
	}

	public static boolean isEnabled(ItemStack stack) {
		checkNBT(stack);
		return stack.getTag().getBoolean($ENABLE);
	}

	public static SymmetryElement getMirror(ItemStack stack) {
		checkNBT(stack);
		return SymmetryElement.fromNBT((CompoundNBT) stack.getTag().getCompound($SYMMETRY));
	}

	public static void apply(World world, ItemStack wand, PlayerEntity player, BlockPos pos, BlockState block) {
		checkNBT(wand);
		if (!isEnabled(wand))
			return;

		Map<BlockPos, BlockState> blockSet = new HashMap<>();
		blockSet.put(pos, block);
		SymmetryElement symmetry = SymmetryElement
				.fromNBT((CompoundNBT) wand.getTag().getCompound($SYMMETRY));

		Vec3d mirrorPos = symmetry.getPosition();
		if (mirrorPos.distanceTo(new Vec3d(pos)) > 50)
			return;

		symmetry.process(blockSet);
		
		BlockPos to = new BlockPos(mirrorPos);
		List<BlockPos> targets = new ArrayList<>();

		targets.add(pos);
		for (BlockPos position : blockSet.keySet()) {
			if (world.func_217350_a(block, position, ISelectionContext.forEntity(player))) {
				world.setBlockState(position, blockSet.get(position));
				targets.add(position);				
			}
		}
		
		Packets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new PacketSymmetryEffect(to, targets));
	}

	public static void remove(World world, ItemStack wand, PlayerEntity player, BlockPos pos) {
		BlockState air = Blocks.AIR.getDefaultState();
		checkNBT(wand);
		if (!isEnabled(wand))
			return;

		Map<BlockPos, BlockState> blockSet = new HashMap<>();
		blockSet.put(pos, air);
		SymmetryElement symmetry = SymmetryElement
				.fromNBT((CompoundNBT) wand.getTag().getCompound($SYMMETRY));

		Vec3d mirrorPos = symmetry.getPosition();
		if (mirrorPos.distanceTo(new Vec3d(pos)) > 50)
			return;

		symmetry.process(blockSet);

		BlockPos to = new BlockPos(mirrorPos);
		List<BlockPos> targets = new ArrayList<>();
		
		targets.add(pos);
		for (BlockPos position : blockSet.keySet()) {
			targets.add(position);
			world.setBlockState(position, air);
		}
		
		Packets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new PacketSymmetryEffect(to, targets));
	}
	
}
