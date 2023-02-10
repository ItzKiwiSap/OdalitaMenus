package nl.odalitadevelopments.menus.menu.type.types;

import nl.odalitadevelopments.menus.menu.type.MenuType;
import nl.odalitadevelopments.menus.menu.type.SupportedFeatures;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public final class BrewingMenuType implements MenuType {

    @Override
    public @NotNull InventoryType type() {
        return InventoryType.BREWING;
    }

    @Override
    public int maxRows() {
        return 0;
    }

    @Override
    public int maxColumns() {
        return 0;
    }

    @Override
    public @NotNull Collection<@NotNull Integer> otherSlots() {
        return Set.of(0, 1, 2, 3, 4);
    }

    @Override
    public @NotNull Collection<@NotNull SupportedFeatures> disallowedFeatures() {
        return Set.of(
                SupportedFeatures.PAGINATION,
                SupportedFeatures.SCROLLABLE
        );
    }
}