package nl.tritewolf.tritemenus.annotations;

import nl.tritewolf.tritemenus.menu.TriteMenuType;
import org.bukkit.event.inventory.InventoryType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TriteMenu {

    String displayName();

    TriteMenuType menuType();

    byte rows() default 3;

    InventoryType inventoryType() default InventoryType.CHEST;
}
