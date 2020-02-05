package com.simibubi.create.modules.contraptions.relays.gauge;

import static net.minecraft.util.text.TextFormatting.AQUA;
import static net.minecraft.util.text.TextFormatting.DARK_GRAY;
import static net.minecraft.util.text.TextFormatting.GRAY;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.modules.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.modules.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class GaugeInformationRenderer {

	private static DecimalFormat decimalFormat = new DecimalFormat("#.##");

	@SubscribeEvent
	public static void lookingAtBlocksThroughGogglesShowsTooltip(RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.HOTBAR)
			return;

		RayTraceResult objectMouseOver = Minecraft.getInstance().objectMouseOver;
		if (!(objectMouseOver instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) objectMouseOver;
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;
		BlockPos pos = result.getPos();
		BlockState state = world.getBlockState(pos);
		ItemStack goggles = mc.player.getItemStackFromSlot(EquipmentSlotType.HEAD);

		if (!AllItems.GOGGLES.typeOf(goggles))
			return;
		if (mc.player.isSneaking())
			return;

		List<String> tooltip = new ArrayList<>();
		TileEntity te = world.getTileEntity(pos);

		if (state.getBlock() instanceof GaugeBlock)
			addGaugeTooltip(state, tooltip, te);
		if (te instanceof GeneratingKineticTileEntity)
			addGeneratorTooltip(state, tooltip, (GeneratingKineticTileEntity) te);
		if (te instanceof KineticTileEntity)
			addStressTooltip(state, tooltip, (KineticTileEntity) te);

		if (tooltip.isEmpty())
			return;

		GlStateManager.pushMatrix();
		Screen tooltipScreen = new Screen(null) {

			@Override
			public void init(Minecraft mc, int width, int height) {
				this.minecraft = mc;
				this.itemRenderer = mc.getItemRenderer();
				this.font = mc.fontRenderer;
				this.width = width;
				this.height = height;
			}

		};

		tooltipScreen.init(mc, mc.mainWindow.getScaledWidth(), mc.mainWindow.getScaledHeight());
		tooltipScreen.renderTooltip(tooltip, tooltipScreen.width / 2, tooltipScreen.height / 2);
		ScreenElementRenderer.render3DItem(() -> {
			GlStateManager.translated(tooltipScreen.width / 2 + 10, tooltipScreen.height / 2 - 16, 0);
			return goggles;
		});
		GlStateManager.popMatrix();

	}

	private static void addStressTooltip(BlockState state, List<String> tooltip, KineticTileEntity te) {
		String spacing = "    ";
		float stressApplied = te.getStressApplied();
		if (stressApplied == 0)
			return;

		String _kineticStatsTitle = Lang.translate("gui.goggles.kinetic_stats");
		String _stressImpact = Lang.translate("tooltip.stressImpact");
		String _atCurrentSpeed = Lang.translate("gui.goggles.at_current_speed");
		String _stressUnit = Lang.translate("generic.unit.stress");
		String _baseValue = Lang.translate("gui.goggles.base_value");

		tooltip.add(spacing + _kineticStatsTitle);
		tooltip.add(spacing + GRAY + _stressImpact);

		String addedStress = AQUA + "" + format(stressApplied) + _stressUnit + " " + DARK_GRAY + _atCurrentSpeed;
		String addedStressAtBase = GRAY + "" + format(stressApplied * Math.abs(te.getSpeed())) + _stressUnit + " "
				+ DARK_GRAY + _baseValue;
		tooltip.add(spacing + " " + addedStress);
		tooltip.add(spacing + " " + addedStressAtBase);
	}

	private static void addGeneratorTooltip(BlockState state, List<String> tooltip, GeneratingKineticTileEntity te) {
		String spacing = "    ";
		float addedStressCapacity = te.getAddedStressCapacity();
		if (addedStressCapacity == 0)
			return;

		String _stressUnit = Lang.translate("generic.unit.stress");
		String _atCurrentSpeed = Lang.translate("gui.goggles.at_current_speed");
		String _baseValue = Lang.translate("gui.goggles.base_value");
		String _generatorStatsTitle = Lang.translate("gui.goggles.generator_stats");
		String _capacityProvided = Lang.translate("tooltip.capacityProvided");

		if (te.speed != te.getGeneratedSpeed() && te.speed != 0)
			addedStressCapacity *= (te.getGeneratedSpeed() / te.speed);

		tooltip.add(spacing + _generatorStatsTitle);
		tooltip.add(spacing + GRAY + _capacityProvided);

		float actualSpeed = Math.abs(te.speed);
		float relativeCap = 0;
		if (actualSpeed != 0)
			relativeCap = addedStressCapacity * actualSpeed;

		String addedCapacity = AQUA + "" + format(addedStressCapacity) + _stressUnit + " " + DARK_GRAY
				+ _atCurrentSpeed;
		String addedCapacityAtBase = GRAY + "" + format(relativeCap) + _stressUnit + " " + DARK_GRAY + _baseValue;
		tooltip.add(spacing + " " + addedCapacity);
		tooltip.add(spacing + " " + addedCapacityAtBase);
	}

	private static void addGaugeTooltip(BlockState state, List<String> tooltip, TileEntity te) {

		String _rpmUnit = Lang.translate("generic.unit.rpm");
		String _speedGaugeTitle = Lang.translate("gui.speed_gauge.title");
		String _infoHeader = Lang.translate("gui.gauge.info_header");
		String _overStressed = Lang.translate("gui.stress_gauge.overstressed");
		String _noRotation = Lang.translate("gui.stress_gauge.no_rotation");
		String _capacity = Lang.translate("gui.stress_gauge.capacity");
		String _stressGaugeTitle = Lang.translate("gui.stress_gauge.title");
		String _stressUnit = Lang.translate("generic.unit.stress");
		String _atCurrentSpeed = Lang.translate("gui.goggles.at_current_speed");
		String _baseValue = Lang.translate("gui.goggles.base_value");

		String spacing = "    ";
		tooltip.add(spacing + _infoHeader);

		if (AllBlocks.STRESS_GAUGE.typeOf(state)) {
			if (!(te instanceof StressGaugeTileEntity))
				return;
			StressGaugeTileEntity stressGauge = (StressGaugeTileEntity) te;
			List<String> stressLevels = Lang.translatedOptions("tooltip.stressImpact", "low", "medium", "high");
			double stress = stressGauge.currentStress;
			double cap = stressGauge.maxStress;
			double relStress = stress / (cap == 0 ? 1 : cap);
			StressImpact impactId = relStress > 1 ? null
					: (relStress > .75f) ? StressImpact.HIGH
							: (relStress > .5f ? StressImpact.MEDIUM : StressImpact.LOW);

			TextFormatting color = TextFormatting.RED;
			if (impactId == StressImpact.LOW)
				color = TextFormatting.GREEN;
			if (impactId == StressImpact.MEDIUM)
				color = TextFormatting.YELLOW;
			if (impactId == StressImpact.HIGH)
				color = TextFormatting.GOLD;

			String level = TextFormatting.DARK_RED + ItemDescription.makeProgressBar(3, 2) + "" + _overStressed;
			if (impactId != null) {
				int index = impactId.ordinal();
				level = color + ItemDescription.makeProgressBar(3, index) + stressLevels.get(index);
			}

			level += " (" + (int) (relStress * 100) + "%)";

			float actualSpeed = stressGauge.speed;
			if (actualSpeed == 0)
				level = DARK_GRAY + ItemDescription.makeProgressBar(3, -1) + _noRotation;

			tooltip.add(spacing + GRAY + _stressGaugeTitle);
			tooltip.add(spacing + level);

			if (actualSpeed != 0) {
				tooltip.add(spacing + GRAY + _capacity);

				String capacity = color + "" + format((cap - stress) / Math.abs(actualSpeed)) + _stressUnit + " "
						+ DARK_GRAY + _atCurrentSpeed;
				String capacityAtBase = GRAY + "" + format(cap - stress) + _stressUnit + " " + DARK_GRAY + _baseValue;
				tooltip.add(spacing + " " + capacity);
				tooltip.add(spacing + " " + capacityAtBase);
			}
		}

		if (AllBlocks.SPEED_GAUGE.typeOf(state)) {
			if (!(te instanceof SpeedGaugeTileEntity))
				return;
			SpeedGaugeTileEntity speedGauge = (SpeedGaugeTileEntity) te;
			boolean overstressed = speedGauge.currentStress > speedGauge.maxStress && speedGauge.speed != 0;

			SpeedLevel speedLevel = SpeedLevel.of(speedGauge.speed);
			String color = speedLevel.getTextColor() + "";
			if (overstressed)
				color = DARK_GRAY + "" + TextFormatting.STRIKETHROUGH;

			List<String> speedLevels = Lang.translatedOptions("tooltip.speedRequirement", "none", "medium", "high");
			int index = speedLevel.ordinal();
			String level = color + ItemDescription.makeProgressBar(3, index)
					+ (speedLevel != SpeedLevel.NONE ? speedLevels.get(index) : "");
			level += " (" + format(Math.abs(speedGauge.speed)) + "" + _rpmUnit + ") ";

			tooltip.add(spacing + GRAY + _speedGaugeTitle);
			tooltip.add(spacing + level);

			if (overstressed)
				tooltip.add(spacing + TextFormatting.DARK_RED + _overStressed);
		}
	}

	private static String format(double d) {
		return decimalFormat.format(d);
	}

}
