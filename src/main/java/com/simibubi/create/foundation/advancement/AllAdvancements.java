package com.simibubi.create.foundation.advancement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.ComponentTier;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem.Components;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.PlacedBlockTrigger;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

@SuppressWarnings("unused") // dont warn about unused avancements
public class AllAdvancements implements IDataProvider {

	static final String LANG = "advancement." + Create.ID + ".";

	public void register(Consumer<Advancement> t) {
		String id = Create.ID;

		Advancement root = Advancement.Builder.builder()
			.withDisplay(AllItems.BRASS_HAND.asStack(), new TranslationTextComponent(LANG + "root"),
				new TranslationTextComponent(LANG + "root.desc"),
				new ResourceLocation(Create.ID, "textures/block/palettes/gabbro/bricks.png"), FrameType.TASK, false,
				false, false)
			.withCriterion("0", InventoryChangeTrigger.Instance.forItems(new IItemProvider[] {}))
			.register(t, id + ":root");

		Advancement andesite_alloy =
			advancement("andesite_alloy", AllItems.ANDESITE_ALLOY.get(), TaskType.NORMAL).withParent(root)
				.withCriterion("0", itemGathered(AllItems.ANDESITE_ALLOY.get()))
				.register(t, id + ":andesite_alloy");

		kineticsBranch(t, andesite_alloy);

		Advancement aesthetics =
			advancement("aesthetics", AllBlocks.WOODEN_BRACKET.get(), TaskType.NORMAL).withParent(andesite_alloy)
				.withCriterion("0", AllTriggers.BRACKET_APPLY_TRIGGER.forEntries(AllBlocks.SHAFT.get()))
				.withCriterion("1", AllTriggers.BRACKET_APPLY_TRIGGER.forEntries(AllBlocks.COGWHEEL.get(), AllBlocks.LARGE_COGWHEEL.get()))
				.withCriterion("2", AllTriggers.BRACKET_APPLY_TRIGGER.forEntries(AllBlocks.FLUID_PIPE.get()))
				.register(t, id + ":aesthetics");

		Advancement reinforced =
			advancement("reinforced", AllBlocks.ANDESITE_ENCASED_SHAFT.get(), TaskType.NORMAL).withParent(aesthetics)
				.withCriterion("0", AllTriggers.CASING_SHAFT.instance())
				.withCriterion("1", AllTriggers.CASING_BELT.instance())
				.withCriterion("2", AllTriggers.CASING_PIPE.instance())
				.register(t, id + ":reinforced");

		Advancement water_wheel =
			advancement("water_wheel", AllBlocks.WATER_WHEEL.get(), TaskType.NORMAL).withParent(andesite_alloy)
				.withCriterion("0", placeBlock(AllBlocks.WATER_WHEEL.get()))
				.withCriterion("1", AllTriggers.WATER_WHEEL.instance())
				.register(t, id + ":water_wheel");

		Advancement lava_wheel = advancement("lava_wheel", Items.LAVA_BUCKET, TaskType.SECRET).withParent(water_wheel)
			.withCriterion("0", AllTriggers.LAVA_WHEEL.instance())
			.register(t, id + ":lava_wheel");

		Advancement chocolate_wheel = advancement("chocolate_wheel", AllFluids.CHOCOLATE.get()
			.getFilledBucket(), TaskType.SECRET).withParent(water_wheel)
				.withCriterion("0", AllTriggers.CHOCOLATE_WHEEL.instance())
				.register(t, id + ":chocolate_wheel");

		Advancement millstone =
			kinecticAdvancement("millstone", AllBlocks.MILLSTONE.get(), TaskType.NORMAL).withParent(andesite_alloy)
				.register(t, id + ":millstone");

		Advancement cuckoo =
			advancement("cuckoo", AllBlocks.CUCKOO_CLOCK.get(), TaskType.NORMAL).withParent(andesite_alloy)
				.withCriterion("0", AllTriggers.CUCKOO.instance())
				.register(t, id + ":cuckoo");

		Advancement windmill =
			advancement("windmill", AllBlocks.WINDMILL_BEARING.get(), TaskType.NORMAL).withParent(andesite_alloy)
				.withCriterion("0", AllTriggers.WINDMILL.instance())
				.register(t, id + ":windmill");

		Advancement maxed_windmill =
			advancement("maxed_windmill", AllBlocks.WINDMILL_BEARING.get(), TaskType.GOAL).withParent(windmill)
				.withCriterion("0", AllTriggers.MAXED_WINDMILL.instance())
				.register(t, id + ":maxed_windmill");

		Advancement andesite_casing =
			advancement("andesite_casing", AllBlocks.ANDESITE_CASING.get(), TaskType.GOAL).withParent(andesite_alloy)
				.withCriterion("0", itemGathered(AllBlocks.ANDESITE_CASING.get()))
				.register(t, id + ":andesite_casing");

		Advancement drill = kinecticAdvancement("mechanical_drill", AllBlocks.MECHANICAL_DRILL.get(), TaskType.NORMAL)
			.withParent(andesite_casing)
			.register(t, id + ":mechanical_drill");

		Advancement press =
			advancement("press", AllBlocks.MECHANICAL_PRESS.get(), TaskType.MILESTONE).withParent(andesite_casing)
				.withCriterion("0", AllTriggers.BONK.instance())
				.register(t, id + ":press");

		Advancement fan = advancement("fan", AllBlocks.ENCASED_FAN.get(), TaskType.NORMAL).withParent(press)
			.withCriterion("0", AllTriggers.FAN.instance())
			.register(t, id + ":fan");

		Advancement fan_lava = advancement("fan_lava", Items.LAVA_BUCKET, TaskType.NORMAL).withParent(fan)
			.withCriterion("0", AllTriggers.FAN_LAVA.instance())
			.register(t, id + ":fan_lava");

		Advancement fan_smoke = advancement("fan_smoke", Items.CAMPFIRE, TaskType.NORMAL).withParent(fan)
			.withCriterion("0", AllTriggers.FAN_SMOKE.instance())
			.register(t, id + ":fan_smoke");

		Advancement fan_water = advancement("fan_water", Items.WATER_BUCKET, TaskType.NORMAL).withParent(fan)
			.withCriterion("0", AllTriggers.FAN_WATER.instance())
			.register(t, id + ":fan_water");

		Advancement rose_quartz =
			itemAdvancement("polished_rose_quartz", AllItems.POLISHED_ROSE_QUARTZ, TaskType.NORMAL)
				.withParent(andesite_casing)
				.register(t, id + ":polished_rose_quartz");

		Advancement electron_tube =
			itemAdvancement("electron_tube", AllItems.ELECTRON_TUBE, TaskType.NORMAL).withParent(rose_quartz)
				.register(t, id + ":electron_tube");

		Advancement saw =
			kinecticAdvancement("mechanical_saw", AllBlocks.MECHANICAL_SAW.get(), TaskType.NORMAL).withParent(press)
				.register(t, id + ":mechanical_saw");

		Advancement basin = advancement("basin", AllBlocks.BASIN.get(), TaskType.NORMAL).withParent(press)
			.withCriterion("0", placeBlock(AllBlocks.BASIN.get()))
			.withCriterion("1", AllTriggers.BASIN_THROW.instance())
			.register(t, id + ":basin");

		Advancement mixer = advancement("mixer", AllBlocks.MECHANICAL_MIXER.get(), TaskType.MILESTONE)
			.withCriterion("0", placeBlock(AllBlocks.MECHANICAL_MIXER.get()))
			.withCriterion("1", isPowered(AllBlocks.MECHANICAL_MIXER.get()))
			.withCriterion("2", AllTriggers.MIXER_MIX.instance())
			.withParent(basin)
			.register(t, id + ":mixer");

		Advancement compact = advancement("compact", Blocks.IRON_BLOCK, TaskType.NORMAL)
			.withCriterion("0", AllTriggers.PRESS_COMPACT.instance())
			.withParent(basin)
			.register(t, id + ":compact");

		Advancement blaze_burner =
			itemAdvancement("blaze_burner", AllBlocks.BLAZE_BURNER, TaskType.NORMAL).withParent(mixer)
				.register(t, id + ":blaze_burner");

		Advancement brass = itemAdvancement("brass", AllItems.BRASS_INGOT, TaskType.NORMAL).withParent(blaze_burner)
			.register(t, id + ":brass");

		brassAge(t, brass);
		copperAge(t, press);
	}

