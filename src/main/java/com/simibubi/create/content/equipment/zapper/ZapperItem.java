package com.simibubi.create.content.equipment.zapper;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.item.CustomArmPoseItem;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.nbt.NBTProcessors;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class ZapperItem extends Item implements CustomArmPoseItem {

	public ZapperItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		if (stack.has(AllDataComponents.SHAPER_BLOCK_USED)) {
			MutableComponent usedBlock = stack.get(AllDataComponents.SHAPER_BLOCK_USED).getBlock().getName();
			tooltip.add(CreateLang.translateDirect("terrainzapper.usingBlock", usedBlock.withStyle(ChatFormatting.GRAY))
					.withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		boolean differentBlock = false;
		if (oldStack.has(AllDataComponents.SHAPER_BLOCK_USED) && newStack.has(AllDataComponents.SHAPER_BLOCK_USED))
			differentBlock = oldStack.get(AllDataComponents.SHAPER_BLOCK_USED) != newStack.get(AllDataComponents.SHAPER_BLOCK_USED);
		return slotChanged || !isZapper(newStack) || differentBlock;
	}

	public boolean isZapper(ItemStack newStack) {
		return newStack.getItem() instanceof ZapperItem;
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		// Shift -> open GUI
		if (context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown()) {
			if (context.getLevel().isClientSide) {
				CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> {
					openHandgunGUI(context.getItemInHand(), context.getHand());
				});
				context.getPlayer()
					.getCooldowns()
					.addCooldown(context.getItemInHand()
						.getItem(), 10);
			}
			return InteractionResult.SUCCESS;
		}
		return super.useOn(context);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack item = player.getItemInHand(hand);
		boolean mainHand = hand == InteractionHand.MAIN_HAND;

		// Shift -> Open GUI
		if (player.isShiftKeyDown()) {
			if (world.isClientSide) {
				CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> {
					openHandgunGUI(item, hand);
				});
				player.getCooldowns()
					.addCooldown(item.getItem(), 10);
			}
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, item);
		}

		if (ShootableGadgetItemMethods.shouldSwap(player, item, hand, this::isZapper))
			return new InteractionResultHolder<>(InteractionResult.FAIL, item);

		// Check if can be used
		Component msg = validateUsage(item);
		if (msg != null) {
			AllSoundEvents.DENY.play(world, player, player.blockPosition());
			player.displayClientMessage(msg.plainCopy()
				.withStyle(ChatFormatting.RED), true);
			return new InteractionResultHolder<>(InteractionResult.FAIL, item);
		}

		BlockState stateToUse = Blocks.AIR.defaultBlockState();
		if (item.has(AllDataComponents.SHAPER_BLOCK_USED))
			stateToUse = item.get(AllDataComponents.SHAPER_BLOCK_USED);
		stateToUse = BlockHelper.setZeroAge(stateToUse);
		CompoundTag data = null;
		if (AllBlockTags.SAFE_NBT.matches(stateToUse) && item.has(AllDataComponents.SHAPER_BLOCK_DATA)) {
			data = item.get(AllDataComponents.SHAPER_BLOCK_DATA);
		}

		// Raytrace - Find the target
		Vec3 start = player.position()
			.add(0, player.getEyeHeight(), 0);
		Vec3 range = player.getLookAngle()
			.scale(getZappingRange(item));
		BlockHitResult raytrace =
			world.clip(new ClipContext(start, start.add(range), Block.OUTLINE, Fluid.NONE, player));
		BlockPos pos = raytrace.getBlockPos();
		BlockState stateReplaced = world.getBlockState(pos);

		// No target
		if (pos == null || stateReplaced.getBlock() == Blocks.AIR) {
			ShootableGadgetItemMethods.applyCooldown(player, item, hand, this::isZapper, getCooldownDelay(item));
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, item);
		}

		// Find exact position of gun barrel for VFX
		Vec3 barrelPos = ShootableGadgetItemMethods.getGunBarrelVec(player, mainHand, new Vec3(.35f, -0.1f, 1));

		// Client side
		if (world.isClientSide) {
			CreateClient.ZAPPER_RENDER_HANDLER.dontAnimateItem(hand);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, item);
		}

		// Server side
		if (activate(world, player, item, stateToUse, raytrace, data)) {
			ShootableGadgetItemMethods.applyCooldown(player, item, hand, this::isZapper, getCooldownDelay(item));
			ShootableGadgetItemMethods.sendPackets(player,
				b -> new ZapperBeamPacket(barrelPos, hand, b, raytrace.getLocation()));
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, item);
	}

	public Component validateUsage(ItemStack item) {
		if (!canActivateWithoutSelectedBlock(item) && !item.has(AllDataComponents.SHAPER_BLOCK_USED))
			return CreateLang.translateDirect("terrainzapper.leftClickToSet");
		return null;
	}

	protected abstract boolean activate(Level world, Player player, ItemStack item, BlockState stateToUse,
		BlockHitResult raytrace, CompoundTag data);

	@OnlyIn(Dist.CLIENT)
	protected abstract void openHandgunGUI(ItemStack item, InteractionHand hand);

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
	public boolean canAttackBlock(BlockState state, Level worldIn, BlockPos pos, Player player) {
		return false;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	@Override
	@Nullable
	public ArmPose getArmPose(ItemStack stack, AbstractClientPlayer player, InteractionHand hand) {
		if (!player.swinging) {
			return ArmPose.CROSSBOW_HOLD;
		}
		return null;
	}

	public static void setBlockEntityData(Level world, BlockPos pos, BlockState state, CompoundTag data, Player player) {
		if (data != null && AllBlockTags.SAFE_NBT.matches(state)) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity != null) {
				data = NBTProcessors.process(state, blockEntity, data, !player.isCreative());
				if (data == null)
					return;
				data.putInt("x", pos.getX());
				data.putInt("y", pos.getY());
				data.putInt("z", pos.getZ());
				blockEntity.loadWithComponents(data, world.registryAccess());
			}
		}
	}

}
