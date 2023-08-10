package com.simibubi.create.foundation.item;

import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.GRAY;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.kinetics.BlockStressValues;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.content.kinetics.crank.ValveHandleBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;

import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.catnip.utility.lang.Lang;
import net.createmod.catnip.utility.lang.LangBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class KineticStats implements TooltipModifier {
	protected final Block block;

	public KineticStats(Block block) {
		this.block = block;
	}

	@Nullable
	public static KineticStats create(Item item) {
		if (item instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();
			if (block instanceof IRotate || block instanceof SteamEngineBlock) {
				return new KineticStats(block);
			}
		}
		return null;
	}

	@Override
	public void modify(ItemTooltipEvent context) {
		List<Component> kineticStats = getKineticStats(block, context.getPlayer());
		if (!kineticStats.isEmpty()) {
			List<Component> tooltip = context.getToolTip();
			tooltip.add(Components.immutableEmpty());
			tooltip.addAll(kineticStats);
		}
	}

	public static List<Component> getKineticStats(Block block, Player player) {
		List<Component> list = new ArrayList<>();

		CKinetics config = AllConfigs.server().kinetics;
		LangBuilder rpmUnit = CreateLang.translate("generic.unit.rpm");
		LangBuilder suUnit = CreateLang.translate("generic.unit.stress");

		boolean hasGoggles = GogglesItem.isWearingGoggles(player);

		boolean showStressImpact;
		if (block instanceof IRotate) {
			showStressImpact = !((IRotate) block).hideStressImpact();
		} else {
			showStressImpact = true;
		}

		if (block instanceof ValveHandleBlock)
			block = AllBlocks.COPPER_VALVE_HANDLE.get();

		boolean hasStressImpact =
			StressImpact.isEnabled() && showStressImpact && BlockStressValues.getImpact(block) > 0;
		boolean hasStressCapacity = StressImpact.isEnabled() && BlockStressValues.hasCapacity(block);

		if (hasStressImpact) {
			CreateLang.translate("tooltip.stressImpact")
				.style(GRAY)
				.addTo(list);

			double impact = BlockStressValues.getImpact(block);
			StressImpact impactId = impact >= config.highStressImpact.get() ? StressImpact.HIGH
				: (impact >= config.mediumStressImpact.get() ? StressImpact.MEDIUM : StressImpact.LOW);
			LangBuilder builder = CreateLang.builder()
				.add(CreateLang.text(TooltipHelper.makeProgressBar(3, impactId.ordinal() + 1))
					.style(impactId.getAbsoluteColor()));

			if (hasGoggles) {
				builder.add(CreateLang.number(impact))
					.text("x ")
					.add(rpmUnit)
					.addTo(list);
			} else
				builder.translate("tooltip.stressImpact." + Lang.asId(impactId.name()))
					.addTo(list);
		}

		if (hasStressCapacity) {
			CreateLang.translate("tooltip.capacityProvided")
				.style(GRAY)
				.addTo(list);

			double capacity = BlockStressValues.getCapacity(block);
			Couple<Integer> generatedRPM = BlockStressValues.getGeneratedRPM(block);

			StressImpact impactId = capacity >= config.highCapacity.get() ? StressImpact.HIGH
				: (capacity >= config.mediumCapacity.get() ? StressImpact.MEDIUM : StressImpact.LOW);
			StressImpact opposite = StressImpact.values()[StressImpact.values().length - 2 - impactId.ordinal()];
			LangBuilder builder = CreateLang.builder()
				.add(CreateLang.text(TooltipHelper.makeProgressBar(3, impactId.ordinal() + 1))
					.style(opposite.getAbsoluteColor()));

			if (hasGoggles) {
				builder.add(CreateLang.number(capacity))
					.text("x ")
					.add(rpmUnit)
					.addTo(list);

				if (generatedRPM != null) {
					LangBuilder amount = CreateLang.number(capacity * generatedRPM.getSecond())
						.add(suUnit);
					CreateLang.text(" -> ")
						.add(!generatedRPM.getFirst()
							.equals(generatedRPM.getSecond()) ? CreateLang.translate("tooltip.up_to", amount) : amount)
						.style(DARK_GRAY)
						.addTo(list);
				}
			} else
				builder.translate("tooltip.capacityProvided." + Lang.asId(impactId.name()))
					.addTo(list);
		}

		return list;
	}
}