	void kineticsBranch(Consumer<Advancement> t, Advancement root) {
		String id = Create.ID;

		Advancement its_alive = advancement("its_alive", AllBlocks.COGWHEEL.get(), TaskType.NORMAL).withParent(root)
			.withCriterion("0", AllTriggers.ROTATION.instance())
			.register(t, id + ":its_alive");

		Advancement belt = advancement("belt", AllItems.BELT_CONNECTOR.get(), TaskType.NORMAL).withParent(its_alive)
			.withCriterion("0", AllTriggers.CONNECT_BELT.instance())
			.register(t, id + ":belt");

		Advancement tunnel = advancement("tunnel", AllBlocks.ANDESITE_TUNNEL.get(), TaskType.NORMAL).withParent(belt)
			.withCriterion("0", AllTriggers.PLACE_TUNNEL.instance())
			.register(t, id + ":tunnel");

		Advancement splitter_tunnel =
			advancement("splitter_tunnel", AllBlocks.BRASS_TUNNEL.get(), TaskType.MILESTONE).withParent(tunnel)
				.withCriterion("0", AllTriggers.CONNECT_TUNNEL.instance())
				.register(t, id + ":splitter_tunnel");

		Advancement chute = advancement("chute", AllBlocks.CHUTE.get(), TaskType.NORMAL).withParent(belt)
			.withCriterion("0", placeBlock(AllBlocks.CHUTE.get()))
			.register(t, id + ":chute");

		Advancement upward_chute =
			advancement("upward_chute", AllBlocks.ENCASED_FAN.get(), TaskType.NORMAL).withParent(chute)
				.withCriterion("0", AllTriggers.UPWARD_CHUTE.instance())
				.register(t, id + ":upward_chute");

		Advancement belt_funnel =
			advancement("belt_funnel", AllBlocks.ANDESITE_FUNNEL.get(), TaskType.NORMAL).withParent(belt)
				.withCriterion("0", AllTriggers.BELT_FUNNEL.instance())
				.register(t, id + ":belt_funnel");

		Advancement belt_funnel_kiss =
			advancement("belt_funnel_kiss", AllBlocks.BRASS_FUNNEL.get(), TaskType.SECRET).withParent(belt_funnel)
				.withCriterion("0", AllTriggers.BELT_FUNNEL_KISS.instance())
				.register(t, id + ":belt_funnel_kiss");

		Advancement wrench = itemAdvancement("wrench", AllItems.WRENCH, TaskType.NORMAL).withParent(its_alive)
			.register(t, id + ":wrench");

		Advancement goggles = itemAdvancement("goggles", AllItems.GOGGLES, TaskType.NORMAL).withParent(its_alive)
			.register(t, id + ":goggles");

		Advancement speed_gauge =
			kinecticAdvancement("speedometer", AllBlocks.SPEEDOMETER.get(), TaskType.NORMAL).withParent(goggles)
				.register(t, id + ":speedometer");

		Advancement stress_gauge =
			kinecticAdvancement("stressometer", AllBlocks.STRESSOMETER.get(), TaskType.NORMAL).withParent(goggles)
				.register(t, id + ":stressometer");

		Advancement shifting_gears =
			advancement("shifting_gears", AllBlocks.LARGE_COGWHEEL.get(), TaskType.NORMAL).withParent(its_alive)
				.withCriterion("0", AllTriggers.SHIFTING_GEARS.instance())
				.register(t, id + ":shifting_gears");

		Advancement overstressed = advancement("overstressed", Items.BARRIER, TaskType.SECRET).withParent(its_alive)
			.withCriterion("0", AllTriggers.OVERSTRESSED.instance())
			.register(t, id + ":overstressed");

	}

