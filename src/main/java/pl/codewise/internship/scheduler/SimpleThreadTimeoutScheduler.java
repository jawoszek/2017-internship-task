package pl.codewise.internship.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Simple implementation of {@link TimeoutScheduler} interface.
 * More details about implementation can be found in method's documentation.
 *
 * @author Kacper Jawoszek
 */
public class SimpleThreadTimeoutScheduler implements TimeoutScheduler {
    private Map<Long, SchedulerTask> taskMap = new HashMap<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private AtomicLong currentID = new AtomicLong(0);

    /**
     * Creates new timeout scheduler task with next ID in a row.
     * IDs are stored in long value, so no overflow mechanism is implemented as this is practically impossible to generate so many tasks.
     *
     * @param millis   time in milliseconds after which new schedule task will execute callback and terminate. Must not be negative.
     * @param callback {@link Runnable} which will be executed if there will be no stop before timeout. In case of null - nothing will be executed.
     * @return id of created task by which it can be referred to later.
     * @throws IllegalArgumentException in case millis is negative.
     */
    @Override
    public long start(long millis, Runnable callback) {
        if (millis < 0) throw new IllegalArgumentException("Time cannot be negative");
        long id = currentID.incrementAndGet();
        SchedulerTask newTask = new SchedulerTask(millis, callback, id);
        try {
            lock.writeLock().lock();
            taskMap.put(id, newTask);
        } finally {
            lock.writeLock().unlock();
        }
        newTask.start();
        return id;
    }

    /**
     * Stops scheduler task with given ID.
     * It is impossible that both this method returns true and task with given ID executes it's callback, so positive output specifically means that task was found and it won't (and it didn't) execute callback (possibly due to null callback).
     * Negative output means that there is no such task or given task already started executing callback.
     *
     * @param timerID ID of the task to stop.
     * @return boolean indicating if there was task to stop. Returns false for no task with given ID or if task already started executing callback. Returns true if task with given ID was successfully stopped by this or another thread.
     */
    @Override
    public boolean stop(long timerID) {
        SchedulerTask taskToStop;
        try {
            lock.readLock().lock();
            taskToStop = taskMap.get(timerID);
        } finally {
            lock.readLock().unlock();
        }
        return taskToStop != null && taskToStop.stopTask();
    }

    // designed only to be used from within task itself
    private void removeTask(long id) {
        try {
            lock.writeLock().lock();
            taskMap.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Inner class which is responsible for holding information and taking hold of timeout for single task.
     */
    private class SchedulerTask extends Thread {
        private long millis;
        private Runnable callback;
        private long timerID;
        // ugly flags and lock - but were necessary for quick implementation of crucial thread-safe features
        private boolean interrupted = false;
        private boolean beforeCallback = true;
        private Lock stopLock = new ReentrantLock();

        SchedulerTask(long millis, Runnable callback, long timerID) {
            this.millis = millis;
            this.callback = callback;
            this.timerID = timerID;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ignored) {
                interrupted = true;
            }
            if (!interrupted && callback != null) {
                try {
                    stopLock.lock();
                    if (!interrupted && callback != null) {
                        beforeCallback = false;
                        callback.run();
                    }
                } finally {
                    stopLock.unlock();
                }
            }
            SimpleThreadTimeoutScheduler.this.removeTask(timerID);
        }

        // stop task anyway and return true if before callback and lock is free
        boolean stopTask() {
            this.interrupt();
            try {
                return (stopLock.tryLock() && beforeCallback);
            } finally {
                interrupted = true;
                stopLock.unlock();
            }
        }
    }
}
