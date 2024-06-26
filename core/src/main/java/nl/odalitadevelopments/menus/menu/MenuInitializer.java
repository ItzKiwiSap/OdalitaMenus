package nl.odalitadevelopments.menus.menu;

import lombok.AllArgsConstructor;
import nl.odalitadevelopments.menus.annotations.Menu;
import nl.odalitadevelopments.menus.contents.MenuContents;
import nl.odalitadevelopments.menus.items.ItemProcessor;
import nl.odalitadevelopments.menus.menu.providers.MenuProvider;
import nl.odalitadevelopments.menus.menu.type.InventoryCreation;
import nl.odalitadevelopments.menus.menu.type.SupportedMenuType;
import nl.odalitadevelopments.menus.menu.type.SupportedMenuTypes;
import nl.odalitadevelopments.menus.nms.OdalitaMenusNMS;
import nl.odalitadevelopments.menus.nms.utils.OdalitaLogger;
import nl.odalitadevelopments.menus.pagination.IPagination;
import nl.odalitadevelopments.menus.providers.providers.ColorProvider;
import nl.odalitadevelopments.menus.scrollable.Scrollable;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
final class MenuInitializer<P extends MenuProvider> {

    private final MenuProcessor menuProcessor;
    private final ItemProcessor itemProcessor;
    private final SupportedMenuTypes supportedMenuTypes;
    private final MenuOpenerBuilderImpl<P> builder;

    void initializeMenu() {
        P menuProvider = this.builder.getProvider();
        Player player = this.builder.getPlayer();

        try {
            Menu annotation = menuProvider.getClass().getAnnotation(Menu.class);

            ColorProvider colorProvider = this.menuProcessor.getInstance().getProvidersContainer().getColorProvider();
            String inventoryTitle = colorProvider.handle(annotation.title());

            SupportedMenuType menuType = this.supportedMenuTypes.getSupportedMenuType(annotation.type());
            InventoryCreation inventoryCreation = menuType.createInventory(player, inventoryTitle);

            String menuId = (annotation.id().isEmpty() || annotation.id().isBlank()) ? null : annotation.id();
            MenuSession menuSession = new MenuSession(this.menuProcessor.getInstance(), this.builder, player, menuId, menuType, inventoryCreation, annotation.title(), annotation.globalCacheKey());

            MenuContents contents = menuSession.getMenuContents();
            this.builder.getProviderLoader().load(menuProvider, player, contents);
            menuSession.initialized();

            this.builder.getPaginationPages().forEach((id, page) -> {
                IPagination<?, ?> pagination = menuSession.getCache().getPaginationMap().get(id);
                if (pagination == null) return;

                pagination.setPage(page);
            });

            this.builder.getScrollableAxes().forEach((id, axes) -> {
                Scrollable scrollable = menuSession.getCache().getScrollableMap().get(id);
                if (scrollable == null) return;

                scrollable.setAxes(axes.getKey(), axes.getValue());
            });

            this.itemProcessor.initializeItems(menuSession, contents);

            this.openInventory(player, menuSession);

            Bukkit.getScheduler().runTaskLater(this.menuProcessor.getInstance().getJavaPlugin(), menuSession::opened, 1L);
        } catch (Exception exception) {
            OdalitaLogger.error(exception);
        }
    }

    private void openInventory(Player player, MenuSession menuSession) {
        MenuSession oldSession = this.menuProcessor.getOpenMenus().put(player, menuSession);
        if (oldSession != null) {
            // Mark old session as closed, if present to prevent items being set in the wrong inventory
            oldSession.setClosed(true);
        }

        InventoryCreation inventoryData = menuSession.getInventoryData();
        Object nmsInventory = inventoryData.nmsInventory();
        Object inventoryToOpen = nmsInventory == null ? inventoryData.bukkitInventory() : nmsInventory;

        try {
            OdalitaMenusNMS.getInstance().openInventory(player, inventoryToOpen, menuSession.getTitle());

            // Make sure the player is added to the viewers list
            List<HumanEntity> viewers = menuSession.getInventory().getViewers();
            if (!viewers.contains(player)) {
                viewers.add(player);
            }
        } catch (Exception exception) {
            OdalitaLogger.error(exception);
            this.menuProcessor.getOpenMenus().remove(player);
        }
    }
}