package nl.odalitadevelopments.menus.pagination;

import nl.odalitadevelopments.menus.contents.MenuContents;
import nl.odalitadevelopments.menus.items.MenuItem;
import nl.odalitadevelopments.menus.iterators.MenuObjectIterator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class ObjectPaginationImpl<T> extends AbstractPagination<ObjectPagination<T>, MenuObjectIterator<T>> implements ObjectPagination<T> {

    ObjectPaginationImpl(MenuContents contents, String id, int itemsPerPage, MenuObjectIterator<T> iterator) {
        super(contents, id, itemsPerPage, iterator);
    }

    @Override
    protected ObjectPagination<T> self() {
        return this;
    }

    @Override
    public @NotNull MenuObjectIterator<T> iterator() {
        return this.iterator;
    }

    @Override
    public int lastPage() {
        return Math.max(0, (int) (Math.ceil((double) this.iterator.getObjects().size() / (double) this.itemsPerPage) - 1));
    }

    @Override
    public @NotNull ObjectPagination<T> addItem(@NotNull T value) {
        this.iterator.add(value);
        return this;
    }

    @Override
    public @NotNull ObjectPagination<T> filter(@NotNull String id, @NotNull Predicate<T> predicate) {
        this.iterator.filter(id, predicate);
        return this;
    }

    @Override
    public @NotNull ObjectPagination<T> sorter(int priority, @NotNull Comparator<@NotNull T> comparator) {
        this.iterator.sorter(priority, comparator);
        return this;
    }

    @Override
    public @NotNull ObjectPagination<T> removeFilter(@NotNull String id) {
        this.iterator.removeFilter(id);
        return this;
    }

    @Override
    public @NotNull ObjectPagination<T> removeSorter(int priority) {
        this.iterator.removeSorter(priority);
        return this;
    }

    @Override
    public void apply() {
        this.iterator.apply();
    }

    @Override
    public void createBatch() {
        this.iterator.createBatch();
    }

    @Override
    public void endBatch() {
        this.iterator.endBatch();
    }

    @ApiStatus.Internal
    @Override
    public @NotNull List<Supplier<MenuItem>> getItemsOnPage() {
        if (this.initialized || this.contents.menuFrameData() != null) return List.of();

        return this.getItemsOnPage(this.currentPage);
    }

    @Override
    protected List<Supplier<MenuItem>> getItemsOnPage(int page) {
        List<Supplier<MenuItem>> items = new ArrayList<>();

        List<T> objects = this.iterator.getObjects();
        List<T> pageList = objects.subList(page * this.itemsPerPage, Math.min(objects.size(), (page + 1) * this.itemsPerPage));

        for (T value : pageList) {
            MenuItem menuItem = this.iterator.createMenuItem(value);
            if (menuItem == null) continue;

            items.add(() -> menuItem);
        }

        for (int i = items.size(); i < this.itemsPerPage; i++) {
            items.add(null);
        }

        return items;
    }
}