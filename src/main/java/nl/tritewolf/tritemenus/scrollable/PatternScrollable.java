package nl.tritewolf.tritemenus.scrollable;

import nl.tritewolf.tritemenus.contents.SlotPos;
import nl.tritewolf.tritemenus.items.MenuItem;
import nl.tritewolf.tritemenus.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

sealed class PatternScrollable extends AbstractScrollable permits ContinuousPatternScrollable {

    private final ScrollableDirectionPatternCache patternCache;

    private int lastPatternIndex;

    PatternScrollable(@NotNull ScrollableBuilderImpl builder) {
        super(builder);

        this.patternCache = builder.getPatternCache();

        this.lastPatternIndex = -1;
    }

    @Override
    public synchronized @NotNull Scrollable addItem(@NotNull Supplier<@NotNull MenuItem> menuItemSupplier) {
        Map.Entry<Integer, Integer> newIndexEntry = this.patternCache.index().entrySet().stream()
                .filter((entry) -> entry.getValue() > this.lastPatternIndex)
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .orElse(null);

        int index;
        if (newIndexEntry == null || (index = newIndexEntry.getKey()) <= -1) {
            return this;
        }

        this.lastPatternIndex = newIndexEntry.getValue();

        ScrollableSlotPos slotPos = this.createSlotPos(index);
        int row = slotPos.getRow();
        int column = slotPos.getColumn();

        if (row < this.showYAxis && column < this.showXAxis) {
            this.contents.setAsync(SlotPos.of(this.startRow + row, this.startColumn + column), menuItemSupplier.get());
        }

        this.items.put(index, menuItemSupplier);
        return this;
    }

    @Override
    public int lastVertical() {
        return this.patternCache.height() - this.showYAxis;
    }

    @Override
    public int lastHorizontal() {
        return this.patternCache.width() - this.showXAxis;
    }

    @Override
    @NotNull Scrollable open(int newAxis, @NotNull ScrollableDirection direction) {
        if (!this.isValidDirection(direction)) return this;

        int lastAxis = (direction == ScrollableDirection.HORIZONTALLY) ? this.lastHorizontal() : this.lastVertical();
        if (newAxis > lastAxis) return this;

        newAxis = Math.max(0, newAxis);

        Map<Integer, Supplier<MenuItem>> pageItems = this.getPageItems(newAxis, direction);
        if (pageItems.isEmpty()) return this;

        this.cleanMenuGrid();

        if (this.contents.getTriteMenu().isHasUpdatableItems()) {
            this.contents.getTriteMenu().setHasUpdatableItems(false);
        }

        this.setNewPage(newAxis, direction);

        for (Map.Entry<Integer, Supplier<MenuItem>> entry : pageItems.entrySet()) {
            Pair<Integer, Integer> pair = this.getUpdateRowColumn(entry.getKey(), newAxis, direction);
            int row = pair.getKey();
            int column = pair.getValue();

            if (row < this.startRow || column < this.startColumn || row > this.showYAxis || column > this.showXAxis) {
                continue;
            }

            this.updateItem(SlotPos.of(row, column).getSlot(), entry.getValue());
        }

        return this;
    }

    protected Pair<Integer, Integer> getUpdateRowColumn(int index, int newAxis, ScrollableDirection direction) {
        ScrollableSlotPos slotPos = this.createSlotPos(index);
        int row = this.startRow + slotPos.getRow() - this.currentYAxis;
        int column = this.startColumn + slotPos.getColumn() - this.currentXAxis;
        return new Pair<>(row, column);
    }

    protected Pair<Integer, Integer> getPageItemsStartEndIndex(int newAxis, ScrollableDirection direction) {
        int startIndex = this.createSlotPos(
                (direction == ScrollableDirection.HORIZONTALLY) ? this.currentYAxis : newAxis, // row
                (direction == ScrollableDirection.HORIZONTALLY) ? newAxis : this.currentXAxis // column
        ).getSlot();

        int endIndex = this.createSlotPos(
                (direction == ScrollableDirection.HORIZONTALLY) ? this.showYAxis + this.currentYAxis - 1 : this.showYAxis + newAxis - 1, // row
                (direction == ScrollableDirection.HORIZONTALLY) ? this.showXAxis + newAxis : this.showXAxis + this.currentXAxis // column
        ).getSlot();

        return new Pair<>(startIndex, endIndex);
    }

    private Map<Integer, Supplier<MenuItem>> getPageItems(int newAxis, ScrollableDirection direction) {
        Pair<Integer, Integer> pair = this.getPageItemsStartEndIndex(newAxis, direction);
        int startIndex = pair.getKey();
        int endIndex = pair.getValue();

        Map<Integer, Supplier<MenuItem>> pageItems = new HashMap<>(
                this.items.subMap(startIndex, endIndex)
        );

        for (int i = startIndex; i < endIndex; i++) {
            pageItems.putIfAbsent(i, null);
        }

        return pageItems;
    }

    private ScrollableSlotPos createSlotPos(int index) {
        return ScrollableSlotPos.of(this.patternCache.height(), this.patternCache.width(), index);
    }

    private ScrollableSlotPos createSlotPos(int row, int column) {
        return ScrollableSlotPos.of(this.patternCache.height(), this.patternCache.width(), row, column);
    }
}