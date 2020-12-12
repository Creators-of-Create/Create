package com.simibubi.create.content.curiosities.zapper;

import java.util.List;

import javax.annotation.Nonnull;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;

public abstract class ZapperItem extends Item {

	public ZapperItem(Properties properties) {
		super(properties.maxStackSize(1)
			.rarity(Rarity.UNCOMMON));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTag() && stack.getTag()
			.contains("BlockUsed")) {
			String usedblock = NBTUtil.readBlockState(stack.getTag()
				.getCompound("BlockUsed"))
				.getBlock()
				.getTranslationKey();
			ItemDescription.add(tooltip,
				Lang.translate("blockzapper.usingBlock",
					new TranslationTextComponent(usedblock).formatted(TextFormatting.GRAY))
					.formatted(TextFormatting.DARK_GRAY));
		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		boolean differentBlock = false;
		if (oldStack.hasTag() && newStack.hasTag() && oldStack.getTag()
			.contains("BlockUsed")
			&& newStack.getTag()
				.contains("BlockUsed"))
			differentBlock = NBTUtil.readBlockState(oldStack.getTag()
				.getCompound("BlockUsed")) != NBTUtil.readBlockState(
					newStack.getTag()
						.getCompound("BlockUsed"));
		return slotChanged || !isZapper(newStack) || differentBlock;
	}

	public boolean isZapper(ItemStack newStack) {
		return newStack.getItem() instanceof ZapperItem;
	}

	@Nonnull
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		// Shift -> open GUI
		if (context.getPlayer() != null && context.getPlayer()
			.isSneaking()) {
			if (context.getWorld().isRemote) {
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
					openHandgunGUI(context.getItem(), context.getHand() == Hand.OFF_HAND);
				});
				applyCooldown(context.getPlayer(), context.getItem(), false);
			}
			return ActionResultType.SUCCESS;
		}
		return super.onItemUse(context);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack item = player.getHeldItem(hand);
		CompoundNBT nbt = item.getOrCreateTag();

		// Shift -> Open GUI
		if (player.isSneaking()) {
			if (world.isRemote) {
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
					openHandgunGUI(item, hand == Hand.OFF_HAND);
				});
				applyCooldown(player, item, false);
			}
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, item);
		}

		boolean mainHand = hand == Hand.MAIN_HAND;
		boolean isSwap = item.getTag()
			.contains("_Swap");
		boolean gunInOtherHand = isZapper(player.getHeldItem(mainHand ? Hand.OFF_HAND : Hand.MAIN_HAND));

		// Pass To Offhand
		if (mainHand && isSwap && gunInOtherHand)
			return new ActionResult<ItemStack>(ActionResultType.FAIL, item);
		if (mainHand && !isSwap && gunInOtherHand)
			item.getTag()
				.putBoolean("_Swap", true);
		if (!mainHand && isSwap)
			item.getTag()
				.remove("_Swap");
		if (!mainHand && gunInOtherHand)
			player.getHeldItem(Hand.MAIN_HAND)
				.getTag()
				.remove("_Swap");
		player.setActiveHand(hand);

		// Check if can be used
		ITextComponent msg = validateUsage(item);
		if (msg != null) {
			world.playSound(player, player.getBlockPos(), AllSoundEvents.BLOCKZAPPER_DENY.get(), SoundCategory.BLOCKS,
				1f, 0.5f);
			player.sendStatusMessage(msg.copy().formatted(TextFormatting.RED), true);
			return new ActionResult<ItemStack>(ActionResultType.FAIL, item);
		}

		BlockState stateToUse = Blocks.AIR.getDefaultState();
		if (nbt.contains("BlockUsed"))
			stateToUse = NBTUtil.readBlockState(nbt.getCompound("BlockUsed"));
		stateToUse = BlockHelper.setZeroAge(stateToUse);
		CompoundNBT data = null;
		if (AllBlockTags.SAFE_NBT.matches(stateToUse) && nbt.contains("BlockData", NBT.TAG_COMPOUND)) {
			data = nbt.getCompound("BlockData");
		}

		// Raytrace - Find the target
		Vector3d start = player.getPositionVec()
			.add(0, player.getEyeHeight(), 0);
		Vector3d range = player.getLookVec()
			.scale(getZappingRange(item));
		BlockRayTraceResult raytrace = world
			.rayTraceBlocks(new RayTraceContext(start, start.add(range), BlockMode.OUTLINE, FluidMode.NONE, player));
		BlockPos pos = raytrace.getPos();
		BlockState stateReplaced = world.getBlockState(pos);

		// No target
		if (pos == null || stateReplaced.getBlock() == Blocks.AIR) {
			applyCooldown(player, item, gunInOtherHand);
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, item);
		}

		// Find exact position of gun barrel for VFX
		float yaw = (float) ((player.rotationYaw) / -180 * Math.PI);
		float pitch = (float) ((player.rotationPitch) / -180 * Math.PI);
		Vector3d barrelPosNoTransform =
			new Vector3d(mainHand == (player.getPrimaryHand() == HandSide.RIGHT) ? -.35f : .35f, -0.1f, 1);
		Vector3d barrelPos = start.add(barrelPosNoTransform.rotatePitch(pitch)
			.rotateYaw(yaw));

		// Client side
		if (world.isRemote) {
			ZapperRenderHandler.dontAnimateItem(hand);
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, item);
		}

		// Server side
		if (activate(world, player, item, stateToUse, raytrace, data)) {
			applyCooldown(player, item, gunInOtherHand);
			AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> player),
				new ZapperBeamPacket(barrelPos, raytrace.getHitVec(), hand, false));
			AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
				new ZapperBeamPacket(barrelPos, raytrace.getHitVec(), hand, true));
		}

		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, item);
	}

	public ITextComponent validateUsage(ItemStack item) {
		CompoundNBT tag = item.getOrCreateTag();
		if (!canActivateWithoutSelectedBlock(item) && !tag.contains("BlockUsed"))
			return Lang.createTranslationTextComponent("blockzapper.leftClickToSet");
		return null;
	}

	protected abstract boolean activate(World world, PlayerEntity player, ItemStack item, BlockState stateToUse,
		BlockRayTraceResult raytrace, CompoundNBT data);

	@OnlyIn(Dist.CLIENT)
	protected abstract void openHandgunGUI(ItemStack item, boolean b);

	protected abstract int getCooldownDelay(ItemStack item);

	protected abstract int getZappingRange(ItemStack stack);

	protected boolean canActivateWithoutSelectedBlock(ItemStack stack) {
		return false;
	}

	protected void applyCooldown(PlayerEntity playerIn, ItemStack item, boolean dual) {
		int delay = getCooldownDelay(item);
		playerIn.getCooldownTracker()
			.setCooldown(item.getItem(), dual ? delay * 2 / 3 : delay);
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		return true;
	}

	@Override
	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return false;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.NONE;
	}

	public static void setTileData(World world, BlockPos pos, CompoundNBT data) {
		if (data != null) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null && !tile.onlyOpsCanSetNbt()) {
				data.putInt("x", pos.getX());
				data.putInt("y", pos.getY());
				data.putInt("z", pos.getZ());
				tile.read(data);
			}
		}
	}

}
