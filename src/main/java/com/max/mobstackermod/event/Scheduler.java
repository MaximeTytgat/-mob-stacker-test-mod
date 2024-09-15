package com.max.mobstackermod.event;

import net.minecraft.server.MinecraftServer;

import java.util.Hashtable;

public class Scheduler {
    private final Hashtable<Long, Task> tasks = new Hashtable<>();
    private final MinecraftServer server;

    public Scheduler(MinecraftServer server) {
        this.server = server;
    }

    public void schedule(Task t) {
        tasks.put(server.getTickCount() + t.delay, t);
    }

    public void tick() {
        long currentTick = server.getTickCount();
        for (long taskTick : tasks.keySet()) {
            Task t = tasks.get(taskTick);
            if (taskTick <= currentTick) {
                t.run();

                if (t.period < 1) {
                    tasks.remove(taskTick);
                } else {
                    tasks.put(server.getTickCount() + t.period, t);
                    tasks.remove(taskTick);
                }
            }
        }
    }

    /**
     * A Task to be used with {@link Scheduler#schedule(Task)}.
     */
    public static class Task {
        private final Runnable runnable;
        private final long delay;
        private final long period;
        private long lastRun;

        public Task(Runnable runnable, long delayTicks, long periodTicks) {
            this.runnable = runnable;
            this.delay = delayTicks;
            this.period = periodTicks;
        }

        public Task(Runnable runnable, long delayTicks) {
            this(runnable, delayTicks, 0);
        }

        public void run() {
            if (System.currentTimeMillis() - lastRun >= period) {
                runnable.run();
                lastRun = System.currentTimeMillis();
            }
        }
    }


}
