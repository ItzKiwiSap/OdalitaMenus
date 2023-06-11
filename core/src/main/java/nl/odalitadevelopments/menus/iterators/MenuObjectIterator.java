package nl.odalitadevelopments.menus.iterators;

import nl.odalitadevelopments.menus.contents.MenuContents;
import nl.odalitadevelopments.menus.items.MenuItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class MenuObjectIterator<T> extends AbstractMenuIterator<MenuObjectIterator<T>> {

    private final List<T> objects;
    private final Function<T, MenuItem> menuItemCreatorFunction;

    private boolean batch = false;
    private Comparator<T> comparator;

    public MenuObjectIterator(MenuContents contents, MenuIteratorType type, int startRow, int startColumn,
                              Collection<T> objects, Function<T, MenuItem> menuItemCreatorFunction) {
        super(contents, type, startRow, startColumn);

        this.objects = new ArrayList<>(objects);
        this.menuItemCreatorFunction = menuItemCreatorFunction;
    }

    @Override
    protected @NotNull MenuObjectIterator<T> self() {
        return this;
    }

    public int size() {
        return this.objects.size();
    }

    public void createBatch() {
        this.batch = true;
    }

    public void endBatch() {
        this.batch = false;

        if (this.comparator != null) {
            this.sort(this.comparator);
        }
    }

    public void add(@NotNull T value) {
        this.objects.add(value);

        if (this.comparator != null && !this.batch) {
            this.sort(this.comparator);
        }
    }

    public void sort(@NotNull Comparator<@NotNull T> comparator) {
        this.reset();

        this.sort0(comparator);

        for (T value : this.objects) {
            if (this.hasNext()) {
                int slot = this.next();
                this.set0(value, slot);
            }
        }

        this.comparator = comparator;
    }

    private void set0(@NotNull T value, int slot) {
        MenuItem item = this.menuItemCreatorFunction.apply(value);

        this.contents.set(slot, item);
    }

    private void sort0(Comparator<T> comparator) {
        this.objects.sort(comparator);
    }
}