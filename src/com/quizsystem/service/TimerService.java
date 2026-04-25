package com.quizsystem.service;

import java.util.concurrent.*;

/**
 * TimerService — Per-question countdown timer.
 *
 * Uses ScheduledExecutorService for accurate, non-blocking timing.
 * When time expires, the onTimeUp callback fires automatically.
 *
 * Usage:
 *   TimerService timer = new TimerService();
 *   timer.start(15, () -> System.out.println("Time's up!"));
 *   // ... user answers ...
 *   long elapsed = timer.stop(); // returns elapsed time in ms
 */
public class TimerService {

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> timerTask;
    private long startTimeMs;
    private volatile boolean expired = false;

    /**
     * Starts a countdown timer.
     *
     * @param seconds  number of seconds for the countdown
     * @param onTimeUp callback executed when time expires
     */
    public void start(int seconds, Runnable onTimeUp) {
        stop(); // cancel any existing timer
        expired = false;
        startTimeMs = System.currentTimeMillis();

        if (seconds <= 0) return; // no time limit

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "QuizTimer");
            t.setDaemon(true); // won't prevent JVM shutdown
            return t;
        });

        timerTask = scheduler.schedule(() -> {
            expired = true;
            if (onTimeUp != null) onTimeUp.run();
        }, seconds, TimeUnit.SECONDS);
    }

    /**
     * Stops the timer and returns elapsed time in milliseconds.
     *
     * @return elapsed time in ms since start() was called
     */
    public long stop() {
        long elapsed = System.currentTimeMillis() - startTimeMs;
        if (timerTask != null) {
            timerTask.cancel(false);
            timerTask = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        return elapsed;
    }

    /** Returns true if the timer has expired. */
    public boolean isExpired() {
        return expired;
    }

    /** Returns elapsed time since start, without stopping. */
    public long getElapsedMs() {
        return System.currentTimeMillis() - startTimeMs;
    }

    /** Returns remaining seconds (approximate). */
    public int getRemainingSeconds(int totalSeconds) {
        long elapsed = getElapsedMs();
        int remaining = totalSeconds - (int)(elapsed / 1000);
        return Math.max(0, remaining);
    }
}
