package nl.tritewolf.tritemenus.listeners;

import nl.tritewolf.tritejection.annotations.TriteJect;
import nl.tritewolf.tritemenus.contents.SlotPos;
import nl.tritewolf.tritemenus.items.MenuItem;
import nl.tritewolf.tritemenus.menu.MenuProcessor;
import nl.tritewolf.tritemenus.menu.MenuSession;
import nl.tritewolf.tritemenus.menu.PlaceableItemsCloseAction;
import nl.tritewolf.tritemenus.menu.type.MenuType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class InventoryListener implements Listener {

    @TriteJect
    private MenuProcessor menuProcessor;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        MenuSession openMenuSession = this.menuProcessor.getOpenMenus().get(player);
        if (openMenuSession == null) return;

        MenuType menuType = openMenuSession.getMenuType();
        Inventory clickedInventory = event.getClickedInventory();

        if (event.getView().getTopInventory().equals(openMenuSession.getInventory())) {
            List<Integer> placeableItems = openMenuSession.getCache().getPlaceableItems();
            if (event.getClick().isShiftClick() && !event.getView().getTopInventory().equals(clickedInventory)) {
                event.setCancelled(true);
                return;
            }

            if (!placeableItems.isEmpty() && event.getView().getBottomInventory().equals(clickedInventory)) return;
            if (placeableItems.contains(event.getSlot())) return;

            event.setCancelled(true);

            MenuItem menuItem = openMenuSession.getContent(SlotPos.of(menuType.maxRows(), menuType.maxColumns(), event.getSlot()));
            if (menuItem != null) {
                menuItem.onClick().accept(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        MenuSession openMenuSession = this.menuProcessor.getOpenMenus().get(player);
        if (openMenuSession == null) return;

        List<Integer> placeableItems = openMenuSession.getCache().getPlaceableItems();

        if (event.getView().getTopInventory().equals(openMenuSession.getInventory())) {
            Set<Integer> inventorySlots = event.getInventorySlots();

            boolean b = event.getRawSlots().stream().allMatch(integer -> integer > (openMenuSession.getInventory().getSize() - 1));
            if (!placeableItems.isEmpty() && b) return;

            boolean matchAllSlots = new HashSet<>(placeableItems).containsAll(inventorySlots);
            if (!matchAllSlots) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        MenuSession openMenuSession = this.menuProcessor.getOpenMenus().get(player);
        if (openMenuSession == null) return;

        Inventory inventory = event.getInventory();
        if (openMenuSession.getInventory().equals(inventory)) {
            PlaceableItemsCloseAction placeableItemsCloseAction = openMenuSession.getCache().getPlaceableItemsCloseAction();

            if (placeableItemsCloseAction != null && placeableItemsCloseAction.equals(PlaceableItemsCloseAction.RETURN)) {
                List<Integer> placeableItems = openMenuSession.getCache().getPlaceableItems();

                placeableItems.forEach(integer -> {
                    ItemStack item = inventory.getItem(integer);
                    if (item != null) player.getInventory().addItem(item);
                });
            }

            this.menuProcessor.getOpenMenus().remove(player);
        }
    }
}