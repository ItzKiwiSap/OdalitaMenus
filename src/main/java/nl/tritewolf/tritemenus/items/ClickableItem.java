package nl.tritewolf.tritemenus.items;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClickableItem implements MenuItem {

    public static @NotNull ClickableItem of(@NotNull ItemStack itemStack, @NotNull Consumer<@NotNull InventoryClickEvent> clickHandler) {
        return new ClickableItem(itemStack, clickHandler);
    }

    private final ItemStack itemStack;
    private final Consumer<InventoryClickEvent> clickHandler;

    @Override
    public @NotNull ItemStack getItemStack() {
        return this.itemStack;
    }

    @Override
    public @NotNull Consumer<InventoryClickEvent> onClick() {
        return this.clickHandler;
    }
}