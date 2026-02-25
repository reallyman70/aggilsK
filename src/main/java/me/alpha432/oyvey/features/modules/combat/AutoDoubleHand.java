package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ClickType;

public class AutoDoubleHand extends Module {

    private float healthThreshold = 12.0f; // change this if you want

    public AutoDoubleHand() {
        super("AutoDoubleHand", "Automatically equips a totem in offhand", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        LocalPlayer player = mc.player;

        // Already holding totem
        if (player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING)
            return;

        float totalHealth = player.getHealth() + player.getAbsorptionAmount();

        // Only activate when below threshold
        if (totalHealth > healthThreshold)
            return;

        int slot = findTotemSlot();
        if (slot == -1)
            return;

        swapToOffhand(slot);
    }

    private int findTotemSlot() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING)
                return i;
        }
        return -1;
    }

    private void swapToOffhand(int slot) {
        int containerSlot = slot < 9 ? slot + 36 : slot;

        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                containerSlot,
                0,
                ClickType.PICKUP,
                mc.player
        );

        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                45, // offhand slot
                0,
                ClickType.PICKUP,
                mc.player
        );

        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                containerSlot,
                0,
                ClickType.PICKUP,
                mc.player
        );
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(healthThreshold);
    }
}
