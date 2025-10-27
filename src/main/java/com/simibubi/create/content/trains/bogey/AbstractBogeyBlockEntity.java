package com.simibubi.create.content.trains.bogey;

import static com.simibubi.create.content.trains.entity.CarriageBogey.UPSIDE_DOWN_KEY;

import net.minecraft.core.HolderLookup;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllBogeyStyles;
import com.simibubi.create.foundation.blockEntity.CachedRenderBBBlockEntity;

import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class AbstractBogeyBlockEntity extends CachedRenderBBBlockEntity {
	public static final String BOGEY_STYLE_KEY = "BogeyStyle";
	public static final String BOGEY_DATA_KEY = "BogeyData";

	private CompoundTag bogeyData;

	public AbstractBogeyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public abstract BogeyStyle getDefaultStyle();

	public CompoundTag getBogeyData() {
		if (this.bogeyData == null || !this.bogeyData.contains(BOGEY_STYLE_KEY))
			this.bogeyData = this.createBogeyData();
		return this.bogeyData;
	}

	public void setBogeyData(@NotNull CompoundTag newData) {
		if (!newData.contains(BOGEY_STYLE_KEY)) {
			ResourceLocation style = getDefaultStyle().id;
			NBTHelper.writeResourceLocation(newData, BOGEY_STYLE_KEY, style);
		}
		this.bogeyData = newData;
	}

	public void setBogeyStyle(@NotNull BogeyStyle style) {
		ResourceLocation location = style.id;
		CompoundTag data = this.getBogeyData();
		NBTHelper.writeResourceLocation(data, BOGEY_STYLE_KEY, location);
		markUpdated();
	}

	@NotNull
	public BogeyStyle getStyle() {
		CompoundTag data = this.getBogeyData();
		ResourceLocation currentStyle = NBTHelper.readResourceLocation(data, BOGEY_STYLE_KEY);
		BogeyStyle style = AllBogeyStyles.BOGEY_STYLES.get(currentStyle);
		if (style == null) {
			setBogeyStyle(getDefaultStyle());
			return getStyle();
		}
		return style;
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
		CompoundTag data = this.getBogeyData();
		if (data != null) tag.put(BOGEY_DATA_KEY, data); // Now contains style
		super.saveAdditional(tag, registries);
	}

	@Override
	protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
		if (tag.contains(BOGEY_DATA_KEY))
			this.bogeyData = tag.getCompound(BOGEY_DATA_KEY);
		else
			this.bogeyData = this.createBogeyData();
		super.loadAdditional(tag, registries);
	}

	private CompoundTag createBogeyData() {
		CompoundTag nbt = new CompoundTag();
		NBTHelper.writeResourceLocation(nbt, BOGEY_STYLE_KEY, getDefaultStyle().id);
		boolean upsideDown = false;
		if (getBlockState().getBlock() instanceof AbstractBogeyBlock<?> bogeyBlock)
			upsideDown = bogeyBlock.isUpsideDown(getBlockState());
		nbt.putBoolean(UPSIDE_DOWN_KEY, upsideDown);
		return nbt;
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().inflate(2);
	}

	// Ponder
	LerpedFloat virtualAnimation = LerpedFloat.angular();

	public float getVirtualAngle(float partialTicks) {
		return virtualAnimation.getValue(partialTicks);
	}

	public void animate(float distanceMoved) {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof AbstractBogeyBlock<?> type))
			return;
		double angleDiff = 360 * distanceMoved / (Math.PI * 2 * type.getWheelRadius());
		double newWheelAngle = (virtualAnimation.getValue() - angleDiff) % 360;
		virtualAnimation.setValue(newWheelAngle);
	}

	private void markUpdated() {
		setChanged();
		Level level = getLevel();
		if (level != null)
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
	}
}
