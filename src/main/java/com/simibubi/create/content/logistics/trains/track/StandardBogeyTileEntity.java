package com.simibubi.create.content.logistics.trains.track;

import com.simibubi.create.AllBogeyStyles;
import com.simibubi.create.content.logistics.trains.AbstractBogeyBlock;
import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;
import com.simibubi.create.foundation.tileEntity.CachedRenderBBTileEntity;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
;
import org.jetbrains.annotations.NotNull;

public class StandardBogeyTileEntity extends CachedRenderBBTileEntity {
	public static String BOGEY_STYLE_KEY = "BogeyStyle";
	public static String BOGEY_DATA_KEY = "BogeyData";

	private CompoundTag bogeyData;

	public StandardBogeyTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public CompoundTag getBogeyData() {
		if (this.bogeyData == null || !this.bogeyData.contains(BOGEY_STYLE_KEY))
			this.bogeyData = this.createBogeyData();
		return this.bogeyData;
	}

	public void setBogeyData(@NotNull CompoundTag newData) {
		if (!newData.contains(BOGEY_STYLE_KEY)) {
			ResourceLocation style = AllBogeyStyles.STANDARD.name;
			NBTHelper.writeResourceLocation(newData, BOGEY_STYLE_KEY, style);
		}
		this.bogeyData = newData;
	}

	public void setBogeyStyle(@NotNull BogeyStyle style) {
		ResourceLocation location = style.name;
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
			setBogeyStyle(AllBogeyStyles.STANDARD);
			return getStyle();
		}
		return style;
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag pTag) {
		CompoundTag data = this.getBogeyData();
		if (data != null) pTag.put(BOGEY_DATA_KEY, data); // Now contains style
		super.saveAdditional(pTag);
	}

	@Override
	public void load(CompoundTag pTag) {
		if (pTag.contains(BOGEY_DATA_KEY))
			this.bogeyData = pTag.getCompound(BOGEY_DATA_KEY);
		else
			this.bogeyData = this.createBogeyData();
		super.load(pTag);
	}

	private CompoundTag createBogeyData() {
		CompoundTag nbt = new CompoundTag();
		NBTHelper.writeResourceLocation(nbt, BOGEY_STYLE_KEY, AllBogeyStyles.STANDARD.name);
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
		if (!(blockState.getBlock() instanceof AbstractBogeyBlock type))
			return;
		double angleDiff = 360 * distanceMoved / (Math.PI * 2 * type.getWheelRadius());
		double newWheelAngle = (virtualAnimation.getValue() - angleDiff) % 360;
		virtualAnimation.setValue(newWheelAngle);
	}

	private void markUpdated() {
		setChanged();
		Level level = getLevel();
		if (level != null)
			getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
	}
}