	void copperAge(Consumer<Advancement> t, Advancement root) {
		String id = Create.ID;

		Advancement copper_casing =
			advancement("copper_casing", AllBlocks.COPPER_CASING.get(), TaskType.GOAL).withParent(root)
				.withCriterion("0", itemGathered(AllBlocks.COPPER_CASING.get()))
				.register(t, id + ":copper_casing");

		Advancement item_drain =
			advancement("item_drain", AllBlocks.ITEM_DRAIN.get(), TaskType.NORMAL).withParent(copper_casing)
				.withCriterion("0", AllTriggers.ITEM_DRAIN.instance())
				.register(t, id + ":item_drain");

		Advancement chained_item_drain =
			advancement("chained_item_drain", AllBlocks.ITEM_DRAIN.get(), TaskType.SECRET).withParent(item_drain)
				.withCriterion("0", AllTriggers.CHAINED_ITEM_DRAIN.instance())
				.register(t, id + ":chained_item_drain");

		Advancement spout = advancement("spout", AllBlocks.SPOUT.get(), TaskType.NORMAL).withParent(copper_casing)
			.withCriterion("0", AllTriggers.SPOUT.instance())
			.register(t, id + ":spout");

		Advancement spout_potion = advancement("spout_potion", Items.POTION, TaskType.GOAL).withParent(spout)
			.withCriterion("0", AllTriggers.SPOUT_POTION.instance())
			.register(t, id + ":spout_potion");

		Advancement chocolate = itemAdvancement("chocolate", () -> AllFluids.CHOCOLATE.get()
			.getFilledBucket(), TaskType.GOAL).withParent(spout)
				.register(t, id + ":chocolate");

		Advancement glass_pipe =
			advancement("glass_pipe", AllBlocks.FLUID_PIPE.get(), TaskType.NORMAL).withParent(copper_casing)
				.withCriterion("0", AllTriggers.GLASS_PIPE.instance())
				.register(t, id + ":glass_pipe");

		Advancement pipe_collision =
			advancement("pipe_collision", AllBlocks.FLUID_VALVE.get(), TaskType.NORMAL).withParent(glass_pipe)
				.withCriterion("0", AllTriggers.PIPE_COLLISION.instance())
				.register(t, id + ":pipe_collision");

		Advancement pipe_spill = advancement("pipe_spill", Items.BUCKET, TaskType.NORMAL).withParent(glass_pipe)
			.withCriterion("0", AllTriggers.PIPE_SPILL.instance())
			.register(t, id + ":pipe_spill");

		Advancement hose_pulley =
			advancement("hose_pulley", AllBlocks.HOSE_PULLEY.get(), TaskType.NORMAL).withParent(pipe_spill)
				.withCriterion("0", AllTriggers.HOSE_PULLEY.instance())
				.register(t, id + ":hose_pulley");

		Advancement infinite_water =
			advancement("infinite_water", Items.WATER_BUCKET, TaskType.NORMAL).withParent(hose_pulley)
				.withCriterion("0", isInfinite(Fluids.WATER))
				.register(t, id + ":infinite_water");

		Advancement infinite_lava =
			advancement("infinite_lava", Items.LAVA_BUCKET, TaskType.GOAL).withParent(hose_pulley)
				.withCriterion("0", isInfinite(Fluids.LAVA))
				.register(t, id + ":infinite_lava");

		Advancement infinite_chocolate = advancement("infinite_chocolate", AllFluids.CHOCOLATE.get()
			.getFilledBucket(), TaskType.CHALLENGE).withParent(hose_pulley)
				.withCriterion("0", isInfinite(AllFluids.CHOCOLATE.get()))
				.register(t, id + ":infinite_chocolate");
	}

