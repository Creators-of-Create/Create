package com.simibubi.create.foundation.advancement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlacedBlockTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

@SuppressWarnings("unused") // dont warn about unused avancements
public class AllAdvancements implements DataProvider {

	static final String LANG = "advancement." + Create.ID + ".";

	public void register(Consumer<Advancement> t) {
		String id = Create.ID;

		Advancement root = Advancement.Builder.advancement()
			.display(AllItems.BRASS_HAND.asStack(), new TranslatableComponent(LANG + "root"),
				new TranslatableComponent(LANG + "root.desc"),
				new ResourceLocation(Create.ID, "textures/gui/advancements.png"), FrameType.TASK, false,
				false, false)
			.addCriterion("0", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[] {}))
			.save(t, id + ":root");

		Advancement andesite_alloy =
			advancement("andesite_alloy", AllItems.ANDESITE_ALLOY.get(), TaskType.NORMAL).parent(root)
				.addCriterion("0", itemGathered(AllItems.ANDESITE_ALLOY.get()))
				.save(t, id + ":andesite_alloy");

		kineticsBranch(t, andesite_alloy);

		Advancement aesthetics = advancement("aesthetics", AllBlocks.WOODEN_BRACKET.get(), TaskType.NORMAL)
			.parent(andesite_alloy)
			.addCriterion("0", AllTriggers.BRACKET_APPLY_TRIGGER.forEntries(AllBlocks.SHAFT.get()))
			.addCriterion("1",
				AllTriggers.BRACKET_APPLY_TRIGGER.forEntries(AllBlocks.COGWHEEL.get(), AllBlocks.LARGE_COGWHEEL.get()))
			.addCriterion("2", AllTriggers.BRACKET_APPLY_TRIGGER.forEntries(AllBlocks.FLUID_PIPE.get()))
			.save(t, id + ":aesthetics");

		Advancement reinforced =
			advancement("reinforced", AllBlocks.ANDESITE_ENCASED_SHAFT.get(), TaskType.NORMAL).parent(aesthetics)
				.addCriterion("0", AllTriggers.CASING_SHAFT.instance())
				.addCriterion("1", AllTriggers.CASING_BELT.instance())
				.addCriterion("2", AllTriggers.CASING_PIPE.instance())
				.save(t, id + ":reinforced");

		Advancement water_wheel =
			advancement("water_wheel", AllBlocks.WATER_WHEEL.get(), TaskType.NORMAL).parent(andesite_alloy)
				.addCriterion("0", placeBlock(AllBlocks.WATER_WHEEL.get()))
				.addCriterion("1", AllTriggers.WATER_WHEEL.instance())
				.save(t, id + ":water_wheel");

		Advancement lava_wheel = advancement("lava_wheel", Items.LAVA_BUCKET, TaskType.SECRET).parent(water_wheel)
			.addCriterion("0", AllTriggers.LAVA_WHEEL.instance())
			.save(t, id + ":lava_wheel");

		Advancement chocolate_wheel = advancement("chocolate_wheel", AllFluids.CHOCOLATE.get()
			.getBucket(), TaskType.SECRET).parent(water_wheel)
				.addCriterion("0", AllTriggers.CHOCOLATE_WHEEL.instance())
				.save(t, id + ":chocolate_wheel");

		Advancement millstone =
			kinecticAdvancement("millstone", AllBlocks.MILLSTONE.get(), TaskType.NORMAL).parent(andesite_alloy)
				.save(t, id + ":millstone");

		Advancement cuckoo =
			advancement("cuckoo", AllBlocks.CUCKOO_CLOCK.get(), TaskType.NORMAL).parent(andesite_alloy)
				.addCriterion("0", AllTriggers.CUCKOO.instance())
				.save(t, id + ":cuckoo");

		Advancement windmill =
			advancement("windmill", AllBlocks.WINDMILL_BEARING.get(), TaskType.NORMAL).parent(andesite_alloy)
				.addCriterion("0", AllTriggers.WINDMILL.instance())
				.save(t, id + ":windmill");

		Advancement maxed_windmill =
			advancement("maxed_windmill", AllBlocks.WINDMILL_BEARING.get(), TaskType.GOAL).parent(windmill)
				.addCriterion("0", AllTriggers.MAXED_WINDMILL.instance())
				.save(t, id + ":maxed_windmill");

