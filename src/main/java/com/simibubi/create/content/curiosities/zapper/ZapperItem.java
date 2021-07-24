package com.simibubi.create.content.curiosities.zapper;

import java.util.List;

import javax.annotation.Nonnull;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTProcessors;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
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

public abstract class ZapperItem extends Item {

	public ZapperItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTag() && stack.getTag()
			.contains("BlockUsed")) {
			String usedblock = NBTUtil.readBlockState(stack.getTag()
				.getCompound("BlockUsed"))
				.getBlock()
				.getDescriptionId();
			ItemDescription.add(tooltip,
				Lang.translate("terrainzapper.usingBlock",
					new TranslationTextComponent(usedblock).withStyle(TextFormatting.GRAY))
					.withStyle(TextFormatting.DARK_GRAY));
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
	public ActionResultType useOn(ItemUseContext context) {
		// Shift -> open GUI
		if (context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown()) {
			if (context.getLevel().isClientSide) {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
					openHandgunGUI(context.getItemInHand(), context.getHand() == Hand.OFF_HAND);
				});
				context.getPlayer()
					.getCooldowns()
					.addCooldown(context.getItemInHand()
						.getItem(), 10);
			}
			return ActionResultType.SUCCESS;
		}
		return super.useOn(context);
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack item = player.getItemInHand(hand);
		CompoundNBT nbt = item.getOrCreateTag();
		boolean mainHand = hand == Hand.MAIN_HAND;

		// Shift -> Open GUI
		if (player.isShiftKeyDown()) {
			if (world.isClientSide) {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
					openHandgunGUI(item, hand == Hand.OFF_HAND);
				});
				player.getCooldowns()
					.addCooldown(item.getItem(), 10);
			}
			return new ActionResult<>(ActionResultType.SUCCESS, item);
		}

		if (ShootableGadgetItemMethods.shouldSwap(player, item, hand, this::isZapper))
			return new ActionResult<>(ActionResultType.FAIL, item);

		// Check if can be used
		ITextComponent msg = validateUsage(item);
		if (msg != null) {
			AllSoundEvents.DENY.play(world, player, player.blockPosition());
			player.displayClientMessage(msg.plainCopy()
				.withStyle(TextFormatting.RED), true);
			return new ActionResult<>(ActionResultType.FAIL, item);
		}

		BlockState stateToUse = Blocks.AIR.defaultBlockState();
		if (nbt.contains("BlockUsed"))
			stateToUse = NBTUtil.readBlockState(nbt.getCompound("BlockUsed"));
		stateToUse = BlockHelper.setZeroAge(stateToUse);
		CompoundNBT data = null;
		if (AllBlockTags.SAFE_NBT.matches(stateToUse) && nbt.contains("BlockData", NBT.TAG_COMPOUND)) {
			data = nbt.getCompound("BlockData");
		}

		// Raytrace - Find the target
		Vector3d start = player.position()
			.add(0, player.getEyeHeight(), 0);
		Vector3d range = player.getLookAngle()
			.scale(getZappingRange(item));
		BlockRayTraceResult raytrace = world
			.clip(new RayTraceContext(start, start.add(range), BlockMode.OUTLINE, FluidMode.NONE, player));
		BlockPos pos = raytrace.getBlockPos();
		BlockState stateReplaced = world.getBlockState(pos);

		// No target
		if (pos == null || stateReplaced.getBlock() == Blocks.AIR) {
			ShootableGadgetItemMethods.applyCooldown(player, item, hand, this::isZapper, getCooldownDelay(item));
			return new ActionResult<>(ActionResultType.SUCCESS, item);
		}

		// Find exact position of gun barrel for VFX
		Vector3d barrelPos = ShootableGadgetItemMethods.getGunBarrelVec(player, mainHand, new Vector3d(.35f, -0.1f, 1));

		// Client side
		if (world.isClientSide) {
			CreateClient.ZAPPER_RENDER_HANDLER.dontAnimateItem(hand);
			return new ActionResult<>(ActionResultType.SUCCESS, item);
		}

		// Server side
		if (activate(world, player, item, stateToUse, raytrace, data)) {
			ShootableGadgetItemMethods.applyCooldown(player, item, hand, this::isZapper, getCooldownDelay(item));
			ShootableGadgetItemMethods.sendPackets(player,
				b -> new ZapperBeamPacket(barrelPos, raytrace.getLocation(), hand, b));
		}

		return new ActionResult<>(ActionResultType.SUCCESS, item);
	}

	public ITextComponent validateUsage(ItemStack item) {
		CompoundNBT tag = item.getOrCreateTag();
		if (!canActivateWithoutSelectedBlock(item) && !tag.contains("BlockUsed"))
			return Lang.createTranslationTextComponent("terrainzapper.leftClickToSet");
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

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		return true;
	}

	@Override
	public boolean canAttackBlock(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return false;
	}

	@Override
	public UseAction getUseAnimation(ItemStack stack) {
		return UseAction.NONE;
	}

	public static void setTileData(World world, BlockPos pos, BlockState state, CompoundNBT data, PlayerEntity player) {
		if (data != null && AllBlockTags.SAFE_NBT.matches(state)) {
			TileEntity tile = world.getBlockEntity(pos);
			if (tile != null) {
				data = NBTProcessors.process(tile, data, !player.isCreative());
				if (data == null)
					return;
				data.putInt("x", pos.getX());
				data.putInt("y", pos.getY());
				data.putInt("z", pos.getZ());
				tile.load(state, data);
			}
		}
	}

}
