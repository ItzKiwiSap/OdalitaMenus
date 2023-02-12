package nl.odalitadevelopments.menus;

import lombok.AccessLevel;
import lombok.Getter;
import nl.odalitadevelopments.menus.items.ItemProcessor;
import nl.odalitadevelopments.menus.listeners.InventoryListener;
import nl.odalitadevelopments.menus.menu.MenuOpenerBuilder;
import nl.odalitadevelopments.menus.menu.MenuProcessor;
import nl.odalitadevelopments.menus.menu.providers.MenuProvider;
import nl.odalitadevelopments.menus.menu.providers.MenuProviderLoader;
import nl.odalitadevelopments.menus.menu.providers.frame.MenuFrameProvider;
import nl.odalitadevelopments.menus.menu.providers.frame.MenuFrameProviderLoader;
import nl.odalitadevelopments.menus.menu.session.SessionCache;
import nl.odalitadevelopments.menus.menu.type.SupportedMenuTypes;
import nl.odalitadevelopments.menus.patterns.MenuPattern;
import nl.odalitadevelopments.menus.patterns.PatternContainer;
import nl.odalitadevelopments.menus.providers.ProvidersContainer;
import nl.odalitadevelopments.menus.tasks.MenuTasksProcessor;
import nl.odalitadevelopments.menus.utils.cooldown.CooldownContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Getter
final class OdalitaMenusImpl implements OdalitaMenus, Listener {

    private static final Map<Plugin, OdalitaMenus> INSTANCES = new HashMap<>();

    static @NotNull OdalitaMenus createInstance(@NotNull JavaPlugin javaPlugin) {
        return new OdalitaMenusImpl(javaPlugin);
    }

    static @NotNull OdalitaMenus getInstance(@NotNull JavaPlugin javaPlugin) {
        OdalitaMenus instance = INSTANCES.get(javaPlugin);
        if (instance == null) {
            throw new IllegalStateException("OdalitaMenus is not initialized for this plugin! (JavaPlugin: " + javaPlugin.getName() + ")");
        }

        return instance;
    }

    static boolean hasInstance(@NotNull JavaPlugin javaPlugin) {
        return INSTANCES.containsKey(javaPlugin);
    }

    private final JavaPlugin javaPlugin;

    private final SupportedMenuTypes supportedMenuTypes;

    private final ItemProcessor itemProcessor;
    private final MenuProcessor menuProcessor;
    private final SessionCache sessionCache;

    private final PatternContainer patternContainer;

    private final ProvidersContainer providersContainer;
    private final CooldownContainer cooldownContainer;

    @Getter(AccessLevel.NONE)
    private final InventoryListener inventoryListener;
    @Getter(AccessLevel.NONE)
    private final ScheduledFuture<?> menuTask;

    private OdalitaMenusImpl(JavaPlugin javaPlugin) {
        if (INSTANCES.containsKey(javaPlugin)) {
            throw new IllegalStateException("OdalitaMenus is already initialized for this plugin! (JavaPlugin: " + javaPlugin.getName() + ")");
        }

        this.javaPlugin = javaPlugin;

        this.supportedMenuTypes = new SupportedMenuTypes();

        this.itemProcessor = new ItemProcessor();
        this.menuProcessor = new MenuProcessor(this, this.itemProcessor, this.supportedMenuTypes);
        this.sessionCache = new SessionCache();

        this.patternContainer = new PatternContainer();

        this.providersContainer = new ProvidersContainer();
        this.cooldownContainer = new CooldownContainer();

        this.inventoryListener = new InventoryListener(this, this.menuProcessor);
        javaPlugin.getServer().getPluginManager().registerEvents(this.inventoryListener, javaPlugin);
        javaPlugin.getServer().getPluginManager().registerEvents(this.sessionCache, javaPlugin);
        javaPlugin.getServer().getPluginManager().registerEvents(this, javaPlugin);

        this.menuTask = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new MenuTasksProcessor(this), 0, 50, TimeUnit.MILLISECONDS);

        INSTANCES.put(javaPlugin, this);
    }

    @Override
    public <P extends MenuProvider> void registerProviderLoader(@NotNull Class<P> providerClass, @NotNull MenuProviderLoader<P> loader) {
        this.menuProcessor.registerProviderLoader(providerClass, loader);
    }

    @Override
    public <P extends MenuProvider> boolean isProviderLoaderRegistered(@NotNull Class<P> providerClass) {
        return this.menuProcessor.isProviderLoaderRegistered(providerClass);
    }

    @Override
    public <P extends MenuFrameProvider> void registerFrameProviderLoader(@NotNull Class<P> providerClass, @NotNull MenuFrameProviderLoader<P> loader) {
        this.menuProcessor.getMenuFrameProcessor().registerFrameProviderLoader(providerClass, loader);
    }

    @Override
    public <P extends MenuFrameProvider> boolean isFrameProviderLoaderRegistered(@NotNull Class<P> providerClass) {
        return this.menuProcessor.getMenuFrameProcessor().isFrameProviderLoaderRegistered(providerClass);
    }

    @Override
    public <P extends MenuProvider> void openMenu(@NotNull P menuProvider, @NotNull Player player, @NotNull MenuProviderLoader<P> providerLoader) {
        this.menuProcessor.openMenu(menuProvider, player, providerLoader);
    }

    @Override
    public void openMenu(@NotNull MenuProvider menuProvider, @NotNull Player player) {
        this.menuProcessor.openMenu(menuProvider, player);
    }

    @Override
    public <P extends MenuProvider> @NotNull MenuOpenerBuilder openMenuBuilder(@NotNull P menuProvider, @NotNull Player player,
                                                                               @NotNull MenuProviderLoader<P> providerLoader) {
        return this.menuProcessor.openMenuBuilder(menuProvider, player, providerLoader);
    }

    @Override
    public <P extends MenuProvider> @NotNull MenuOpenerBuilder openMenuBuilder(@NotNull P menuProvider, @NotNull Player player) {
        return this.menuProcessor.openMenuBuilder(menuProvider, player);
    }

    @Override
    public <CacheType, T extends MenuPattern<CacheType>> void registerPattern(@NotNull T pattern) {
        this.patternContainer.registerPattern(pattern);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPluginDisable(PluginDisableEvent event) {
        if (this.javaPlugin.equals(event.getPlugin())) {
            for (Player player : this.menuProcessor.getOpenMenus().keySet()) {
                player.closeInventory();
            }

            this.menuTask.cancel(true);

            HandlerList.unregisterAll(this.inventoryListener);
            HandlerList.unregisterAll(this.sessionCache);
            HandlerList.unregisterAll(this);

            INSTANCES.remove(this.javaPlugin);
        }
    }
}