	void brassAge(Consumer<Advancement> t, Advancement root) {
		String id = Create.ID;

		Advancement brass_casing =
			advancement("brass_casing", AllBlocks.BRASS_CASING.get(), TaskType.GOAL).withParent(root)
				.withCriterion("0", itemGathered(AllBlocks.BRASS_CASING.get()))
				.register(t, id + ":brass_casing");

		Advancement nixie_tube =
			advancement("nixie_tube", AllBlocks.NIXIE_TUBE.get(), TaskType.NORMAL).withParent(brass_casing)
				.withCriterion("0", placeBlock(AllBlocks.NIXIE_TUBE.get()))
				.register(t, id + ":nixie_tube");

		Advancement crafter = kinecticAdvancement("crafter", AllBlocks.MECHANICAL_CRAFTER.get(), TaskType.MILESTONE)
			.withParent(brass_casing)
			.register(t, id + ":crafter");

		Advancement flywheel = advancement("flywheel", AllBlocks.FLYWHEEL.get(), TaskType.NORMAL).withParent(crafter)
			.withCriterion("0", AllTriggers.FLYWHEEL.instance())
			.register(t, id + ":flywheel");

		Advancement overstress_flywheel =
			advancement("overstress_flywheel", AllBlocks.FURNACE_ENGINE.get(), TaskType.CHALLENGE).withParent(flywheel)
				.withCriterion("0", AllTriggers.OVERSTRESS_FLYWHEEL.instance())
				.register(t, id + ":overstress_flywheel");

		Advancement integrated_circuit =
			itemAdvancement("integrated_circuit", AllItems.INTEGRATED_CIRCUIT, TaskType.NORMAL).withParent(crafter)
				.register(t, id + ":integrated_circuit");

		Advancement integrated_circuit_eob = deadEnd().withParent(integrated_circuit)
			.withCriterion("0", itemGathered(AllItems.INTEGRATED_CIRCUIT.get()))
			.register(t, id + ":integrated_circuit_eob");

		Advancement speed_controller =
			kinecticAdvancement("speed_controller", AllBlocks.ROTATION_SPEED_CONTROLLER.get(), TaskType.NORMAL)
				.withParent(integrated_circuit)
				.register(t, id + ":speed_controller");

		Advancement clockwork_bearing =
			advancement("clockwork_bearing", AllBlocks.CLOCKWORK_BEARING.get(), TaskType.NORMAL)
				.withParent(brass_casing)
				.withCriterion("0", AllTriggers.CLOCKWORK_BEARING.instance())
				.register(t, id + ":clockwork_bearing");

		Advancement extendo_grip =
			advancement("extendo_grip", AllItems.EXTENDO_GRIP.get(), TaskType.NORMAL).withParent(crafter)
				.withCriterion("0", AllTriggers.EXTENDO.instance())
				.register(t, id + ":extendo_grip");

		Advancement dual_extendo_grip =
			advancement("dual_extendo_grip", AllItems.EXTENDO_GRIP.get(), TaskType.SECRET).withParent(extendo_grip)
				.withCriterion("0", AllTriggers.GIGA_EXTENDO.instance())
				.register(t, id + ":dual_extendo_grip");

		Advancement mechanical_arm = advancement("mechanical_arm", AllBlocks.MECHANICAL_ARM.get(), TaskType.MILESTONE)
			.withCriterion("0", AllTriggers.MECHANICAL_ARM.instance())
			.withParent(brass_casing)
			.register(t, id + ":mechanical_arm");

		Advancement musical_arm = advancement("musical_arm", Items.MUSIC_DISC_13, TaskType.MILESTONE)
			.withCriterion("0", AllTriggers.MUSICAL_ARM.instance())
			.withParent(mechanical_arm)
			.register(t, id + ":musical_arm");

		Advancement arm_many_targets = advancement("arm_many_targets", AllBlocks.BRASS_FUNNEL.get(), TaskType.MILESTONE)
			.withCriterion("0", AllTriggers.ARM_MANY_TARGETS.instance())
			.withParent(mechanical_arm)
			.register(t, id + ":arm_many_targets");

		Advancement arm_blaze_burner = advancement("arm_blaze_burner", AllBlocks.BLAZE_BURNER.get(), TaskType.NORMAL)
			.withCriterion("0", AllTriggers.ARM_BLAZE_BURNER.instance())
			.withParent(mechanical_arm)
			.register(t, id + ":arm_blaze_burner");

		Advancement deployer =
			kinecticAdvancement("deployer", AllBlocks.DEPLOYER.get(), TaskType.MILESTONE).withParent(brass_casing)
				.register(t, id + ":deployer");

		Advancement fist_bump = advancement("fist_bump", AllBlocks.DEPLOYER.get(), TaskType.SECRET).withParent(deployer)
			.withCriterion("0", AllTriggers.DEPLOYER_BOOP.instance())
			.register(t, id + ":fist_bump");

		Advancement crushing_wheel =
			advancement("crushing_wheel", AllBlocks.CRUSHING_WHEEL.get(), TaskType.MILESTONE).withParent(crafter)
				.withCriterion("0", itemGathered(AllBlocks.CRUSHING_WHEEL.get()))
				.register(t, id + ":crushing_wheel");

		Advancement blaze_cake =
			itemAdvancement("blaze_cake", AllItems.BLAZE_CAKE, TaskType.NORMAL).withParent(crushing_wheel)
				.register(t, id + ":blaze_cake");

		Advancement chromatic_compound =
			itemAdvancement("chromatic_compound", AllItems.CHROMATIC_COMPOUND, TaskType.NORMAL).withParent(blaze_cake)
				.register(t, id + ":chromatic_compound");

		Advancement shadow_steel =
			itemAdvancement("shadow_steel", AllItems.SHADOW_STEEL, TaskType.GOAL).withParent(chromatic_compound)
				.register(t, id + ":shadow_steel");

		Advancement refined_radiance =
			itemAdvancement("refined_radiance", AllItems.REFINED_RADIANCE, TaskType.GOAL).withParent(chromatic_compound)
				.register(t, id + ":refined_radiance");

		Advancement chromatic_age = advancement("chromatic_age", AllBlocks.REFINED_RADIANCE_CASING.get(), TaskType.GOAL)
			.withParent(chromatic_compound)
			.withCriterion("0", itemGathered(AllBlocks.SHADOW_STEEL_CASING.get()))
			.withCriterion("1", itemGathered(AllBlocks.REFINED_RADIANCE_CASING.get()))
			.register(t, id + "chromatic_age");

		Advancement chromatic_eob = deadEnd().withParent(chromatic_age)
			.withCriterion("0", itemGathered(AllBlocks.SHADOW_STEEL_CASING.get()))
			.withCriterion("1", itemGathered(AllBlocks.REFINED_RADIANCE_CASING.get()))
			.register(t, id + ":chromatic_eob");

		Advancement deforester =
			itemAdvancement("deforester", AllItems.DEFORESTER, TaskType.NORMAL).withParent(refined_radiance)
				.register(t, id + ":deforester");

		Advancement zapper =
			itemAdvancement("zapper", AllItems.BLOCKZAPPER, TaskType.NORMAL).withParent(refined_radiance)
				.register(t, id + ":zapper");

		ItemStack gunWithPurpurStuff = AllItems.BLOCKZAPPER.asStack();
		for (Components c : Components.values())
			BlockzapperItem.setTier(c, ComponentTier.Chromatic, gunWithPurpurStuff);
		Advancement upgraded_zapper = advancement("upgraded_zapper", gunWithPurpurStuff, TaskType.CHALLENGE)
			.withCriterion("0", AllTriggers.UPGRADED_ZAPPER.instance())
			.withParent(zapper)
			.register(t, id + ":upgraded_zapper");

		Advancement symmetry_wand =
			itemAdvancement("wand_of_symmetry", AllItems.WAND_OF_SYMMETRY, TaskType.NORMAL).withParent(refined_radiance)
				.register(t, id + ":wand_of_symmetry");

	}

