package nl.odalitadevelopments.menus.items;

import nl.odalitadevelopments.menus.OdalitaMenus;
import nl.odalitadevelopments.menus.contents.MenuContents;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class MenuItem {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private final int id = ID_COUNTER.get() >= 10_000 ? ID_COUNTER.getAndSet(0) : ID_COUNTER.getAndIncrement();

    public abstract @NotNull ItemStack getItemStack(@NotNull OdalitaMenus instance, @NotNull MenuContents contents);

    public abstract @NotNull Consumer<InventoryClickEvent> onClick(@NotNull OdalitaMenus instance, @NotNull MenuContents contents);

    public boolean isUpdatable() {
        return false;
    }

    public int getUpdateTicks() {
        return -1;
    }

    public final int getId() {
        return this.id;
    }
}