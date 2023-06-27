package com.simibubi.create.foundation.blockEntity.behaviour.scrollValue;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

public class ScrollValueBehaviour extends BlockEntityBehaviour implements ValueSettingsBehaviour {

	public static final BehaviourType<ScrollValueBehaviour> TYPE = new BehaviourType<>();

	ValueBoxTransform slotPositioning;
	Vec3 textShift;

	int min = 0;
	protected int max = 1;
	public int value;
	public Component label;
	Consumer<Integer> callback;
	Consumer<Integer> clientCallback;
	Function<Integer, String> formatter;
	private Supplier<Boolean> isActive;
	boolean needsWrench;

	public ScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
		super(be);
		this.setLabel(label);
		slotPositioning = slot;
		callback = i -> {
		};
		clientCallback = i -> {
		};
		formatter = i -> Integer.toString(i);
		value = 0;
		isActive = () -> true;
	}

	@Override
	public boolean isSafeNBT() {
		return true;
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		nbt.putInt("ScrollValue", value);
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		value = nbt.getInt("ScrollValue");
		super.read(nbt, clientPacket);
	}

	public ScrollValueBehaviour withClientCallback(Consumer<Integer> valueCallback) {
		clientCallback = valueCallback;
		return this;
	}

	public ScrollValueBehaviour withCallback(Consumer<Integer> valueCallback) {
		callback = valueCallback;
		return this;
	}

	public ScrollValueBehaviour between(int min, int max) {
		this.min = min;
		this.max = max;
		return this;
	}

	public ScrollValueBehaviour requiresWrench() {
		this.needsWrench = true;
		return this;
	}

	public ScrollValueBehaviour withFormatter(Function<Integer, String> formatter) {
		this.formatter = formatter;
		return this;
	}

	public ScrollValueBehaviour onlyActiveWhen(Supplier<Boolean> condition) {
		isActive = condition;
		return this;
	}

	public void setValue(int value) {
		value = Mth.clamp(value, min, max);
		if (value == this.value)
			return;
		this.value = value;
		callback.accept(value);
		blockEntity.setChanged();
		blockEntity.sendData();
	}

	public int getValue() {
		return value;
	}

	public String formatValue() {
		return formatter.apply(value);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean isActive() {
		return isActive.get();
	}

	@Override
	public boolean testHit(Vec3 hit) {
		BlockState state = blockEntity.getBlockState();
		Vec3 localHit = hit.subtract(Vec3.atLowerCornerOf(blockEntity.getBlockPos()));
		return slotPositioning.testHit(state, localHit);
	}

	public void setLabel(Component label) {
		this.label = label;
	}

	public static class StepContext {
		public int currentValue;
		public boolean forward;
		public boolean shift;
		public boolean control;
	}

	@Override
	public ValueBoxTransform getSlotPositioning() {
		return slotPositioning;
	}

	@Override
	public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
		return new ValueSettingsBoard(label, max, 10, ImmutableList.of(Components.literal("Value")),
			new ValueSettingsFormatter(ValueSettings::format));
	}

	@Override
	public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
		if (valueSetting.equals(getValueSettings()))
			return;
		setValue(valueSetting.value());
		playFeedbackSound(this);
	}

	@Override
	public ValueSettings getValueSettings() {
		return new ValueSettings(0, value);
	}

	@Override
	public boolean onlyVisibleWithWrench() {
		return needsWrench;
	}

	@Override
	public void onShortInteract(Player player, InteractionHand hand, Direction side) {
		if (player instanceof FakePlayer)
			blockEntity.getBlockState()
				.use(getWorld(), player, hand,
					new BlockHitResult(VecHelper.getCenterOf(getPos()), side, getPos(), true));
	}

}