	// Datagen

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting()
		.create();
	private final DataGenerator generator;

	public AllAdvancements(DataGenerator generatorIn) {
		this.generator = generatorIn;
	}

	@Override
	public void act(DirectoryCache cache) throws IOException {
		Path path = this.generator.getOutputFolder();
		Set<ResourceLocation> set = Sets.newHashSet();
		Consumer<Advancement> consumer = (p_204017_3_) -> {
			if (!set.add(p_204017_3_.getId()))
				throw new IllegalStateException("Duplicate advancement " + p_204017_3_.getId());

			Path path1 = getPath(path, p_204017_3_);

			try {
				IDataProvider.save(GSON, cache, p_204017_3_.copy()
					.serialize(), path1);
			} catch (IOException ioexception) {
				LOGGER.error("Couldn't save advancement {}", path1, ioexception);
			}
		};

		register(consumer);
	}

	private static Path getPath(Path pathIn, Advancement advancementIn) {
		return pathIn.resolve("data/" + advancementIn.getId()
			.getNamespace() + "/advancements/"
			+ advancementIn.getId()
				.getPath()
			+ ".json");
	}

	@Override
	public String getName() {
		return "Create's Advancements";
	}

	public PlacedBlockTrigger.Instance placeBlock(Block block) {
		return PlacedBlockTrigger.Instance.placedBlock(block);
	}

