package com.simibubi.create.infrastructure.gametest.tests;

import java.util.List;

import static com.simibubi.create.infrastructure.gametest.CreateGameTestHelper.TICKS_PER_SECOND;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.redstone.diodes.PulseTimerBlockEntity;
import com.simibubi.create.content.redstone.diodes.PulseRepeaterBlockEntity;
import com.simibubi.create.content.redstone.diodes.PulseExtenderBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;

@GameTestGroup(path = "goggles_tooltip")
public class TestGogglesTooltip {
    // ===== Test Variables =====

    private static final int PROGRESSION_CHECK_SECONDS = 3;
    private static final BlockPos BLOCK_POS = new BlockPos(1, 2, 1);

    // ===== Test Methods =====

    // Blaze Burner Tooltip Tests
    @GameTest(template = "blaze_burner_empty")
    public static void noFuelTooltip(CreateGameTestHelper helper) {
        BlazeBurnerBlockEntity burner = getBurner(helper);
        String testType = "Blaze Burner Empty";

        // Check if the blaze burner is in creative mode (it should not be)
        if (burner.isCreative())
            helper.fail(testType + ": Should not be in creative mode for this test");
        // Check if the blaze burner has no fuel
        if (burner.getHeatLevelFromBlock() != HeatLevel.SMOULDERING)
            helper.fail(testType + ": Should not have fuel for this test. Expected heat level: SMOULDERING, got: " + burner.getHeatLevelFromBlock());
        // Check if active fuel is NONE
        if (burner.getActiveFuel() != FuelType.NONE)
            helper.fail(testType + ": Should not have fuel for this test. Expected fuel type: NONE, got: " + burner.getActiveFuel());

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = burner.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for an empty blaze burner
        assertTooltipContains(helper, tooltip, result, testType,
        "create.tooltip.blaze_burner.header",
                    "create.tooltip.blaze_burner.fuel_capacity",
                    "create.tooltip.blaze_burner.empty");

        // Assert that the tooltip does not contain the "Remaining Burn Time" text
        String fullText = collectTooltipText(tooltip);
        String remaining = Component.translatable("create.tooltip.blaze_burner.remaining").getString();
        if (fullText.contains(remaining))
            helper.fail(testType + ": Tooltip should not contain \"" + remaining + "\"");

        helper.succeed();
    }

    @GameTest(template = "blaze_burner_infinite")
    public static void infiniteFuelTooltip(CreateGameTestHelper helper) {
        BlazeBurnerBlockEntity burner = getBurner(helper);
        String testType = "Blaze Burner Infinite";

        // Check if the blaze burner is in creative mode
        if (!burner.isCreative())
            helper.fail(testType + ": Should be in creative mode for this test");
        // Check if the remaining burn time is 0 for infinite fuel
        if (burner.getRemainingBurnTime() != 0)
            helper.fail(testType + ": Remaining burn time should be 0 for this test. Got: " + burner.getRemainingBurnTime());

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = burner.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for an infinite fuel blaze burner
        assertTooltipContains(helper, tooltip, result, testType,
                    "create.tooltip.blaze_burner.header",
                    "create.tooltip.blaze_burner.fuel_capacity",
                    "create.tooltip.blaze_burner.infinite");

            // Assert that the tooltip does not contain the "Remaining Burn Time" text
        String fullText = collectTooltipText(tooltip);
        String remaining = Component.translatable("create.tooltip.blaze_burner.remaining").getString();
        if (fullText.contains(remaining))
            helper.fail(testType + ": Tooltip should not contain \"" + remaining + "\"");

        helper.succeed();
    }

