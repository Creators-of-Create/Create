package com.simibubi.create.content.logistics.trains.track;

import com.simibubi.create.AllBogeyStyles;
import com.simibubi.create.AllRegistries;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.content.logistics.trains.AbstractBogeyBlock;
import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;
import com.simibubi.create.foundation.tileEntity.CachedRenderBBTileEntity;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;

public class StandardBogeyTileEntity extends CachedRenderBBTileEntity {
	private BogeyStyle style;
	public CompoundTag bogeyData;

	public StandardBogeyTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public void setBogeyStyle(@NotNull BogeyStyle style) {
		this.style = style;
		markUpdated();
	}

	@NotNull
	public BogeyStyle getStyle() {
		if (this.style == null)
			setBogeyStyle(AllBogeyStyles.STANDARD.get());
		return this.style;
	}


	@Override
	protected void saveAdditional(CompoundTag pTag) {
		if (style != null && style.getRegistryName() != null)
			NBTHelper.writeResourceLocation(pTag, "bogeyStyle", style.getRegistryName());
		if (bogeyData != null)
			pTag.put("bogeyData", bogeyData);
		super.saveAdditional(pTag);
	}

	@Override
	public void load(CompoundTag pTag) {
		if (pTag.contains("bogeyStyle")) {
			ResourceLocation location = NBTHelper.readResourceLocation(pTag, "bogeyStyle");
			this.style = AllRegistries.BOGEY_REGISTRY.get().getValue(location);
		} else {
			this.style = AllBogeyStyles.STANDARD.get();
		}
		if (pTag.contains("bogeyData"))
			this.bogeyData = pTag.getCompound("bogeyData");
		super.load(pTag);
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
