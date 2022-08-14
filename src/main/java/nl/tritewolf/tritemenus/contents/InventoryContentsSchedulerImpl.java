package nl.tritewolf.tritemenus.contents;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import nl.tritewolf.tritemenus.menu.MenuSession;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class InventoryContentsSchedulerImpl implements InventoryContentsScheduler {

    private final MenuSession menuSession;

    @Override
    public @NotNull MenuTask schedule(@NotNull String id, @NotNull Runnable runnable, int ticksPeriod, int runTimes) {
        return this.delay(id, runnable, 0, ticksPeriod, runTimes);
    }

    @Override
    public @NotNull MenuTask delay(@NotNull String id, @NotNull Runnable runnable, int ticksDelay, int ticksPeriod, int runTimes) {
        if (ticksDelay < 0 || ticksPeriod < 0) {
            throw new IllegalArgumentException("Delay, period must be positive");
        }

        if (runTimes <= 0) {
            throw new IllegalArgumentException("You can't start a task you don't want to run");
        }

        if (this.menuSession.getCache().getTasks().containsKey(id)) {
            throw new IllegalArgumentException("Task with id '" + id + "' already exists");
        }

        MenuTask task = new MenuTask(this, id, runnable, ticksDelay, ticksPeriod, runTimes);
        this.menuSession.getCache().getTasks().put(task.getId(), task);
        return task;
    }

    @Override
    public boolean isRunning(@NotNull String id) {
        return this.menuSession.getCache().getTasks().containsKey(id);
    }

    @Override
    public void cancel(@NotNull String id) {
        this.menuSession.getCache().getTasks().remove(id);
    }
}