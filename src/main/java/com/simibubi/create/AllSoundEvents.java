package com.simibubi.create;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.mojang.datafixers.util.Either;
import com.simibubi.create.foundation.sound.Sfx;

import net.minecraft.data.DataGenerator;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

public class AllSoundEvents extends SoundDefinitionsProvider {
	private static final HashMap<SoundEvent, SoundDefinition> deferredRegister = new LinkedHashMap<>();

	// CONSTANTS ==========================================

	public static final Sfx SCHEMATICANNON_LAUNCH_BLOCK = new Sfx.Mixer(
			"block.schematicannon.launch", SoundSource.BLOCKS)
			.track(sound(SoundEvents.GENERIC_EXPLODE).volume(.1f).pitch(1.1f))
			.mix(AllSoundEvents::register);
	public static final Sfx SCHEMATICANNON_FINISH = new Sfx.Mixer(
			"block.schematicannon.finish", SoundSource.BLOCKS)
			.track(sound(SoundEvents.NOTE_BLOCK_BELL).volume(1).pitch(.7f))
			.mix(AllSoundEvents::register);
	public static final Sfx DEPOT_SLIDE = new Sfx.Mixer(
			"block.depot.slide", SoundSource.BLOCKS)
			.track(sound(SoundEvents.SAND_BREAK).volume(.125f).pitch(1.5f))
			.mix(AllSoundEvents::register);
	public static final Sfx DEPOT_PLOP = new Sfx.Mixer(
			"block.depot.plop", SoundSource.BLOCKS)
			.track(sound(SoundEvents.ITEM_FRAME_ADD_ITEM).volume(.25f).pitch(1.25f))
			.mix(AllSoundEvents::register);
	public static final Sfx FUNNEL_FLAP = new Sfx.Mixer(
			"block.funnel.flap", SoundSource.BLOCKS)
			.track(sound(SoundEvents.ITEM_FRAME_ROTATE_ITEM).volume(.125f).pitch(1.5f))
			.track(sound(SoundEvents.WOOL_BREAK).volume(.0425f).pitch(.75f))
			.mix(AllSoundEvents::register);
	public static final Sfx SLIME_ADDED = new Sfx.Mixer(
			"event.slime", SoundSource.BLOCKS)
			.track(sound(SoundEvents.SLIME_BLOCK_PLACE))
			.mix(AllSoundEvents::register);
	public static final Sfx MECHANICAL_PRESS_ACTIVATE = new Sfx.Mixer(
			"block.mechanical_press.activate", SoundSource.BLOCKS)
			.track(sound(SoundEvents.ANVIL_LAND).volume(.125f).pitch(1f))
			.track(sound(SoundEvents.ITEM_BREAK).volume(.5f).pitch(1f))
			.mix(AllSoundEvents::register);
	public static final Sfx MECHANICAL_PRESS_ACTIVATE_ON_BELT = new Sfx.Mixer(
			"block.mechanical_press.activate_belt", SoundSource.BLOCKS)
			.track(sound(SoundEvents.WOOL_HIT).volume(.75f).pitch(1f))
			.track(sound(SoundEvents.ITEM_BREAK).volume(.15f).pitch(.75f))
			.mix(AllSoundEvents::register);
	public static final Sfx MIXER_MIXING = new Sfx.Mixer(
			"block.mixer.mixing", SoundSource.BLOCKS)
			.track(sound(SoundEvents.GILDED_BLACKSTONE_BREAK).volume(.125f).pitch(.5f))
			.track(sound(SoundEvents.NETHERRACK_BREAK).volume(.125f).pitch(.5f))
			.mix(AllSoundEvents::register);
	public static final Sfx CRANK_CRANKING = new Sfx.Mixer(
			"block.crank.cranking", SoundSource.BLOCKS)
			.track(sound(SoundEvents.WOOD_PLACE).volume(.075f).pitch(.5f))
			.track(sound(SoundEvents.WOODEN_BUTTON_CLICK_OFF).volume(.025f).pitch(.5f))
			.mix(AllSoundEvents::register);
	public static final Sfx WORLDSHAPER_PLACE = new Sfx.Mixer(
			"item.worldshaper.place", SoundSource.PLAYERS)
			.track(sound(SoundEvents.NOTE_BLOCK_BASEDRUM))
			.mix(AllSoundEvents::register);
	public static final Sfx SCROLL_VALUE = new Sfx.Mixer(
			"event.scroll", SoundSource.PLAYERS)
			.track(sound(SoundEvents.NOTE_BLOCK_HAT).volume(.124f).pitch(1f))
			.mix(AllSoundEvents::register);
	public static final Sfx CONFIRM = new Sfx.Mixer(
			"event.confirm", SoundSource.PLAYERS)
			.track(sound(SoundEvents.NOTE_BLOCK_BELL).volume(0.5f).pitch(0.8f))
			.mix(AllSoundEvents::register);
	public static final Sfx DENY = new Sfx.Mixer(
			"event.deny", SoundSource.PLAYERS)
			.track(sound(SoundEvents.NOTE_BLOCK_BASS).volume(1f).pitch(0.5f))
			.mix(AllSoundEvents::register);
	public static final Sfx COGS = new Sfx.Mixer(
			"soundscapes.cogs", SoundSource.BLOCKS)
			.track(sound("soundscapes/cogs"))
			.mix(AllSoundEvents::register);
	public static final Sfx FWOOMP = new Sfx.Mixer(
			"item.potato_cannon.fwoomp", SoundSource.PLAYERS)
			.track(sound("item/potato_cannon/fwoomp"))
			.mix(AllSoundEvents::register);
	public static final Sfx POTATO_HIT = new Sfx.Mixer(
			"entity.potato.hit", SoundSource.PLAYERS)
			.track(sound(SoundEvents.ITEM_FRAME_BREAK).volume(.75f).pitch(.75f))
			.track(sound(SoundEvents.WEEPING_VINES_BREAK).volume(.75f).pitch(1.25f))
			.mix(AllSoundEvents::register);
	public static final Sfx CONTRAPTION_ASSEMBLE = new Sfx.Mixer(
			"event.contraption.assemble", SoundSource.BLOCKS)
			.track(sound(SoundEvents.WOODEN_TRAPDOOR_OPEN).volume(.5f).pitch(.5f))
			.track(sound(SoundEvents.IRON_TRAPDOOR_OPEN).volume(.5f).pitch(.5f))
			.mix(AllSoundEvents::register);
	public static final Sfx CONTRAPTION_DISASSEMBLE = new Sfx.Mixer(
			"event.contraption.disassemble", SoundSource.BLOCKS)
			.track(sound(SoundEvents.IRON_TRAPDOOR_CLOSE).volume(.35f).pitch(.75f))
			.track(sound(SoundEvents.WOODEN_TRAPDOOR_CLOSE).volume(.35f).pitch(.75f))
			.mix(AllSoundEvents::register);
	public static final Sfx WRENCH_ROTATE = new Sfx.Mixer(
			"item.wrench.rotate", SoundSource.BLOCKS)
			.track(sound(SoundEvents.WOODEN_TRAPDOOR_CLOSE).volume(.25f).pitch(1.25f))
			.mix(AllSoundEvents::register);
	public static final Sfx WRENCH_REMOVE = new Sfx.Mixer(
			"item.wrench.remove", SoundSource.BLOCKS)
			.track(sound(SoundEvents.ITEM_PICKUP).volume(.25f).pitch(.75f))
			.track(sound(SoundEvents.NETHERITE_BLOCK_HIT).volume(.25f).pitch(.75f))
			.mix(AllSoundEvents::register);
	public static final Sfx CRAFTER_CLICK = new Sfx.Mixer(
			"block.crafter.click", SoundSource.BLOCKS)
			.track(sound(SoundEvents.NETHERITE_BLOCK_HIT).volume(.25f).pitch(1))
			.track(sound(SoundEvents.WOODEN_TRAPDOOR_OPEN).volume(.125f).pitch(1))
			.mix(AllSoundEvents::register);
	public static final Sfx CRAFTER_CRAFT = new Sfx.Mixer(
			"block.crafter.craft", SoundSource.BLOCKS)
			.track(sound(SoundEvents.ITEM_BREAK).volume(.125f).pitch(.75f))
			.mix(AllSoundEvents::register);
	public static final Sfx COPPER_ARMOR_EQUIP = new Sfx.Mixer(
			"item.copper_armor.equip", SoundSource.PLAYERS)
			.track(sound(SoundEvents.ARMOR_EQUIP_GOLD).volume(1f).pitch(1f))
			.mix(AllSoundEvents::register);
	public static final Sfx SANDING_SHORT = new Sfx.Mixer(
			"item.sanding.short", SoundSource.PLAYERS)
			.track(
					sound("item/sandpaper/sanding_short"),
					sound("item/sandpaper/sanding_short_1")
			)
			.mix(AllSoundEvents::register);
	public static final Sfx SANDING_LONG = new Sfx.Mixer(
			"item.sanding.long", SoundSource.PLAYERS)
			.track(sound("item/sandpaper/sanding_long"))
			.mix(AllSoundEvents::register);
	public static final Sfx CONTROLLER_CLICK = new Sfx.Mixer(
			"block.controller.click", SoundSource.BLOCKS)
			.track(sound(SoundEvents.ITEM_FRAME_ADD_ITEM).volume(.35f).pitch(1f))
			.mix(AllSoundEvents::register);
	public static final Sfx CONTROLLER_PUT = new Sfx.Mixer(
			"block.controller.put", SoundSource.BLOCKS)
			.track(sound(SoundEvents.BOOK_PUT).volume(1f).pitch(1f))
			.mix(AllSoundEvents::register);
	public static final Sfx CONTROLLER_TAKE = new Sfx.Mixer(
			"block.controller.take", SoundSource.BLOCKS)
			.track(sound(SoundEvents.ITEM_FRAME_REMOVE_ITEM).volume(1f).pitch(1f))
			.mix(AllSoundEvents::register);
	public static final Sfx SAW_ACTIVATE_WOOD = new Sfx.Mixer(
			"block.saw.activate.wood", SoundSource.BLOCKS)
			.track(sound(SoundEvents.BOAT_PADDLE_LAND).volume(.75f).pitch(1.5f))
			.mix(AllSoundEvents::register);
	public static final Sfx SAW_ACTIVATE_STONE = new Sfx.Mixer(
			"block.saw.activate.stone", SoundSource.BLOCKS)
			.track(sound(SoundEvents.UI_STONECUTTER_TAKE_RESULT).volume(.125f).pitch(1.25f))
			.mix(AllSoundEvents::register);
	public static final Sfx BLAZE_MUNCH = new Sfx.Mixer(
			"block.blaze.munch", SoundSource.BLOCKS)
			.track(sound(SoundEvents.GENERIC_EAT).volume(.5f).pitch(1f))
			.mix(AllSoundEvents::register);
	public static final Sfx CRUSHING_1 = new Sfx.Mixer(
			"block.crusher.crushing_1", SoundSource.BLOCKS)
			.track(sound(SoundEvents.NETHERRACK_HIT))
			.mix(AllSoundEvents::register);
	public static final Sfx CRUSHING_2 = new Sfx.Mixer(
			"block.crusher.crushing_2", Either.right(false), SoundSource.BLOCKS)
			.track(sound(SoundEvents.GRAVEL_PLACE))
			.mix(AllSoundEvents::register);
	public static final Sfx CRUSHING_3 = new Sfx.Mixer(
			"block.crusher.crushing_3", Either.right(false), SoundSource.BLOCKS)
			.track(sound(SoundEvents.NETHERITE_BLOCK_BREAK))
			.mix(AllSoundEvents::register);
	public static final Sfx PECULIAR_BELL_USE = new Sfx.Mixer(
			"block.peculiar_bell.use", SoundSource.BLOCKS)
			.track(sound(SoundEvents.BELL_BLOCK))
			.mix(AllSoundEvents::register);
	public static final Sfx HAUNTED_BELL_CONVERT = new Sfx.Mixer(
			"block.haunted_bell.convert", SoundSource.BLOCKS)
			.track(sound("block/haunted_bell/convert"))
			.mix(AllSoundEvents::register);
	public static final Sfx HAUNTED_BELL_USE = new Sfx.Mixer(
			"block.haunted_bell.use", SoundSource.BLOCKS)
			.track(sound("block/haunted_bell/use"))
			.mix(AllSoundEvents::register);
	public static final Sfx CONTACT_ACTIVATE = new Sfx.Mixer(
			"block.contact.activate", SoundSource.BLOCKS)
			.track(sound("block/contact/click").volume(.1f).pitch(1f))
			.mix(AllSoundEvents::register);
	public static final Sfx CONTACT_DEACTIVATE = new Sfx.Mixer(
			"block.contact.deactivate", SoundSource.BLOCKS)
			.track(sound("block/contact/click").volume(.1f).pitch(.75f))
			.mix(AllSoundEvents::register);
	public static final Sfx LATCH_ACTIVATE = new Sfx.Mixer(
			"block.latch.activate", SoundSource.BLOCKS)
			.track(sound(SoundEvents.LEVER_CLICK).volume(.15f).pitch(.9f))
			.mix(AllSoundEvents::register);
	public static final Sfx LATCH_DEACTIVATE = new Sfx.Mixer(
			"block.latch.deactivate", SoundSource.BLOCKS)
			.track(sound(SoundEvents.LEVER_CLICK).volume(.15f).pitch(.8f))
			.mix(AllSoundEvents::register);

	// END CONSTANTS ======================================

	protected AllSoundEvents(DataGenerator generator, ExistingFileHelper helper) {
		super(generator, Create.ID, helper);
	}

	private static final void register(SoundEvent event, SoundDefinition definition) {
		deferredRegister.put(event, definition);
	}

	protected static SoundDefinition.Sound sound(SoundEvent existingEvent) {
		return SoundDefinition.Sound.sound(existingEvent.getLocation(), SoundDefinition.SoundType.EVENT);
	}

	protected static SoundDefinition.Sound sound(String assetPath) {
		return SoundDefinition.Sound.sound(Create.asResource(assetPath), SoundDefinition.SoundType.SOUND);
	}

	@Override
	public void registerSounds() {
		deferredRegister.forEach(this::add);
		deferredRegister.clear(); // definitions no longer needed after registration
	}
}
