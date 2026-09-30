package de.guntram.mcmod.randomblockplacement;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class RandomBlockPlacement implements ClientModInitializer {
    static final String MODID = "randomblockplacement";
    static final String MODNAME = "RandomBlockPlacement";

    private static RandomBlockPlacement instance;
    private boolean isActive;
    private int minSlot, maxSlot;
    private KeyMapping onOff;

    @Override
    public void onInitializeClient() {
        instance = this;
        setKeyBindings();
        registerCommands();
        isActive = false;
        minSlot = 0;
        maxSlot = Inventory.getSelectionSize() - 1;
    }

    private void setKeyBindings() {
        final KeyMapping.Category category = KeyMapping.Category.register(Identifier.withDefaultNamespace("key.categories.randomblockplacement"));
        KeyMappingHelper.registerKeyMapping(onOff = new KeyMapping("key.randomblockplacement.toggle", InputConstants.Type.KEYBOARD, InputConstants.KEY_R, category));
        ClientTickEvents.END_CLIENT_TICK.register(e -> processKeyBinds());
    }

    private void processKeyBinds() {
        if (onOff.consumeClick()) {
            if (isActive) {
                instance.setInactive();
            } else {
                instance.setActive();
            }
        }
    }

    public static RandomBlockPlacement getInstance() {
        return instance;
    }

    public void setInactive() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(Component.translatable("msg.inactive"));
        }
        isActive = false;
    }

    public void setActive(int first, int last) {
        minSlot = first - 1;
        maxSlot = last - 1;
        if (maxSlot >= Inventory.getSelectionSize()) {
            maxSlot = Inventory.getSelectionSize() - 1;
        }
        setActive();
    }

    public void setActive() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(Component.translatable("msg.active", minSlot + 1, maxSlot + 1));
        }
        isActive = true;
    }

    public void onPlayerInteract() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Inventory inventory = player.getInventory();
        int index = inventory.getSelectedSlot();
        if (maxSlot >= Inventory.getSelectionSize()) {
            maxSlot = Inventory.getSelectionSize() - 1;
        }
        if (isActive && index >= minSlot && index <= maxSlot
                && (inventory.getItem(index).getItem() == Items.AIR
                || inventory.getItem(index).getItem() instanceof BlockItem)) {
            int totalBlocks = 0;
            for (int i = minSlot; i <= maxSlot; i++) {
                totalBlocks += getBlockCount(inventory, i);
            }
            int targetCount = (int) (Math.random() * totalBlocks);
            int targetSlot = minSlot;
            while (targetSlot < maxSlot && targetCount >= getBlockCount(inventory, targetSlot)) {
                targetCount -= getBlockCount(inventory, targetSlot);
                targetSlot++;
            }
            inventory.setSelectedSlot(targetSlot);
        }
    }

    private int getBlockCount(Inventory inventory, int targetSlot) {
        ItemStack stack = inventory.getItem(targetSlot);
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            return stack.getCount();
        } else {
            return 0;
        }
    }

    public void registerCommands() {
        int size = Inventory.getSelectionSize();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("rblock")
                            .then(
                                    literal("off").executes(c -> {
                                        instance.setInactive();
                                        return 1;
                                    })
                            )
                            .then(
                                    argument("b1", integer(1, size)).then(
                                            argument("b2", integer(1, size)).executes(c -> {
                                                instance.setActive(getInteger(c, "b1"), getInteger(c, "b2"));
                                                return 1;
                                            })
                                    )
                            )
            );
        });
    }
}
