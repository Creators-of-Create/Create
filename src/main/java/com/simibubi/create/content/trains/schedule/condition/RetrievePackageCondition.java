package com.simibubi.create.content.trains.schedule.condition;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;

import com.simibubi.create.content.trains.station.GlobalStation.GlobalPackagePort;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.data.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import net.neoforged.neoforge.items.IItemHandlerModifiable;

import net.neoforged.neoforge.items.ItemHandlerHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map.Entry;

public class RetrievePackageCondition extends ScheduleWaitCondition {

	public RetrievePackageCondition() {
		data.putString("Filter", "");
	}

	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		return true;
	}

	@Override
	public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
		return CreateLang.translateDirect("schedule.condition.retrieve_package.status", getFilter());
	}

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(PackageStyles.getDefaultBox(), CreateLang.translateDirect("schedule.condition.retrieve_package.summary"));
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			Component.translatable(getId().getNamespace() + ".schedule." + type + "." + getId().getPath()),
			CreateLang.translateDirect("schedule.condition.retrieve_package.addressed", getFilter())
				.withStyle(ChatFormatting.DARK_AQUA));
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("retrieve_package");
	}

	@OnlyIn(Dist.CLIENT)
	protected void modifyEditBox(EditBox box) {
		box.setFilter(s -> StringUtils.countMatches(s, '*') <= 3);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(ModularGuiLineBuilder builder) {
		builder.addTextInput(0, 121, (e, t) -> modifyEditBox(e), "Filter");
	}

	public String getFilter() {
		return textData("Filter");
	}
}
