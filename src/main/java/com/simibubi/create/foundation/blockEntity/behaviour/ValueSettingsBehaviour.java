package com.simibubi.create.foundation.blockEntity.behaviour;

import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public interface ValueSettingsBehaviour {

	public static record ValueSettings(int row, int value) {

		public MutableComponent format() {
			return Lang.number(value)
				.component();
		}

	};

	public boolean testHit(Vec3 hit);

	public boolean isActive();

	default boolean onlyVisibleWithWrench() {
		return false;
	}

	default void newSettingHovered(ValueSettings valueSetting) {}

	public ValueBoxTransform getSlotPositioning();

	public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult);

	public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown);

	public ValueSettings getValueSettings();

	default boolean acceptsValueSettings() {
		return true;
	}

	default void playFeedbackSound(BlockEntityBehaviour origin) {
		origin.getWorld()
			.playSound(null, origin.getPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.25f, 2f);
		origin.getWorld()
			.playSound(null, origin.getPos(), SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE, SoundSource.BLOCKS, 0.03f, 1.125f);
	}

	default void onShortInteract(Player player, InteractionHand hand, Direction side) {}

}
