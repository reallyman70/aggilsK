package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {

    private final DecimalSetting health = DecimalSetting.Builder.newInstance()
            .setName("Health")
            .setDescription("Switches to totem when health is below this")
            .setModule(this)
            .setValue(12)
            .setMin(1)
            .setMax(36)
            .setStep(0.5)
            .build();

    private final BooleanSetting lethalOnly = BooleanSetting.Builder.newInstance()
            .setName("LethalOnly")
            .setDescription("Only switches if damage would kill you")
            .setModule(this)
            .setValue(false)
            .build();

    private final BooleanSetting checkCrystals = BooleanSetting.Builder.newInstance()
            .setName("CheckCrystals")
            .setDescription("Only activates if crystals are nearby")
            .setModule(this)
            .setValue(false)
            .build();

    private final DecimalSetting crystalRange = DecimalSetting.Builder.newInstance()
            .setName("CrystalRange")
            .setDescription("Range to check for crystals")
            .setModule(this)
            .setValue(6)
            .setMin(1)
            .setMax(12)
            .setStep(0.5)
            .setAvailability(checkCrystals::get)
            .build();

    private final DecimalSetting delay = DecimalSetting.Builder.newInstance()
            .setName("Delay")
            .setDescription("Ticks between swaps")
            .setModule(this)
            .setValue(0)
            .setMin(0)
            .setMax(20)
            .setStep(1)
            .build();

    private int timer = 0;

    public AutoTotem() {
        super("AutoTotem", "Automatically puts a totem in your offhand", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (timer > 0) {
            timer--;
            return;
        }

        ClientPlayerEntity player = mc.player;

        // Already holding totem
        if (player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING)
            return;

        float totalHealth = player.getHealth() + player.getAbsorptionAmount();

        // Health check
        if (!lethalOnly.get() && totalHealth > health.get())
            return;

        // Crystal check
        if (checkCrystals.get() && !isCrystalNearby())
            return;

        int slot = findTotemSlot();
        if (slot == -1)
            return;

        swapToOffhand(slot);
        timer = (int) delay.get();
    }

    private boolean isCrystalNearby() {
        double rangeSq = crystalRange.get() * crystalRange.get();
        for (EndCrystalEntity crystal : mc.world.getEntitiesByClass(
                EndCrystalEntity.class,
                mc.player.getBoundingBox().expand(crystalRange.get()),
                e -> true)) {

            if (mc.player.squaredDistanceTo(crystal) <= rangeSq)
                return true;
        }
        return false;
    }

    private int findTotemSlot() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING)
                return i;
        }
        return -1;
    }

    private void swapToOffhand(int slot) {
        int containerSlot = slot < 9 ? slot + 36 : slot;

        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                containerSlot,
                0,
                SlotActionType.PICKUP,
                mc.player
        );

        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                45,
                0,
                SlotActionType.PICKUP,
                mc.player
        );

        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                containerSlot,
                0,
                SlotActionType.PICKUP,
                mc.player
        );
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(health.get());
    }
}
