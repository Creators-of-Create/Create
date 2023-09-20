package com.simibubi.create.foundation.advancement;

import static com.simibubi.create.foundation.advancement.CreateAdvancement.TaskType.EXPERT;
import static com.simibubi.create.foundation.advancement.CreateAdvancement.TaskType.NOISY;
import static com.simibubi.create.foundation.advancement.CreateAdvancement.TaskType.SECRET;
import static com.simibubi.create.foundation.advancement.CreateAdvancement.TaskType.SILENT;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.advancement.CreateAdvancement.Builder;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class AllAdvancements implements DataProvider {

	public static final List<CreateAdvancement> ENTRIES = new ArrayList<>();
	public static final CreateAdvancement START = null,

		/*
		 * Some ids have trailing 0's to modify their vertical position on the tree
		 * (Advancement ordering seems to be deterministic but hash based)
		 */

		ROOT = create("root", b -> b.icon(AllItems.BRASS_HAND)
			.title("Welcome to Create")
			.description("Here Be Contraptions")
			.awardedForFree()
			.special(SILENT)),

		// Andesite - Central Branch

		ANDESITE = create("andesite_alloy", b -> b.icon(AllItems.ANDESITE_ALLOY)
			.title("Sturdier Rocks")
			.description("Obtain some Andesite Alloy, Create's most important resource")
			.after(ROOT)
			.whenIconCollected()),

		ANDESITE_CASING = create("andesite_casing", b -> b.icon(AllBlocks.ANDESITE_CASING)
			.title("The Andesite Age")
			.description("Apply Andesite Alloy to stripped wood, creating a basic casing for your machines")
			.after(ANDESITE)
			.special(NOISY)),

		PRESS = create("mechanical_press", b -> b.icon(AllBlocks.MECHANICAL_PRESS)
			.title("Bonk!")
			.description("Create some sheets in a Mechanical Press")
			.after(ANDESITE_CASING)
			.special(NOISY)),

		ENCASED_FAN = create("encased_fan", b -> b.icon(AllBlocks.ENCASED_FAN)
			.title("Wind Maker")
			.description("Place and power an Encased Fan")
			.after(PRESS)),

		FAN_PROCESSING = create("fan_processing", b -> b.icon(AllItems.PROPELLER)
			.title("Processing by Particle")
			.description("Use an Encased Fan to process materials")
			.after(ENCASED_FAN)),

		SAW_PROCESSING = create("saw_processing", b -> b.icon(AllBlocks.MECHANICAL_SAW)
			.title("Workshop's Most Feared")
			.description("Use an upright Mechanical Saw to process materials")
			.after(FAN_PROCESSING)),

		COMPACTING = create("compacting", b -> b.icon(Blocks.IRON_BLOCK)
			.title("Compactification")
			.description("Use a Mechanical Press and a Basin to create fewer items from more")
			.after(SAW_PROCESSING)),

		BELT = create("belt", b -> b.icon(AllItems.BELT_CONNECTOR)
			.title("Kelp Drive")
			.description("Connect two Shafts with a Mechanical Belt")
			.after(COMPACTING)),

		FUNNEL = create("funnel", b -> b.icon(AllBlocks.ANDESITE_FUNNEL)
			.title("Airport Aesthetic")
			.description("Extract or insert items into a container using a Funnel")
			.after(BELT)),

		CHUTE = create("chute", b -> b.icon(AllBlocks.CHUTE)
			.title("Vertical Logistics")
			.description("Transport some items by Chute")
			.after(FUNNEL)),

		MIXER = create("mechanical_mixer", b -> b.icon(AllBlocks.MECHANICAL_MIXER)
			.title("Mixing It Up")
			.description("Combine ingredients in a Mechanical Mixer")
			.after(CHUTE)),

		BLAZE_BURNER = create("burner", b -> b.icon(AllBlocks.BLAZE_BURNER)
			.title("Sentient Fireplace")
			.description("Obtain a Blaze Burner")
			.whenIconCollected()
			.after(MIXER)),

		// Andesite - Top Branch

		WATER_WHEEL = create("water_wheel", b -> b.icon(AllBlocks.WATER_WHEEL)
			.title("Harnessed Hydraulics")
			.description("Place a Water Wheel and use it to generate torque")
			.after(ANDESITE)),

		WINDMILL = create("windmill", b -> b.icon(AllBlocks.SAIL_FRAME)
			.title("A Mild Breeze")
			.description("Assemble a windmill and use it to generate torque")
			.after(WATER_WHEEL)),

		COGS = create("shifting_gears", b -> b.icon(AllBlocks.COGWHEEL)
			.title("Shifting Gears")
			.description(
				"Connect a Large Cogwheel to a Small Cogwheel, allowing you to change the speed of your Contraption")
			.after(WINDMILL)),

		MILLSTONE = create("millstone", b -> b.icon(AllBlocks.MILLSTONE)
			.title("Embrace the Grind")
			.description("Use a Millstone to pulverise materials")
			.after(COGS)),

		SUPER_GLUE = create("super_glue", b -> b.icon(AllItems.SUPER_GLUE)
			.title("Area of Connect")
			.description("Super Glue some blocks into a group")
			.after(MILLSTONE)),

		CONTRAPTION_ACTORS = create("contraption_actors", b -> b.icon(AllBlocks.MECHANICAL_HARVESTER)
			.title("Moving with Purpose")
			.description("Create a Contraption with drills, saws, or harvesters on board")
			.after(SUPER_GLUE)),

		PSI = create("portable_storage_interface", b -> b.icon(AllBlocks.PORTABLE_STORAGE_INTERFACE)
			.title("Drive-By Exchange")
			.description("Use a Portable Storage Interface to take or insert items into a Contraption")
			.after(CONTRAPTION_ACTORS)),

		WRENCH_GOGGLES = create("wrench_goggles", b -> b.icon(AllItems.WRENCH)
			.title("Kitted Out")
			.description("Equip Engineer's Goggles and a Wrench")
			.whenIconCollected()
			.whenItemCollected(AllItems.GOGGLES)
			.after(PSI)),

		STRESSOMETER = create("stressometer", b -> b.icon(AllBlocks.STRESSOMETER)
			.title("Stress for Nerds")
			.description("Get an exact readout with the help of Engineer's Goggles and a Stressometer")
			.after(WRENCH_GOGGLES)),

		CUCKOO_CLOCK = create("cuckoo_clock", b -> b.icon(AllBlocks.CUCKOO_CLOCK)
			.title("Is It Time?")
			.description("Witness your Cuckoo Clock announce bedtime")
			.after(STRESSOMETER)
			.special(NOISY)),

		// Andesite - Expert Branch

		WINDMILL_MAXED = create("windmill_maxed", b -> b.icon(AllBlocks.SAIL)
			.title("A Strong Breeze")
			.description("Assemble a windmill of maximum strength")
			.after(ANDESITE)
			.special(EXPERT)),

		EJECTOR_MAXED = create("ejector_maxed", b -> b.icon(AllBlocks.WEIGHTED_EJECTOR)
			.title("Springboard Champion")
			.description("Get launched more than 30 blocks by a Weighted Ejector")
			.after(WINDMILL_MAXED)
			.special(EXPERT)),

		PULLEY_MAXED = create("pulley_maxed", b -> b.icon(AllBlocks.ROPE_PULLEY)
			.title("Rope to Nowhere")
			.description("Extend a Rope Pulley over 200 blocks deep")
			.after(EJECTOR_MAXED)
			.special(EXPERT)),

		CART_PICKUP = create("cart_pickup", b -> b.icon(AllItems.CHEST_MINECART_CONTRAPTION)
			.title("Strong Arms")
			.description("Pick up a Minecart Contraption with at least 200 attached blocks")
			.after(PULLEY_MAXED)
			.special(EXPERT)),

		ANVIL_PLOUGH = create("anvil_plough", b -> b.icon(Blocks.CHIPPED_ANVIL)
			.title("Blacksmith Artillery")
			.description("Launch an Anvil with Mechanical Ploughs")
			.after(CART_PICKUP)
			.special(EXPERT)),

		// Andesite - Hidden

		LAVA_WHEEL = create("lava_wheel_00000", b -> b.icon(AllBlocks.WATER_WHEEL)
			.title("Magma Wheel")
			.description("This shouldn't have worked")
			.after(MIXER)
			.special(SECRET)),

		HAND_CRANK = create("hand_crank_000", b -> b.icon(AllBlocks.HAND_CRANK)
			.title("Workout Session")
			.description("Use a Hand Crank until fully exhausted")
			.after(MIXER)
			.special(SECRET)),

		FUNNEL_KISS = create("belt_funnel_kiss", b -> b.icon(AllBlocks.BRASS_FUNNEL)
			.title("The Parrots and the Flaps")
			.description("Make two Belt-mounted Funnels kiss")
			.after(MIXER)
			.special(SECRET)),

		STRESSOMETER_MAXED = create("stressometer_maxed", b -> b.icon(AllBlocks.STRESSOMETER)
			.title("Perfectly Stressed")
			.description("Get a 100% readout from a Stressometer")
			.after(MIXER)
			.special(SECRET)),

		// Copper - Central Branch

		COPPER = create("copper", b -> b.icon(Items.COPPER_INGOT)
			.title("Cuprum Bokum")
			.description("Amass some Copper Ingots for your exploits in fluid manipulation")
			.whenIconCollected()
			.after(BLAZE_BURNER)
			.special(SILENT)),

		COPPER_CASING = create("copper_casing", b -> b.icon(AllBlocks.COPPER_CASING)
			.title("The Copper Age")
			.description("Apply Copper Ingots to stripped wood, creating a waterproof casing for your machines")
			.after(COPPER)
			.special(NOISY)),

		SPOUT = create("spout", b -> b.icon(AllBlocks.SPOUT)
			.title("Sploosh")
			.description("Watch a fluid-containing item be filled by a Spout")
			.after(COPPER_CASING)),

		DRAIN = create("drain", b -> b.icon(AllBlocks.ITEM_DRAIN)
			.title("Tumble Draining")
			.description("Watch a fluid-containing item be emptied by an Item Drain")
			.after(SPOUT)),

		STEAM_ENGINE = create("steam_engine", b -> b.icon(AllBlocks.STEAM_ENGINE)
			.title("The Powerhouse")
			.description("Use a Steam Engine to generate torque")
			.after(DRAIN)),

		STEAM_WHISTLE = create("steam_whistle", b -> b.icon(AllBlocks.STEAM_WHISTLE)
			.title("Voice of an Angel")
			.description("Activate a Steam Whistle")
			.after(STEAM_ENGINE)),

		BACKTANK = create("backtank", b -> b.icon(AllItems.COPPER_BACKTANK)
			.title("Pressure to Go")
			.description("Create a Copper Backtank and make it accumulate air pressure")
			.after(STEAM_WHISTLE)),

		DIVING_SUIT = create("diving_suit", b -> b.icon(AllItems.COPPER_DIVING_HELMET)
			.title("Ready for the Depths")
			.description("Equip a Diving Helmet and a Copper Backtank, then jump into water")
			.after(BACKTANK)),

		// Copper - Top Branch

		PUMP = create("mechanical_pump_0", b -> b.icon(AllBlocks.MECHANICAL_PUMP)
			.title("Under Pressure")
			.description("Place and power a Mechanical Pump")
			.after(COPPER)),

		GLASS_PIPE = create("glass_pipe", b -> b.icon(AllBlocks.FLUID_PIPE)
			.title("Flow Discovery")
			.description("Use your Wrench on a pipe that contains a fluid")
			.after(PUMP)),

		WATER_SUPPLY = create("water_supply", b -> b.icon(Items.WATER_BUCKET)
			.title("Puddle Collector")
			.description("Use the pulling end of a Fluid Pipe or Mechanical Pump to collect water")
			.after(GLASS_PIPE)),

		HOSE_PULLEY = create("hose_pulley", b -> b.icon(AllBlocks.HOSE_PULLEY)
			.title("Industrial Spillage")
			.description("Lower a Hose Pulley and watch it drain or fill a body of fluid")
			.after(WATER_SUPPLY)),

		CHOCOLATE_BUCKET = create("chocolate_bucket", b -> b.icon(AllFluids.CHOCOLATE.get()
			.getBucket())
			.title("A World of Imagination")
			.description("Obtain a bucket of molten chocolate")
			.whenIconCollected()
			.after(HOSE_PULLEY)),

		HONEY_DRAIN = create("honey_drain", b -> b.icon(Items.BEEHIVE)
			.title("Autonomous Bee-Keeping")
			.description("Use pipes to pull honey from a Bee Nest or Beehive")
			.after(CHOCOLATE_BUCKET)),

		// Copper - Expert Branch

		HOSE_PULLEY_LAVA = create("hose_pulley_lava", b -> b.icon(AllBlocks.HOSE_PULLEY)
			.title("Tapping the Mantle")
			.description("Pump from a body of lava large enough to be considered infinite")
			.after(COPPER)
			.special(EXPERT)),

		STEAM_ENGINE_MAXED = create("steam_engine_maxed", b -> b.icon(AllBlocks.STEAM_ENGINE)
			.title("Full Steam")
			.description("Run a boiler at the maximum level of power")
			.after(HOSE_PULLEY_LAVA)
			.special(EXPERT)),

		FOODS = create("foods", b -> b.icon(AllItems.CHOCOLATE_BERRIES)
			.title("Balanced Diet")
			.description("Create Chocolate Glazed Berries, a Honeyed Apple, and a Sweet Roll all from the same Spout")
			.after(STEAM_ENGINE_MAXED)
			.special(EXPERT)),
		
		DIVING_SUIT_LAVA = create("diving_suit_lava", b -> b.icon(AllItems.NETHERITE_DIVING_HELMET)
			.title("Swimming with the Striders")
			.description("Attempt to take a dive in lava with your netherite diving gear")
			.after(FOODS)
			.special(EXPERT)),

		// Copper - Hidden

		CHAINED_DRAIN = create("chained_drain", b -> b.icon(AllBlocks.ITEM_DRAIN)
			.title("On a Roll")
			.description("Watch an item move across a row of Item Drains")
			.after(BACKTANK)
			.special(SECRET)),

		CROSS_STREAMS = create("cross_streams", b -> b.icon(Blocks.COBBLESTONE)
			.title("Don't Cross the Streams!")
			.description("Watch two fluids meet in your pipe network")
			.after(BACKTANK)
			.special(SECRET)),

		PIPE_ORGAN = create("pipe_organ", b -> b.icon(AllBlocks.STEAM_WHISTLE)
			.title("The Pipe Organ")
			.description("Attach 12 uniquely pitched Steam Whistles to a single Fluid Tank")
			.after(BACKTANK)
			.special(SECRET)),

		// Brass - Central Branch

		BRASS = create("brass", b -> b.icon(AllItems.BRASS_INGOT)
			.title("Real Alloys")
			.description("Create Brass Ingots by alloying Copper and Zinc Ingots in your Blaze-heated Mechanical Mixer")
			.whenIconCollected()
			.after(DIVING_SUIT)),

		BRASS_CASING = create("brass_casing", b -> b.icon(AllBlocks.BRASS_CASING)
			.title("The Brass Age")
			.description("Apply Brass Ingots to stripped wood, creating a casing for more sophisticated machines")
			.after(BRASS)
			.special(NOISY)),

		ROSE_QUARTZ = create("rose_quartz", b -> b.icon(AllItems.POLISHED_ROSE_QUARTZ)
			.title("Pink Diamonds")
			.description("Polish some Rose Quartz")
			.whenIconCollected()
			.after(BRASS_CASING)),

		DEPLOYER = create("deployer", b -> b.icon(AllBlocks.DEPLOYER)
			.title("Artificial Intelligence")
			.description("Place and power a Deployer, the perfect reflection of yourself")
			.after(ROSE_QUARTZ)),

		MECHANISM = create("precision_mechanism", b -> b.icon(AllItems.PRECISION_MECHANISM)
			.title("Complex Curiosities")
			.description("Assemble a Precision Mechanism")
			.whenIconCollected()
			.after(DEPLOYER)
			.special(NOISY)),

		SPEED_CONTROLLER = create("speed_controller", b -> b.icon(AllBlocks.ROTATION_SPEED_CONTROLLER)
			.title("Engineers hate this simple trick!")
			.description("Fine-tune your Contraption with a Rotation Speed Controller")
			.after(MECHANISM)),

		MECHANICAL_ARM = create("mechanical_arm", b -> b.icon(AllBlocks.MECHANICAL_ARM)
			.title("Busy Hands")
			.description("Watch your Mechanical Arm transport its first item")
			.after(SPEED_CONTROLLER)
			.special(NOISY)),

		CRAFTER = create("mechanical_crafter", b -> b.icon(AllBlocks.MECHANICAL_CRAFTER)
			.title("Automated Assembly")
			.description("Place and power some Mechanical Crafters")
			.after(MECHANICAL_ARM)),

		CRUSHING_WHEEL = create("crushing_wheel", b -> b.icon(AllBlocks.CRUSHING_WHEEL)
			.title("A Pair of Giants")
			.description("Place and power a set of Crushing Wheels")
			.after(CRAFTER)
			.special(NOISY)),

		// Brass - Top Branch

		HAUNTED_BELL = create("haunted_bell", b -> b.icon(AllBlocks.HAUNTED_BELL)
			.title("Shadow Sense")
			.description("Toll a Haunted Bell")
			.after(BRASS)
			.special(NOISY)),

		CLOCKWORK_BEARING = create("clockwork_bearing", b -> b.icon(AllBlocks.CLOCKWORK_BEARING)
			.title("Contraption O'Clock")
			.description("Assemble a structure mounted on a Clockwork Bearing")
			.after(HAUNTED_BELL)
			.special(NOISY)),

		DISPLAY_LINK = create("display_link", b -> b.icon(AllBlocks.DISPLAY_LINK)
			.title("Big Data")
			.description("Use a Display Link to visualise information")
			.after(CLOCKWORK_BEARING)
			.special(NOISY)),

		POTATO_CANNON = create("potato_cannon", b -> b.icon(AllItems.POTATO_CANNON)
			.title("Fwoomp!")
			.description("Defeat an enemy with your Potato Cannon")
			.after(DISPLAY_LINK)
			.special(NOISY)),

		EXTENDO_GRIP = create("extendo_grip", b -> b.icon(AllItems.EXTENDO_GRIP)
			.title("Boioioing!")
			.description("Get hold of an Extendo Grip")
			.after(POTATO_CANNON)),

		LINKED_CONTROLLER = create("linked_controller", b -> b.icon(AllItems.LINKED_CONTROLLER)
			.title("Remote Activation")
			.description("Activate a Redstone Link using a Linked Controller")
			.after(EXTENDO_GRIP)),

		ARM_BLAZE_BURNER = create("arm_blaze_burner", b -> b.icon(AllBlocks.BLAZE_BURNER)
			.title("Combust-o-Tron")
			.description("Instruct a Mechanical Arm to feed your Blaze Burner")
			.after(LINKED_CONTROLLER)),

		// Brass - Expert Branch

		CRUSHER_MAXED = create("crusher_maxed_0000", b -> b.icon(AllBlocks.CRUSHING_WHEEL)
			.title("Crushing It")
			.description("Operate a pair of Crushing Wheels at maximum speed")
			.after(BRASS)
			.special(EXPERT)),

		ARM_MANY_TARGETS = create("arm_many_targets", b -> b.icon(AllBlocks.MECHANICAL_ARM)
			.title("Organize-o-Tron")
			.description("Program a Mechanical Arm with 10 or more output locations")
			.after(CRUSHER_MAXED)
			.special(EXPERT)),

		POTATO_CANNON_COLLIDE = create("potato_cannon_collide", b -> b.icon(Items.CARROT)
			.title("Veggie Fireworks")
			.description("Cause Potato Cannon projectiles of different types to collide with each other")
			.after(ARM_MANY_TARGETS)
			.special(EXPERT)),

		SELF_DEPLOYING = create("self_deploying", b -> b.icon(Items.RAIL)
			.title("Self-Driving Cart")
			.description("Create a Minecart Contraption that places tracks in front of itself")
			.after(POTATO_CANNON_COLLIDE)
			.special(EXPERT)),

		// Brass - Hidden

		FIST_BUMP = create("fist_bump", b -> b.icon(AllBlocks.DEPLOYER)
			.title("Pound It, Bro!")
			.description("Make two Deployers fist-bump")
			.after(CRAFTER)
			.special(SECRET)),

		CRAFTER_LAZY = create("crafter_lazy_000", b -> b.icon(AllBlocks.MECHANICAL_CRAFTER)
			.title("Desperate Measures")
			.description("Drastically slow down a Mechanical Crafter to procrastinate on proper infrastructure")
			.after(CRAFTER)
			.special(SECRET)),

		EXTENDO_GRIP_DUAL = create("extendo_grip_dual", b -> b.icon(AllItems.EXTENDO_GRIP)
			.title("To Full Extent")
			.description("Dual-wield Extendo Grips for superhuman reach")
			.after(CRAFTER)
			.special(SECRET)),

		MUSICAL_ARM = create("musical_arm", b -> b.icon(Blocks.JUKEBOX)
			.title("DJ Mechanico")
			.description("Watch a Mechanical Arm operate your Jukebox")
			.after(CRAFTER)
			.special(SECRET)),

		// Trains - Central Branch

		STURDY_SHEET = create("sturdy_sheet", b -> b.icon(AllItems.STURDY_SHEET)
			.title("The Sturdiest Rocks")
			.description("Assemble a Sturdy Sheet by refining Powdered Obsidian")
			.whenIconCollected()
			.after(CRUSHING_WHEEL)),

		TRAIN_CASING = create("train_casing_00", b -> b.icon(AllBlocks.RAILWAY_CASING)
			.title("The Logistical Age")
			.description("Use Sturdy Sheets to create a casing for railway components")
			.after(STURDY_SHEET)
			.special(NOISY)),

		TRAIN = create("train", b -> b.icon(AllBlocks.TRACK_STATION)
			.title("All Aboard!")
			.description("Assemble your first Train")
			.after(TRAIN_CASING)
			.special(NOISY)),

		CONDUCTOR = create("conductor", b -> b.icon(AllItems.SCHEDULE)
			.title("Conductor Instructor")
			.description("Instruct a Train driver with a Train Schedule")
			.after(TRAIN)),

		SIGNAL = create("track_signal", b -> b.icon(AllBlocks.TRACK_SIGNAL)
			.title("Traffic Control")
			.description("Place a Train Signal")
			.after(CONDUCTOR)),

		DISPLAY_BOARD = create("display_board_0", b -> b.icon(AllBlocks.DISPLAY_BOARD)
			.title("Dynamic Timetables")
			.description("Forecast a Train's arrival on your Display Board with the help of Display Links")
			.after(SIGNAL)
			.special(NOISY)),

		// Trains - Top Branch

		TRAIN_TRACK = create("track_0", b -> b.icon(AllBlocks.TRACK)
			.title("A New Gauge")
			.description("Obtain some Train Tracks")
			.whenIconCollected()
			.after(STURDY_SHEET)),

		TRAIN_WHISTLE = create("train_whistle", b -> b.icon(AllBlocks.STEAM_WHISTLE)
			.title("Choo Choo!")
			.description("Assemble a Steam Whistle to your Train and activate it while driving")
			.after(TRAIN_TRACK)),

		TRAIN_PORTAL = create("train_portal", b -> b.icon(Blocks.AMETHYST_BLOCK)
			.title("Dimensional Commuter")
			.description("Ride a Train through a portal")
			.after(TRAIN_WHISTLE)
			.special(NOISY)),

		// Trains - Expert Branch

		TRACK_CRAFTING = create("track_crafting_factory", b -> b.icon(AllBlocks.MECHANICAL_PRESS)
			.title("Track Factory")
			.description("Produce more than 1000 Train Tracks with the same Mechanical Press")
			.after(STURDY_SHEET)
			.special(EXPERT)),

		LONG_TRAIN = create("long_train", b -> b.icon(Items.MINECART)
			.title("Ambitious Endeavours")
			.description("Create a Train with at least 6 carriages")
			.after(TRACK_CRAFTING)
			.special(EXPERT)),

		LONG_TRAVEL = create("long_travel", b -> b.icon(AllBlocks.SEATS.get(DyeColor.GREEN))
			.title("Field Trip")
			.description("Leave a Train Seat over 5000 blocks away from where you started travelling")
			.after(LONG_TRAIN)
			.special(EXPERT)),

		// Trains - Hidden

		TRAIN_ROADKILL = create("train_roadkill", b -> b.icon(Items.DIAMOND_SWORD)
			.title("Road Kill")
			.description("Run over an enemy with your Train")
			.after(SIGNAL)
			.special(SECRET)),

		RED_SIGNAL = create("red_signal", b -> b.icon(AllBlocks.TRACK_SIGNAL)
			.title("Expert Driver")
			.description("Run a red Train Signal")
			.after(SIGNAL)
			.special(SECRET)),

		TRAIN_CRASH = create("train_crash", b -> b.icon(AllItems.INCOMPLETE_TRACK)
			.title("Terrible Service")
			.description("Witness a Train crash as a passenger")
			.after(SIGNAL)
			.special(SECRET)),

		TRAIN_CRASH_BACKWARDS = create("train_crash_backwards", b -> b.icon(AllItems.INCOMPLETE_TRACK)
			.title("Blind Spot")
			.description("Crash into another Train while driving backwards")
			.after(SIGNAL)
			.special(SECRET)),

		//
		END = null;

	private static CreateAdvancement create(String id, UnaryOperator<Builder> b) {
		return new CreateAdvancement(id, b);
	}

	// Datagen

	private static final Logger LOGGER = LogUtils.getLogger();
	private final DataGenerator generator;

	public AllAdvancements(DataGenerator generatorIn) {
		this.generator = generatorIn;
	}

	@Override
	public void run(CachedOutput cache) throws IOException {
		Path path = this.generator.getOutputFolder();
		Set<ResourceLocation> set = Sets.newHashSet();
		Consumer<Advancement> consumer = (p_204017_3_) -> {
			if (!set.add(p_204017_3_.getId()))
				throw new IllegalStateException("Duplicate advancement " + p_204017_3_.getId());

			Path path1 = getPath(path, p_204017_3_);

			try {
				DataProvider.saveStable(cache, p_204017_3_.deconstruct()
					.serializeToJson(), path1);
			} catch (IOException ioexception) {
				LOGGER.error("Couldn't save advancement {}", path1, ioexception);
			}
		};

		for (CreateAdvancement advancement : ENTRIES)
			advancement.save(consumer);
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

	public static void provideLang(BiConsumer<String, String> consumer) {
		for (CreateAdvancement advancement : ENTRIES)
			advancement.provideLang(consumer);
	}

	public static void register() {}

}