    @GameTest(template = "blaze_burner_normal", timeoutTicks = (PROGRESSION_CHECK_SECONDS + 2) * TICKS_PER_SECOND)
    public static void normalFuelTooltipAndDecay(CreateGameTestHelper helper) {
        BlazeBurnerBlockEntity burner = getBurner(helper);
        String testType = "Blaze Burner Normal";

        // Check if the blaze burner is in creative mode (it should not be)
        if (burner.isCreative())
            helper.fail(testType + ": Should not be in creative mode for this test");
        // Check if the blaze burner has normal fuel
        if (burner.getActiveFuel() != FuelType.NORMAL)
            helper.fail(testType + ": Should have normal fuel for this test. Expected fuel type: NORMAL, got: " + burner.getActiveFuel());
        if (burner.getRemainingBurnTime() <= 0)
            helper.fail(testType + ": Remaining burn time should be greater than 0 for this test");

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = burner.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for a normal fuel blaze burner
        assertTooltipContains(helper, tooltip, result, testType,
            "create.tooltip.blaze_burner.header",
            "create.tooltip.blaze_burner.fuel_capacity",
            "create.tooltip.blaze_burner.remaining");

        // Collect the initial burn time and tooltip text for later comparison
        int initialBurnTime = burner.getRemainingBurnTime();

        // Wait for a few seconds and check if the burn time has decayed appropriately
        helper.whenSecondsPassed(PROGRESSION_CHECK_SECONDS, () -> {
            // We expect the burn time to have lowered PROGRESSION_CHECK_SECONDS
            // Note: Burn time decays at 20 ticks per second, but this can be affected by the game's tick rate
            // Create a new tooltip after burn time decay and check if the text has updated accordingly
            List<Component> updatedTooltip = new java.util.ArrayList<>();
            burner.addToGoggleTooltip(updatedTooltip, false);

            String expectedBurnTimeText = (initialBurnTime - PROGRESSION_CHECK_SECONDS * 20) / 20 + " " + Component.translatable("create.generic.unit.seconds").getString();

            String updatedTooltipText = collectTooltipText(updatedTooltip);

            if (!updatedTooltipText.contains(expectedBurnTimeText)){
                helper.fail(testType + ": Tooltip text did not update properly after delay. Expected: \""
                    + expectedBurnTimeText + "\", Got: \"" + updatedTooltipText + "\"");
            }       

            helper.succeed();
        });
    }

    // Pulse Mechanism Tooltip Tests
    @GameTest(template = "pulse_timer", timeoutTicks = (PROGRESSION_CHECK_SECONDS + 2) * TICKS_PER_SECOND)
    public static void pulseTimerTooltip(CreateGameTestHelper helper) {
        PulseTimerBlockEntity pulseTimer = helper.getBlockEntity(AllBlockEntityTypes.PULSE_TIMER.get(), BLOCK_POS);
        String testType = "Pulse Timer";

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = pulseTimer.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for a pulse timer
        assertTooltipContains(helper, tooltip, result, testType,
            "create.tooltip.pulse_timer.header",
            "create.tooltip.pulse.until_next_pulse");

        // Collect the initial tooltip text for later comparison
        String initialTooltipText = collectTooltipText(tooltip);

        // Wait for a few seconds and check if the tooltip has updated to reflect the pulse timer's progression
        helper.whenSecondsPassed(PROGRESSION_CHECK_SECONDS, () -> {
            // Create a new tooltip after time has passed and check if the text has updated accordingly
            List<Component> updatedTooltip = new java.util.ArrayList<>();
            pulseTimer.addToGoggleTooltip(updatedTooltip, false);
            String updatedTooltipText = collectTooltipText(updatedTooltip);

            if (initialTooltipText.equals(updatedTooltipText))
                helper.fail(testType + ": Tooltip text did not update after pulse timer progression. Initial: \""
                    + initialTooltipText + "\", Updated: \"" + updatedTooltipText + "\"");

            helper.succeed();
        });
    }

