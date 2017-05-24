package pl.codewise.internship.scheduler;

/**
 * The {@link TimeoutScheduler} interface should be implemented by class whose purpose is to execute a given callback after specified amount of time passed and no stopped occurred.
 * It is not designed to inform user in any way about current situation except by callback or {@link #stop(long) stop} method.
 * Implementations can differ in some details (for example meaning of {@link #stop(long) stop} return value), so choice of specific TimeoutScheduler should be deeply considered.
 *
 * @author Kacper
 */
public interface TimeoutScheduler {

    /**
     * Starts new task with given timeout time and callback.
     *
     * @param millis timeout in milliseconds.
     * @param callback {@link Runnable} which will be executed if specified amount of time will expire and there will be no stops.
     * @return ID of newly created task.
     */
    long start(long millis, Runnable callback);

    /**
     * Tries to stop a task with given ID.
     *
     * @param timerID ID of task to stop.
     * @return boolean value which indicates if stopping action was successful - exact meaning differs between implementations.
     */
    boolean stop(long timerID);
}