		Advancement andesite_casing =
			advancement("andesite_casing", AllBlocks.ANDESITE_CASING.get(), TaskType.GOAL).parent(andesite_alloy)
				.addCriterion("0", itemGathered(AllBlocks.ANDESITE_CASING.get()))
				.save(t, id + ":andesite_casing");

		Advancement drill = kinecticAdvancement("mechanical_drill", AllBlocks.MECHANICAL_DRILL.get(), TaskType.NORMAL)
			.parent(andesite_casing)
			.save(t, id + ":mechanical_drill");

		Advancement press =
			advancement("press", AllBlocks.MECHANICAL_PRESS.get(), TaskType.MILESTONE).parent(andesite_casing)
				.addCriterion("0", AllTriggers.BONK.instance())
				.save(t, id + ":press");

		Advancement fan = advancement("fan", AllBlocks.ENCASED_FAN.get(), TaskType.NORMAL).parent(press)
			.addCriterion("0", AllTriggers.FAN_PROCESSING.forEntries(InWorldProcessing.Type.NONE))
			.save(t, id + ":fan");

		Advancement fan_lava = advancement("fan_lava", Items.LAVA_BUCKET, TaskType.NORMAL).parent(fan)
			.addCriterion("0", AllTriggers.FAN_PROCESSING.forEntries(InWorldProcessing.Type.BLASTING))
			.save(t, id + ":fan_lava");

		Advancement fan_smoke = advancement("fan_smoke", Items.CAMPFIRE, TaskType.NORMAL).parent(fan)
			.addCriterion("0", AllTriggers.FAN_PROCESSING.forEntries(InWorldProcessing.Type.SMOKING))
			.save(t, id + ":fan_smoke");

		Advancement fan_water = advancement("fan_water", Items.WATER_BUCKET, TaskType.NORMAL).parent(fan)
			.addCriterion("0", AllTriggers.FAN_PROCESSING.forEntries(InWorldProcessing.Type.SPLASHING))
			.save(t, id + ":fan_water");

		Advancement rose_quartz =
			itemAdvancement("polished_rose_quartz", AllItems.POLISHED_ROSE_QUARTZ, TaskType.NORMAL)
				.parent(andesite_casing)
				.save(t, id + ":polished_rose_quartz");

		Advancement electron_tube =
			itemAdvancement("electron_tube", AllItems.ELECTRON_TUBE, TaskType.NORMAL).parent(rose_quartz)
				.save(t, id + ":electron_tube");

		Advancement saw =
			kinecticAdvancement("mechanical_saw", AllBlocks.MECHANICAL_SAW.get(), TaskType.NORMAL).parent(press)
				.save(t, id + ":mechanical_saw");

		Advancement basin = advancement("basin", AllBlocks.BASIN.get(), TaskType.NORMAL).parent(press)
			.addCriterion("0", placeBlock(AllBlocks.BASIN.get()))
			.addCriterion("1", AllTriggers.BASIN_THROW.instance())
			.save(t, id + ":basin");

		Advancement mixer = advancement("mixer", AllBlocks.MECHANICAL_MIXER.get(), TaskType.MILESTONE)
			.addCriterion("0", placeBlock(AllBlocks.MECHANICAL_MIXER.get()))
			.addCriterion("1", AllTriggers.MIXER_MIX.instance())
			.parent(basin)
			.save(t, id + ":mixer");

		Advancement compact = advancement("compact", Blocks.IRON_BLOCK, TaskType.NORMAL)
			.addCriterion("0", AllTriggers.PRESS_COMPACT.instance())
			.parent(basin)
			.save(t, id + ":compact");

		Advancement blaze_burner =
			itemAdvancement("blaze_burner", AllBlocks.BLAZE_BURNER, TaskType.NORMAL).parent(mixer)
				.save(t, id + ":blaze_burner");

		Advancement brass = itemAdvancement("brass", AllItems.BRASS_INGOT, TaskType.NORMAL).parent(blaze_burner)
			.save(t, id + ":brass");