	public RegistryTrigger.Instance<Block> isPowered(Block block) {
		return AllTriggers.KINETIC_BLOCK.forEntries(block);
	}

	public RegistryTrigger.Instance<Fluid> isInfinite(FlowingFluid fluid) {
		return AllTriggers.INFINITE_FLUID.forEntries(fluid.getStillFluid());
	}

	public InventoryChangeTrigger.Instance itemGathered(IItemProvider itemprovider) {
		return InventoryChangeTrigger.Instance.forItems(itemprovider);
	}

	static enum TaskType {

		NORMAL(FrameType.TASK, true, false, false),
		MILESTONE(FrameType.TASK, true, true, false),
		GOAL(FrameType.GOAL, true, true, false),
		SECRET(FrameType.GOAL, true, true, true),
		SILENT_GATE(FrameType.CHALLENGE, false, false, false),
		CHALLENGE(FrameType.CHALLENGE, true, true, false),

		;

		private FrameType frame;
		private boolean toast;
		private boolean announce;
		private boolean hide;

		private TaskType(FrameType frame, boolean toast, boolean announce, boolean hide) {
			this.frame = frame;
			this.toast = toast;
			this.announce = announce;
			this.hide = hide;
		}
	}

	public Builder kinecticAdvancement(String name, Block block, TaskType type) {
		return advancement(name, block, type).withCriterion("0", placeBlock(block));
//			.withCriterion("1", isPowered(block)); Duplicate toast
	}

	public Builder advancement(String name, IItemProvider icon, TaskType type) {
		return advancement(name, new ItemStack(icon), type);
	}

	public Builder deadEnd() {
		return advancement("eob", Items.OAK_SAPLING, TaskType.SILENT_GATE);
	}

	public Builder advancement(String name, ItemStack icon, TaskType type) {
		return Advancement.Builder.builder()
			.withDisplay(icon, new TranslationTextComponent(LANG + name),
				new TranslationTextComponent(LANG + name + ".desc"), null, type.frame, type.toast, type.announce,
				type.hide);
	}

	public Builder itemAdvancement(String name, Supplier<? extends IItemProvider> item, TaskType type) {
		return advancement(name, item.get(), type).withCriterion("0", itemGathered(item.get()));
	}

}