    @GameTest(template = "pulse_repeater", timeoutTicks = (PROGRESSION_CHECK_SECONDS + 2) * TICKS_PER_SECOND)
    public static void pulseRepeaterTooltip(CreateGameTestHelper helper) {
        PulseRepeaterBlockEntity pulseRepeater = helper.getBlockEntity(AllBlockEntityTypes.PULSE_REPEATER.get(), BLOCK_POS);
        String testType = "Pulse Repeater";

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = pulseRepeater.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for a pulse repeater
        assertTooltipContains(helper, tooltip, result, testType,
            "create.tooltip.pulse_repeater.header",
            "create.tooltip.pulse.until_next_pulse");

        // Collect the initial tooltip text for later comparison
        String initialTooltipText = collectTooltipText(tooltip);

        // Wait for a few seconds and check if the tooltip has updated to reflect the pulse repeater's progression
        helper.whenSecondsPassed(PROGRESSION_CHECK_SECONDS, () -> {
            // Create a new tooltip after time has passed and check if the text has updated accordingly
            List<Component> updatedTooltip = new java.util.ArrayList<>();
            pulseRepeater.addToGoggleTooltip(updatedTooltip, false);
            String updatedTooltipText = collectTooltipText(updatedTooltip);

            if (initialTooltipText.equals(updatedTooltipText))
                helper.fail(testType + ": Tooltip text did not update after pulse repeater progression. Initial: \""
                    + initialTooltipText + "\", Updated: \"" + updatedTooltipText + "\"");

            helper.succeed();
        });
    }

    @GameTest(template = "pulse_extender", timeoutTicks = (PROGRESSION_CHECK_SECONDS + 2) * TICKS_PER_SECOND)
    public static void pulseExtenderTooltip(CreateGameTestHelper helper) {
        PulseExtenderBlockEntity pulseExtender = helper.getBlockEntity(AllBlockEntityTypes.PULSE_EXTENDER.get(), BLOCK_POS);
        String testType = "Pulse Extender";

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = pulseExtender.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for a pulse extender
        assertTooltipContains(helper, tooltip, result, testType,
            "create.tooltip.pulse_extender.header",
            "create.tooltip.pulse_extender.remaining");

        // Collect the initial tooltip text for later comparison
        String initialTooltipText = collectTooltipText(tooltip);

        // Wait for a few seconds and check if the tooltip has updated to reflect the pulse extender's progression
        helper.whenSecondsPassed(PROGRESSION_CHECK_SECONDS, () -> {
            // Create a new tooltip after time has passed and check if the text has updated accordingly
            List<Component> updatedTooltip = new java.util.ArrayList<>();
            pulseExtender.addToGoggleTooltip(updatedTooltip, false);
            String updatedTooltipText = collectTooltipText(updatedTooltip);

            if (initialTooltipText.equals(updatedTooltipText))
                helper.fail(testType + ": Tooltip text did not update after pulse extender progression. Initial: \""
                    + initialTooltipText + "\", Updated: \"" + updatedTooltipText + "\"");

            helper.succeed();
        });
    }

    // Kinetic Blocks Tooltip Tests
    // Note: This tests don't cover all affected kinetic blocks, because many of them share the same tooltip logic

