package com.simibubi.create.content.redstone.displayLink;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.LegacyBlockEntityDataComponentBridge;
import com.simibubi.create.foundation.utility.CreateLang;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public abstract class ClickToLinkBlockItem extends BlockItem {
	public ClickToLinkBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@SubscribeEvent
	public static void linkableItemAlwaysPlacesWhenUsed(PlayerInteractEvent.RightClickBlock event) {
		ItemStack usedItem = event.getItemStack();
		if (!(usedItem.getItem() instanceof ClickToLinkBlockItem blockItem))
			return;
		if (event.getLevel()
			.getBlockState(event.getPos())
			.is(blockItem.getBlock()))
			return;
		event.setUseBlock(TriState.FALSE);
	}

	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		ItemStack stack = pContext.getItemInHand();
		BlockPos pos = pContext.getClickedPos();
		Level level = pContext.getLevel();
		BlockState state = level.getBlockState(pos);
		Player player = pContext.getPlayer();
		String msgKey = getMessageTranslationKey();
		int maxDistance = getMaxDistanceFromSelection();

		if (player == null)
			return InteractionResult.FAIL;

		if (player.isShiftKeyDown() && stack.has(AllDataComponents.CLICK_TO_LINK_DATA)) {
			if (level.isClientSide())
				return InteractionResult.SUCCESS;
			player.sendOverlayMessage(CreateLang.translateDirect(msgKey + ".clear"));
			stack.remove(AllDataComponents.CLICK_TO_LINK_DATA);
			stack.remove(DataComponents.BLOCK_ENTITY_DATA);
			return InteractionResult.SUCCESS;
		}

		Identifier placedDim = level.dimension()
			.identifier();

		if (!stack.has(AllDataComponents.CLICK_TO_LINK_DATA)) {
			if (!isValidTarget(level, pos)) {
				if (placeWhenInvalid()) {
					InteractionResult useOn = super.useOn(pContext);
					if (level.isClientSide() || useOn == InteractionResult.FAIL)
						return useOn;

					ItemStack itemInHand = player.getItemInHand(pContext.getHand());
					if (!itemInHand.isEmpty()) {
						stack.remove(AllDataComponents.CLICK_TO_LINK_DATA);
						stack.remove(DataComponents.BLOCK_ENTITY_DATA);
					}
					return useOn;
				}

				if (level.isClientSide())
					AllSoundEvents.DENY.playFrom(player);
				player.sendOverlayMessage(CreateLang.translateDirect(msgKey + ".invalid"));
				return InteractionResult.FAIL;
			}

			if (level.isClientSide())
				return InteractionResult.SUCCESS;

			player.sendOverlayMessage(CreateLang.translateDirect(msgKey + ".set"));
			stack.set(AllDataComponents.CLICK_TO_LINK_DATA, new ClickToLinkData(pos, placedDim));
			return InteractionResult.SUCCESS;
		}

		ClickToLinkData data = stack.get(AllDataComponents.CLICK_TO_LINK_DATA);
		//noinspection DataFlowIssue
		BlockPos selectedPos = data.selectedPos();
		Identifier selectedDim = data.selectedDim();
		BlockPos placedPos = pos.relative(pContext.getClickedFace(), state.canBeReplaced() ? 0 : 1);

		if (maxDistance != -1 && (!selectedPos.closerThan(placedPos, maxDistance) || !selectedDim.equals(placedDim))) {
			player.sendOverlayMessage(CreateLang.translateDirect(msgKey + ".too_far")
				.withStyle(ChatFormatting.RED));
			return InteractionResult.FAIL;
		}

		CompoundTag beTag = new CompoundTag();
		beTag.put("TargetOffset", com.simibubi.create.foundation.utility.LegacyNbtUtilsBridge.writeBlockPos(selectedPos.subtract(placedPos)));
		NBTHelper.writeIdentifier(beTag, "TargetDimension", selectedDim);
		LegacyBlockEntityDataComponentBridge.set(stack, ((IBE<?>) this.getBlock()).getBlockEntityType(), beTag);

		InteractionResult useOn = super.useOn(pContext);
		if (level.isClientSide() || useOn == InteractionResult.FAIL)
			return useOn;

		ItemStack itemInHand = player.getItemInHand(pContext.getHand());
		if (!itemInHand.isEmpty()) {
			stack.remove(AllDataComponents.CLICK_TO_LINK_DATA);
			stack.remove(DataComponents.BLOCK_ENTITY_DATA);
		}
		player.sendOverlayMessage(CreateLang.translateDirect(msgKey + ".success")
			.withStyle(ChatFormatting.GREEN));
		return useOn;
	}

	public static void clientTick() {
		ClickToLinkClient.clientTick();
	}

	public abstract int getMaxDistanceFromSelection();

	public abstract String getMessageTranslationKey();

	public boolean placeWhenInvalid() {
		return false;
	}

	public boolean isValidTarget(LevelAccessor level, BlockPos pos) {
		return true;
	}

	public AABB getSelectionBounds(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		VoxelShape shape = state.getShape(world, pos);
		return shape.isEmpty() ? new AABB(BlockPos.ZERO)
			: shape.bounds()
				.move(pos);
	}

	public record ClickToLinkData(BlockPos selectedPos, Identifier selectedDim) {
		public static final Codec<ClickToLinkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockPos.CODEC.fieldOf("selected_pos").forGetter(ClickToLinkData::selectedPos),
			Identifier.CODEC.fieldOf("selected_dim").forGetter(ClickToLinkData::selectedDim)
		).apply(instance, ClickToLinkData::new));

		public static final StreamCodec<ByteBuf, ClickToLinkData> STREAM_CODEC = StreamCodec.composite(
		    BlockPos.STREAM_CODEC, ClickToLinkData::selectedPos,
		    Identifier.STREAM_CODEC, ClickToLinkData::selectedDim,
		    ClickToLinkData::new
		);
	}
}