		brassAge(t, brass);
		copperAge(t, press);
	}

	void kineticsBranch(Consumer<Advancement> t, Advancement root) {
		String id = Create.ID;

		Advancement its_alive = advancement("its_alive", AllBlocks.COGWHEEL.get(), TaskType.NORMAL).parent(root)
			.addCriterion("0", AllTriggers.ROTATION.instance())
			.save(t, id + ":its_alive");

		Advancement belt = advancement("belt", AllItems.BELT_CONNECTOR.get(), TaskType.NORMAL).parent(its_alive)
			.addCriterion("0", AllTriggers.CONNECT_BELT.instance())
			.save(t, id + ":belt");

		Advancement tunnel = advancement("tunnel", AllBlocks.ANDESITE_TUNNEL.get(), TaskType.NORMAL).parent(belt)
			.addCriterion("0", AllTriggers.PLACE_TUNNEL.instance())
			.save(t, id + ":tunnel");

		Advancement splitter_tunnel =
			advancement("splitter_tunnel", AllBlocks.BRASS_TUNNEL.get(), TaskType.MILESTONE).parent(tunnel)
				.addCriterion("0", AllTriggers.CONNECT_TUNNEL.instance())
				.save(t, id + ":splitter_tunnel");

		Advancement chute = advancement("chute", AllBlocks.CHUTE.get(), TaskType.NORMAL).parent(belt)
			.addCriterion("0", placeBlock(AllBlocks.CHUTE.get()))
			.save(t, id + ":chute");

		Advancement upward_chute =
			advancement("upward_chute", AllBlocks.ENCASED_FAN.get(), TaskType.NORMAL).parent(chute)
				.addCriterion("0", AllTriggers.UPWARD_CHUTE.instance())
				.save(t, id + ":upward_chute");

		Advancement belt_funnel =
			advancement("belt_funnel", AllBlocks.ANDESITE_FUNNEL.get(), TaskType.NORMAL).parent(belt)
				.addCriterion("0", AllTriggers.BELT_FUNNEL.instance())
				.save(t, id + ":belt_funnel");

		Advancement belt_funnel_kiss =
			advancement("belt_funnel_kiss", AllBlocks.BRASS_FUNNEL.get(), TaskType.SECRET).parent(belt_funnel)
				.addCriterion("0", AllTriggers.BELT_FUNNEL_KISS.instance())
				.save(t, id + ":belt_funnel_kiss");

		Advancement wrench = itemAdvancement("wrench", AllItems.WRENCH, TaskType.NORMAL).parent(its_alive)
			.save(t, id + ":wrench");

		Advancement goggles = itemAdvancement("goggles", AllItems.GOGGLES, TaskType.NORMAL).parent(its_alive)
			.save(t, id + ":goggles");

		Advancement speed_gauge =
			kinecticAdvancement("speedometer", AllBlocks.SPEEDOMETER.get(), TaskType.NORMAL).parent(goggles)
				.save(t, id + ":speedometer");

		Advancement stress_gauge =
			kinecticAdvancement("stressometer", AllBlocks.STRESSOMETER.get(), TaskType.NORMAL).parent(goggles)
				.save(t, id + ":stressometer");

		Advancement shifting_gears =
			advancement("shifting_gears", AllBlocks.LARGE_COGWHEEL.get(), TaskType.NORMAL).parent(its_alive)
				.addCriterion("0", AllTriggers.SHIFTING_GEARS.instance())
				.save(t, id + ":shifting_gears");

		Advancement overstressed = advancement("overstressed", Items.BARRIER, TaskType.SECRET).parent(its_alive)
			.addCriterion("0", AllTriggers.OVERSTRESSED.instance())
			.save(t, id + ":overstressed");

	}

	void copperAge(Consumer<Advancement> t, Advancement root) {
		String id = Create.ID;

		Advancement copper_casing =
			advancement("copper_casing", AllBlocks.COPPER_CASING.get(), TaskType.GOAL).parent(root)
				.addCriterion("0", itemGathered(AllBlocks.COPPER_CASING.get()))
				.save(t, id + ":copper_casing");

		Advancement item_drain =
			advancement("item_drain", AllBlocks.ITEM_DRAIN.get(), TaskType.NORMAL).parent(copper_casing)
				.addCriterion("0", AllTriggers.ITEM_DRAIN.instance())
				.save(t, id + ":item_drain");

		Advancement chained_item_drain =
			advancement("chained_item_drain", AllBlocks.ITEM_DRAIN.get(), TaskType.SECRET).parent(item_drain)
				.addCriterion("0", AllTriggers.CHAINED_ITEM_DRAIN.instance())
				.save(t, id + ":chained_item_drain");

		Advancement spout = advancement("spout", AllBlocks.SPOUT.get(), TaskType.NORMAL).parent(copper_casing)
			.addCriterion("0", AllTriggers.SPOUT.instance())
			.save(t, id + ":spout");

		Advancement spout_potion = advancement("spout_potion", Items.POTION, TaskType.GOAL).parent(spout)
			.addCriterion("0", AllTriggers.SPOUT_POTION.instance())
			.save(t, id + ":spout_potion");

		Advancement chocolate = itemAdvancement("chocolate", () -> AllFluids.CHOCOLATE.get()
			.getBucket(), TaskType.GOAL).parent(spout)
				.save(t, id + ":chocolate");

		Advancement glass_pipe =
			advancement("glass_pipe", AllBlocks.FLUID_PIPE.get(), TaskType.NORMAL).parent(copper_casing)
				.addCriterion("0", AllTriggers.GLASS_PIPE.instance())
				.save(t, id + ":glass_pipe");

		Advancement pipe_collision =
			advancement("pipe_collision", AllBlocks.FLUID_VALVE.get(), TaskType.NORMAL).parent(glass_pipe)
				.addCriterion("0", AllTriggers.PIPE_COLLISION.instance())
				.save(t, id + ":pipe_collision");

		Advancement pipe_spill = advancement("pipe_spill", Items.BUCKET, TaskType.NORMAL).parent(glass_pipe)
			.addCriterion("0", AllTriggers.PIPE_SPILL.instance())
			.save(t, id + ":pipe_spill");

		Advancement hose_pulley =
			advancement("hose_pulley", AllBlocks.HOSE_PULLEY.get(), TaskType.NORMAL).parent(pipe_spill)
				.addCriterion("0", AllTriggers.HOSE_PULLEY.instance())
				.save(t, id + ":hose_pulley");

		Advancement infinite_water =
			advancement("infinite_water", Items.WATER_BUCKET, TaskType.NORMAL).parent(hose_pulley)
				.addCriterion("0", isInfinite(Fluids.WATER))
				.save(t, id + ":infinite_water");

		Advancement infinite_lava =
			advancement("infinite_lava", Items.LAVA_BUCKET, TaskType.GOAL).parent(hose_pulley)
				.addCriterion("0", isInfinite(Fluids.LAVA))
				.save(t, id + ":infinite_lava");

		Advancement infinite_chocolate = advancement("infinite_chocolate", AllFluids.CHOCOLATE.get()
			.getBucket(), TaskType.CHALLENGE).parent(hose_pulley)
				.addCriterion("0", isInfinite(AllFluids.CHOCOLATE.get()))
				.save(t, id + ":infinite_chocolate");
	}

	void brassAge(Consumer<Advancement> t, Advancement root) {
		String id = Create.ID;

		Advancement brass_casing =
			advancement("brass_casing", AllBlocks.BRASS_CASING.get(), TaskType.GOAL).parent(root)
				.addCriterion("0", itemGathered(AllBlocks.BRASS_CASING.get()))
				.save(t, id + ":brass_casing");

		Advancement nixie_tube =
			advancement("nixie_tube", AllBlocks.ORANGE_NIXIE_TUBE.get(), TaskType.NORMAL).parent(brass_casing)
				.addCriterion("0", placeBlock(AllBlocks.ORANGE_NIXIE_TUBE.get()))
				.save(t, id + ":nixie_tube");

		Advancement crafter = kinecticAdvancement("crafter", AllBlocks.MECHANICAL_CRAFTER.get(), TaskType.MILESTONE)
			.parent(brass_casing)
			.save(t, id + ":crafter");

		Advancement flywheel = advancement("flywheel", AllBlocks.FLYWHEEL.get(), TaskType.NORMAL).parent(crafter)
			.addCriterion("0", AllTriggers.FLYWHEEL.instance())
			.save(t, id + ":flywheel");

		Advancement overstress_flywheel =
			advancement("overstress_flywheel", AllBlocks.FURNACE_ENGINE.get(), TaskType.CHALLENGE).parent(flywheel)
				.addCriterion("0", AllTriggers.OVERSTRESS_FLYWHEEL.instance())
				.save(t, id + ":overstress_flywheel");

		Advancement clockwork_bearing =
			advancement("clockwork_bearing", AllBlocks.CLOCKWORK_BEARING.get(), TaskType.NORMAL)
				.parent(brass_casing)
				.addCriterion("0", AllTriggers.CLOCKWORK_BEARING.instance())
				.save(t, id + ":clockwork_bearing");

		Advancement mechanical_arm = advancement("mechanical_arm", AllBlocks.MECHANICAL_ARM.get(), TaskType.MILESTONE)
			.addCriterion("0", AllTriggers.MECHANICAL_ARM.instance())
			.parent(brass_casing)
			.save(t, id + ":mechanical_arm");

		Advancement musical_arm = advancement("musical_arm", Items.MUSIC_DISC_13, TaskType.MILESTONE)
			.addCriterion("0", AllTriggers.MUSICAL_ARM.instance())
			.parent(mechanical_arm)
			.save(t, id + ":musical_arm");

		Advancement arm_many_targets = advancement("arm_many_targets", AllBlocks.BRASS_FUNNEL.get(), TaskType.MILESTONE)
			.addCriterion("0", AllTriggers.ARM_MANY_TARGETS.instance())
			.parent(mechanical_arm)
			.save(t, id + ":arm_many_targets");

		Advancement arm_blaze_burner = advancement("arm_blaze_burner", AllBlocks.BLAZE_BURNER.get(), TaskType.NORMAL)
			.addCriterion("0", AllTriggers.ARM_BLAZE_BURNER.instance())
			.parent(mechanical_arm)
			.save(t, id + ":arm_blaze_burner");

		Advancement deployer =
			kinecticAdvancement("deployer", AllBlocks.DEPLOYER.get(), TaskType.MILESTONE).parent(brass_casing)
				.save(t, id + ":deployer");
		
		Advancement clockwork_component =
			itemAdvancement("precision_mechanism", AllItems.PRECISION_MECHANISM, TaskType.NORMAL).parent(deployer)
				.save(t, id + ":precision_mechanism");

		Advancement clockwork_component_eob = deadEnd().parent(clockwork_component)
			.addCriterion("0", itemGathered(AllItems.PRECISION_MECHANISM.get()))
			.save(t, id + ":clockwork_component_eob");
		
		Advancement extendo_grip =
			advancement("extendo_grip", AllItems.EXTENDO_GRIP.get(), TaskType.NORMAL).parent(clockwork_component)
				.addCriterion("0", AllTriggers.EXTENDO.instance())
				.save(t, id + ":extendo_grip");

		Advancement potato_cannon =
			advancement("potato_cannon", AllItems.POTATO_CANNON.get(), TaskType.GOAL).parent(clockwork_component)
				.addCriterion("0", AllTriggers.POTATO_KILL.instance())
				.save(t, id + ":potato_cannon");

		Advancement dual_extendo_grip =
			advancement("dual_extendo_grip", AllItems.EXTENDO_GRIP.get(), TaskType.SECRET).parent(extendo_grip)
				.addCriterion("0", AllTriggers.GIGA_EXTENDO.instance())
				.save(t, id + ":dual_extendo_grip");

		Advancement speed_controller =
			kinecticAdvancement("speed_controller", AllBlocks.ROTATION_SPEED_CONTROLLER.get(), TaskType.NORMAL)
				.parent(clockwork_component)
				.save(t, id + ":speed_controller");

		Advancement fist_bump = advancement("fist_bump", AllBlocks.DEPLOYER.get(), TaskType.SECRET).parent(deployer)
			.addCriterion("0", AllTriggers.DEPLOYER_BOOP.instance())
			.save(t, id + ":fist_bump");

		Advancement crushing_wheel =
			advancement("crushing_wheel", AllBlocks.CRUSHING_WHEEL.get(), TaskType.MILESTONE).parent(crafter)
				.addCriterion("0", itemGathered(AllBlocks.CRUSHING_WHEEL.get()))
				.save(t, id + ":crushing_wheel");

		Advancement blaze_cake =
			itemAdvancement("blaze_cake", AllItems.BLAZE_CAKE, TaskType.NORMAL).parent(crushing_wheel)
				.save(t, id + ":blaze_cake");

		Advancement chromatic_compound =
			itemAdvancement("chromatic_compound", AllItems.CHROMATIC_COMPOUND, TaskType.NORMAL).parent(blaze_cake)
				.save(t, id + ":chromatic_compound");

		Advancement shadow_steel =
			itemAdvancement("shadow_steel", AllItems.SHADOW_STEEL, TaskType.GOAL).parent(chromatic_compound)
				.save(t, id + ":shadow_steel");

		Advancement refined_radiance =
			itemAdvancement("refined_radiance", AllItems.REFINED_RADIANCE, TaskType.GOAL).parent(chromatic_compound)
				.save(t, id + ":refined_radiance");

		Advancement chromatic_age = advancement("chromatic_age", AllBlocks.REFINED_RADIANCE_CASING.get(), TaskType.GOAL)
			.parent(chromatic_compound)
			.addCriterion("0", itemGathered(AllBlocks.SHADOW_STEEL_CASING.get()))
			.addCriterion("1", itemGathered(AllBlocks.REFINED_RADIANCE_CASING.get()))
			.save(t, id + ":chromatic_age");

		Advancement chromatic_eob = deadEnd().parent(chromatic_age)
			.addCriterion("0", itemGathered(AllBlocks.SHADOW_STEEL_CASING.get()))
			.addCriterion("1", itemGathered(AllBlocks.REFINED_RADIANCE_CASING.get()))
			.save(t, id + ":chromatic_eob");

		Advancement symmetry_wand =
			itemAdvancement("wand_of_symmetry", AllItems.WAND_OF_SYMMETRY, TaskType.NORMAL).parent(refined_radiance)
				.save(t, id + ":wand_of_symmetry");

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
	public void run(HashCache cache) throws IOException {
		Path path = this.generator.getOutputFolder();
		Set<ResourceLocation> set = Sets.newHashSet();
		Consumer<Advancement> consumer = (p_204017_3_) -> {
			if (!set.add(p_204017_3_.getId()))
				throw new IllegalStateException("Duplicate advancement " + p_204017_3_.getId());

			Path path1 = getPath(path, p_204017_3_);

			try {
				DataProvider.save(GSON, cache, p_204017_3_.deconstruct()
					.serializeToJson(), path1);
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

	public PlacedBlockTrigger.TriggerInstance placeBlock(Block block) {
		return PlacedBlockTrigger.TriggerInstance.placedBlock(block);
	}

	public RegistryTrigger.Instance<Fluid> isInfinite(FlowingFluid fluid) {
		return AllTriggers.INFINITE_FLUID.forEntries(fluid.getSource());
	}

	public InventoryChangeTrigger.TriggerInstance itemGathered(ItemLike itemprovider) {
		return InventoryChangeTrigger.TriggerInstance.hasItems(itemprovider);
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
		return advancement(name, block, type).addCriterion("0", placeBlock(block));
//			.withCriterion("1", isPowered(block)); Duplicate toast
	}

	public Builder advancement(String name, ItemLike icon, TaskType type) {
		return advancement(name, new ItemStack(icon), type);
	}

	public Builder deadEnd() {
		return advancement("eob", Items.OAK_SAPLING, TaskType.SILENT_GATE);
	}

	public Builder advancement(String name, ItemStack icon, TaskType type) {
		return Advancement.Builder.advancement()
			.display(icon, new TranslatableComponent(LANG + name),
				new TranslatableComponent(LANG + name + ".desc"), null, type.frame, type.toast, type.announce,
				type.hide);
	}

	public Builder itemAdvancement(String name, Supplier<? extends ItemLike> item, TaskType type) {
		return advancement(name, item.get(), type).addCriterion("0", itemGathered(item.get()));
	}

}
