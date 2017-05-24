package pl.codewise.internship.scheduler;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * I'm sorry for ugly tests with usage of real time, but I don't want to pollute interface or class with logic to create controllable time environment.
 * I guess if we want this implementation to work as expected in real time, then times given in tests below should be expected to work.
 *
 * @author Kacper
 */
@SuppressWarnings("Duplicates")
public class SimpleThreadTimeoutSchedulerTest {
    private TimeoutScheduler timeoutScheduler;

    @Before
    public void setUp() {
        timeoutScheduler = new SimpleThreadTimeoutScheduler();
    }

    @Test
    public void simpleSingleNotIncrementedSchedulerTest() throws Exception {
        final Counter counter = new Counter();
        timeoutScheduler.start(1000, counter::increment);
        Thread.sleep(100);
        assertEquals(counter.getValue(), 0);
    }

    @Test
    public void simpleSingleIncrementedSchedulerTest() throws Exception {
        final Counter counter = new Counter();
        timeoutScheduler.start(50, counter::increment);
        Thread.sleep(500);
        assertEquals(counter.getValue(), 1);
    }

    @Test
    public void simpleSingleNullCallbackSchedulerTest() throws Exception {
        timeoutScheduler.start(50, null);
        Thread.sleep(500);
    }

    @Test
    public void simpleSingleStopSchedulerTest() throws Exception {
        final Counter counter = new Counter();
        long id = timeoutScheduler.start(200, counter::increment);
        Thread.sleep(10);
        timeoutScheduler.stop(id);
        Thread.sleep(400);
        assertEquals(counter.getValue(), 0);
    }

    @Test
    public void simpleSingleStopAfterSchedulerTest() throws Exception {
        final Counter counter = new Counter();
        long id = timeoutScheduler.start(50, counter::increment);
        Thread.sleep(200);
        timeoutScheduler.stop(id);
        assertEquals(counter.getValue(), 1);
    }

    @Test
    public void simpleSingleStopOfNoexistentTaskSchedulerTest() throws Exception {
        assertEquals(timeoutScheduler.stop(1), false);
    }

    @Test
    public void simpleMultipleLongTasksToStopSchedulerTest() throws Exception {
        final Counter counter = new Counter();
        long id1 = timeoutScheduler.start(1000, counter::increment);
        long id2 = timeoutScheduler.start(1000, counter::increment);
        long id3 = timeoutScheduler.start(1000, counter::increment);
        long id4 = timeoutScheduler.start(1000, counter::increment);
        Thread.sleep(50);
        assertEquals(timeoutScheduler.stop(id1), true);
        assertEquals(timeoutScheduler.stop(id2), true);
        assertEquals(timeoutScheduler.stop(id3), true);
        assertEquals(timeoutScheduler.stop(id4), true);
    }

    @Test
    public void simpleMultipleDifferentTasksSchedulerTest() throws Exception {
        final Counter counter = new Counter();
        long id1 = timeoutScheduler.start(500, counter::increment);
        long id2 = timeoutScheduler.start(50, counter::increment);
        long id3 = timeoutScheduler.start(200, counter::increment);
        long id4 = timeoutScheduler.start(500, counter::increment);
        long id5 = timeoutScheduler.start(10, counter::increment);
        assertEquals(timeoutScheduler.stop(id3), true);
        Thread.sleep(200);
        assertEquals(timeoutScheduler.stop(id2), false);
        assertEquals(timeoutScheduler.stop(id4), true);
        assertEquals(counter.getValue(), 2);
        Thread.sleep(500);
        assertEquals(counter.getValue(), 3);
    }

    @Test
    public void manyDifferentTaskTimeSpecificSchedulerTest() throws Exception {
        long id1;
        long id2;
        long id3;
        final Counter counter = new Counter();
        timeoutScheduler.start(500, counter::increment);
        id2 = timeoutScheduler.start(200, counter::increment);
        timeoutScheduler.start(100, counter::increment);
        Thread.sleep(50);
        timeoutScheduler.stop(id2);
        assertEquals(counter.getValue(),0);
        timeoutScheduler.start(20, counter::increment);
        id2 = timeoutScheduler.start(20, counter::increment);
        timeoutScheduler.start(1000, counter::increment);
        Thread.sleep(100);
        timeoutScheduler.stop(id2);
        assertEquals(counter.getValue(),3);
        Thread.sleep(1000);
        assertEquals(counter.getValue(),5);
    }

    @Test
    public void simpleTaskWithZeroTimeSchedulerTest() throws Exception {
        final Counter counter = new Counter();
        timeoutScheduler.start(0, counter::increment);
        Thread.sleep(20);
        assertEquals(counter.getValue(),1);
    }


    private static class Counter {
        AtomicInteger atomicInteger = new AtomicInteger(0);

        void increment() {
            atomicInteger.incrementAndGet();
        }

        int getValue() {
            return atomicInteger.get();
        }
    }
}