    @GameTest(template = "creative_motor_clockwise")
    public static void creativeMotorClockwiseTooltip(CreateGameTestHelper helper) {
        CreativeMotorBlockEntity motor = helper.getBlockEntity(AllBlockEntityTypes.MOTOR.get(), BLOCK_POS);
        String testType = "Creative Motor Clockwise";

        // Wait 1 second to ensure the motor has started generating speed before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            if (motor.getGeneratedSpeed() <= 0)
                helper.fail(testType + ": Motor should be generating clockwise speed for this test. Got speed: " + motor.getGeneratedSpeed());
        });

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = motor.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for a clockwise creative motor
        assertTooltipContains(helper, tooltip, result, testType,
            "create.gui.goggles.rotation_direction",
            "create.gui.goggles.rotation_direction.clockwise");

        helper.succeed();
    }

    @GameTest(template = "creative_motor_counter_clockwise")
    public static void creativeMotorCounterClockwiseTooltip(CreateGameTestHelper helper) {
        CreativeMotorBlockEntity motor = helper.getBlockEntity(AllBlockEntityTypes.MOTOR.get(), BLOCK_POS);
        String testType = "Creative Motor Counter-Clockwise";

        // Wait 1 second to ensure the motor has started generating speed before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            if (motor.getGeneratedSpeed() >= 0)
                helper.fail(testType + ": Motor should be rotating counter-clockwise for this test. Got speed: " + motor.getGeneratedSpeed());
        });

        // Create the tooltip and call the method addToGoggleTooltip to populate it
        List<Component> tooltip = new java.util.ArrayList<>();
        boolean result = motor.addToGoggleTooltip(tooltip, false);

        // Assert that the tooltip contains the expected information for a counter-clockwise creative motor
        assertTooltipContains(helper, tooltip, result, testType,
            "create.gui.goggles.rotation_direction",
            "create.gui.goggles.rotation_direction.counter_clockwise");

        helper.succeed();
    }

    @GameTest(template = "shaft_clockwise")
    public static void shaftClockwiseTooltip(CreateGameTestHelper helper) {
        BracketedKineticBlockEntity shaft = helper.getBlockEntity(AllBlockEntityTypes.BRACKETED_KINETIC.get(), BLOCK_POS);
        String testType = "Shaft Clockwise";

        // Wait 1 second to ensure the shaft has started rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            boolean result = shaft.addToGoggleTooltip(tooltip, false);

            // Assert that the tooltip contains the expected information for a clockwise shaft
            assertTooltipContains(helper, tooltip, result, testType,
                "create.tooltip.shaft.header",
                "create.gui.goggles.rotation_direction",
                "create.gui.goggles.rotation_direction.clockwise");

            helper.succeed();
        });
    }

    @GameTest(template = "shaft_counter_clockwise")
    public static void shaftCounterClockwiseTooltip(CreateGameTestHelper helper) {
        BracketedKineticBlockEntity shaft = helper.getBlockEntity(AllBlockEntityTypes.BRACKETED_KINETIC.get(), BLOCK_POS);
        String testType = "Shaft Counter-Clockwise";

        // Wait 1 second to ensure the shaft has started rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            boolean result = shaft.addToGoggleTooltip(tooltip, false);

            // Assert that the tooltip contains the expected information for a counter-clockwise shaft
            assertTooltipContains(helper, tooltip, result, testType,
                "create.tooltip.shaft.header",
                "create.gui.goggles.rotation_direction",
                "create.gui.goggles.rotation_direction.counter_clockwise");

            helper.succeed();
        });
    }

    @GameTest(template = "shaft_stop")
    public static void shaftStopTooltip(CreateGameTestHelper helper) {
        BracketedKineticBlockEntity shaft = helper.getBlockEntity(AllBlockEntityTypes.BRACKETED_KINETIC.get(), BLOCK_POS);
        String testType = "Shaft Stop";

        // Wait 1 second to ensure the shaft is not rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            shaft.addToGoggleTooltip(tooltip, false);

            // Assert that the tooltip contains the expected information for a stopped shaft
            String fullText = collectTooltipText(tooltip);
            String rotation = Component.translatable("create.gui.goggles.rotation_direction").getString();
            if (fullText.contains(rotation))
                helper.fail(testType + ": Tooltip should not contain rotation direction when shaft is stopped. Got tooltip: " + fullText);

            helper.succeed();
        });
    }

    @GameTest(template = "cogwheel_clockwise")
    public static void cogwheelClockwiseTooltip(CreateGameTestHelper helper) {
        BracketedKineticBlockEntity cogwheel = helper.getBlockEntity(AllBlockEntityTypes.BRACKETED_KINETIC.get(), BLOCK_POS);
        String testType = "Cogwheel Clockwise";

        // Wait 1 second to ensure the cogwheel has started rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            boolean result = cogwheel.addToGoggleTooltip(tooltip, false);

            // Assert that the tooltip contains the expected information for a clockwise cogwheel
            assertTooltipContains(helper, tooltip, result, testType,
                "create.tooltip.cogwheel.header",
                "create.gui.goggles.rotation_direction",
                "create.gui.goggles.rotation_direction.clockwise");

            helper.succeed();
        });
    }

    @GameTest(template = "cogwheel_counter_clockwise")
    public static void cogwheelCounterClockwiseTooltip(CreateGameTestHelper helper) {
        BracketedKineticBlockEntity cogwheel = helper.getBlockEntity(AllBlockEntityTypes.BRACKETED_KINETIC.get(), BLOCK_POS);
        String testType = "Cogwheel Counter-Clockwise";

        // Wait 1 second to ensure the cogwheel has started rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            boolean result = cogwheel.addToGoggleTooltip(tooltip, false);

            // Assert that the tooltip contains the expected information for a counter-clockwise cogwheel
            assertTooltipContains(helper, tooltip, result, testType,
                "create.tooltip.cogwheel.header",
                "create.gui.goggles.rotation_direction",
                "create.gui.goggles.rotation_direction.counter_clockwise");

            helper.succeed();
        });
    }

    @GameTest(template = "cogwheel_stop")
    public static void cogwheelStopTooltip(CreateGameTestHelper helper) {
        BracketedKineticBlockEntity cogwheel = helper.getBlockEntity(AllBlockEntityTypes.BRACKETED_KINETIC.get(), BLOCK_POS);
        String testType = "Cogwheel Stop";

        // Wait 1 second to ensure the cogwheel is not rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            cogwheel.addToGoggleTooltip(tooltip, false);

            // Assert that the tooltip contains the expected information for a stopped cogwheel
            String fullText = collectTooltipText(tooltip);
            String rotation = Component.translatable("create.gui.goggles.rotation_direction").getString();
            if (fullText.contains(rotation))
                helper.fail(testType + ": Tooltip should not contain rotation direction when cogwheel is stopped. Got tooltip: " + fullText);

            helper.succeed();
        });
    }

    // Encased Fan Tests
    @GameTest(template = "encased_fan_stop")
    public static void encasedFanStopToolTip(CreateGameTestHelper helper) {
        EncasedFanBlockEntity fan = helper.getBlockEntity(AllBlockEntityTypes.ENCASED_FAN.get(), BLOCK_POS);
        String testType = "Encased Fan Stop";

        // Wait 1 second to ensure the shaft has started rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            boolean result = fan.addToGoggleTooltip(tooltip, false);

            // Assert that the tooltip contains the expected information for a stopped encased fan
            assertTooltipContains(helper, tooltip, result, testType,
                "create.tooltip.encased_fan.header",
                "create.tooltip.encased_fan.not_spinning");

            helper.succeed();
        });
    }

    @GameTest(template = "encased_fan_inward")
    public static void encasedFanInwardToolTip(CreateGameTestHelper helper) {
        EncasedFanBlockEntity fan = helper.getBlockEntity(AllBlockEntityTypes.ENCASED_FAN.get(), BLOCK_POS);
        String testType = "Encased Fan Inward";

        // Wait 1 second to ensure the shaft has started rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            boolean result = fan.addToGoggleTooltip(tooltip, false);

            //Assert that the tooltip contains the expected information for an encased fan that is blowing inwards
            assertTooltipContains(helper, tooltip, result, testType,
                "create.tooltip.encased_fan.header",
                "create.tooltip.encased_fan.direction",
                "create.tooltip.encased_fan.inward",
                "create.tooltip.encased_fan.range"
            );
            
            helper.succeed();
        });
    }

    @GameTest(template = "encased_fan_outward")
    public static void encasedFanOutwardToolTip(CreateGameTestHelper helper) {
        EncasedFanBlockEntity fan = helper.getBlockEntity(AllBlockEntityTypes.ENCASED_FAN.get(), BLOCK_POS);
        String testType = "Encased Fan Outward";

        // Wait 1 second to ensure the shaft has started rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            boolean result = fan.addToGoggleTooltip(tooltip, false);

            // Assert that the tooltip contains the expected information for an encased fan that is blowing outwards
            assertTooltipContains(helper, tooltip, result, testType,
                "create.tooltip.encased_fan.header",
                "create.tooltip.encased_fan.direction",
                "create.tooltip.encased_fan.outward",
                "create.tooltip.encased_fan.range"
            );
            
            helper.succeed();
        });
    }

    // Deployer Filter Tests
    @GameTest(template = "deployer_no_filter")
    public static void DeployerNoFilter(CreateGameTestHelper helper) {
        DeployerBlockEntity deployer = helper.getBlockEntity(AllBlockEntityTypes.DEPLOYER.get(), BLOCK_POS);
        String testType = "Deployer No Filter";

        // Wait 1 second to ensure the shaft has started rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            boolean result = deployer.addToGoggleTooltip(tooltip, false);

            if (!result)
                helper.fail(testType + ": addToGoggleTooltip() returned false");
            if (!tooltip.isEmpty())
                helper.fail(testType + ": tooltip should be empty");
            helper.succeed();
        });
    }

    @GameTest(template = "deployer_single_item_filter")
    public static void DeployerSingleItemFilter(CreateGameTestHelper helper) {
        DeployerBlockEntity deployer = helper.getBlockEntity(AllBlockEntityTypes.DEPLOYER.get(), BLOCK_POS);
        String testType = "Deployer Single Item Filter";

        // Wait 1 second to ensure the shaft has started rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            boolean result = deployer.addToGoggleTooltip(tooltip, false);

            assertTooltipContains(helper, tooltip, result, testType,
                "create.gui.filter.allow_item");

            helper.succeed();
        });
    }

    @GameTest(template = "deployer_empty_list_filter")
    public static void DeployerEmptyListFilter(CreateGameTestHelper helper) {
        DeployerBlockEntity deployer = helper.getBlockEntity(AllBlockEntityTypes.DEPLOYER.get(), BLOCK_POS);
        String testType = "Deployer Empty List Filter";

        // Wait 1 second to ensure the shaft has started rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            boolean result = deployer.addToGoggleTooltip(tooltip, false);

            assertTooltipContains(helper, tooltip, result, testType,
                "create.gui.filter.empty");

            helper.succeed();
        });
    }

    @GameTest(template = "deployer_allow_item_list_filter")
    public static void DeployerAllowItemListFilter(CreateGameTestHelper helper) {
        DeployerBlockEntity deployer = helper.getBlockEntity(AllBlockEntityTypes.DEPLOYER.get(), BLOCK_POS);
        String testType = "Deployer Allow Item List Filter";

        // Wait 1 second to ensure the shaft has started rotating before checking the tooltip
        helper.whenSecondsPassed(1, () -> {
            // Create the tooltip and call the method addToGoggleTooltip to populate it
            List<Component> tooltip = new java.util.ArrayList<>();
            boolean result = deployer.addToGoggleTooltip(tooltip, false);

            assertTooltipContains(helper, tooltip, result, testType,
                "create.gui.filter.allow_list");

            helper.succeed();
        });
    }


    // ===== Helper Methods =====

    // Helper method to collect tooltip text into a single string for easier searching
    private static String collectTooltipText(List<Component> tooltip) {
        StringBuilder sb = new StringBuilder();
        // Concatenate all tooltip components into a single string
        for (Component component : tooltip)
            sb.append(component.getString()).append(" ");
        return sb.toString();
    }

    // Helper method to assert that the tooltip contains the expected text based on the translation key
    private static void assertTooltipContains(CreateGameTestHelper helper, List<Component> tooltip,
        boolean result, String testType, String... translationKeys) {

        // Check if addToGoggleTooltip returned true and that the tooltip is not empty
        if (!result)
            helper.fail(testType + ": addToGoggleTooltip() returned false");
        if (tooltip.isEmpty())
            helper.fail(testType + ": tooltip is empty");

        // Collect the full tooltip text for easier searching
        String fullText = collectTooltipText(tooltip);

        // Check that each expected message is present in the tooltip
        // Note: Used Component.translatable to get the expected text based on the translation key
        for (String key : translationKeys) {
            String expected = Component.translatable(key).getString();

            if (!fullText.contains(expected))
                helper.fail(testType + ": tooltip does not contain \"" + expected
                    + "\". Content: " + fullText);
        }
    }

    private static BlazeBurnerBlockEntity getBurner(CreateGameTestHelper helper) {
        return helper.getBlockEntity(AllBlockEntityTypes.HEATER.get(), BLOCK_POS);
    }
}
