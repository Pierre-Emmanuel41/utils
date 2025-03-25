package fr.pederobien.utils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SimpleTimer extends Timer {

	/**
	 * Creates a new timer. The associated thread does <i>not</i>
	 * {@linkplain Thread#setDaemon run as a daemon}.
	 */
	public SimpleTimer() {
		super();
	}

	/**
	 * Creates a new timer whose associated thread may be specified to
	 * {@linkplain Thread#setDaemon run as a daemon}. A daemon thread is called for
	 * if the timer will be used to schedule repeating "maintenance activities",
	 * which must be performed as long as the application is running, but should not
	 * prolong the lifetime of the application.
	 *
	 * @param isDaemon true if the associated thread should run as a daemon.
	 */
	public SimpleTimer(boolean isDaemon) {
		super(isDaemon);
	}

	/**
	 * Creates a new timer whose associated thread has the specified name. The
	 * associated thread does <i>not</i> {@linkplain Thread#setDaemon run as a
	 * daemon}.
	 *
	 * @param name the name of the associated thread
	 * @throws NullPointerException if {@code name} is null
	 * @since 1.5
	 */
	public SimpleTimer(String name) {
		super(name);
	}

	/**
	 * Creates a new timer whose associated thread has the specified name, and may
	 * be specified to {@linkplain Thread#setDaemon run as a daemon}.
	 *
	 * @param name     the name of the associated thread
	 * @param isDaemon true if the associated thread should run as a daemon
	 * @throws NullPointerException if {@code name} is null
	 * @since 1.5
	 */
	public SimpleTimer(String name, boolean isDaemon) {
		super(name, isDaemon);
	}

	/**
	 * Schedules a task associated to the given runnable for execution after the
	 * specified delay.
	 *
	 * @param runnable The code to execute.
	 * @param delay    delay in milliseconds before task is to be executed.
	 * 
	 * @return The created {@link TimerTask}.
	 * 
	 * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
	 *                                  <tt>delay + System.currentTimeMillis()</tt>
	 *                                  is negative.
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} is null
	 */
	public TimerTask schedule(Runnable runnable, long delay) {
		TimerTask task = createTask(runnable);
		super.schedule(createTask(runnable), delay);
		return task;
	}

	/**
	 * Schedules a task associated to the given runnable for execution at the
	 * specified time. If the time is in the past, the task is scheduled for
	 * immediate execution.
	 *
	 * @param runnable The code to execute.
	 * @param time     time at which task is to be executed.
	 * 
	 * @return The created {@link TimerTask}.
	 * 
	 * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} or {@code time} is null
	 */
	public TimerTask schedule(Runnable runnable, Date time) {
		TimerTask task = createTask(runnable);
		super.schedule(task, time);
		return task;
	}

	/**
	 * Schedules a task associated to the given runnable for repeated <i>fixed-delay
	 * execution</i>, beginning after the specified delay. Subsequent executions
	 * take place at approximately regular intervals separated by the specified
	 * period.
	 *
	 * <p>
	 * In fixed-delay execution, each execution is scheduled relative to the actual
	 * execution time of the previous execution. If an execution is delayed for any
	 * reason (such as garbage collection or other background activity), subsequent
	 * executions will be delayed as well. In the long run, the frequency of
	 * execution will generally be slightly lower than the reciprocal of the
	 * specified period (assuming the system clock underlying
	 * <tt>Object.wait(long)</tt> is accurate).
	 *
	 * <p>
	 * Fixed-delay execution is appropriate for recurring activities that require
	 * "smoothness." In other words, it is appropriate for activities where it is
	 * more important to keep the frequency accurate in the short run than in the
	 * long run. This includes most animation tasks, such as blinking a cursor at
	 * regular intervals. It also includes tasks wherein regular activity is
	 * performed in response to human input, such as automatically repeating a
	 * character as long as a key is held down.
	 *
	 * @param runnable The code to execute.
	 * @param delay    delay in milliseconds before task is to be executed.
	 * @param period   time in milliseconds between successive task executions.
	 * 
	 * @return The created {@link TimerTask}.
	 * 
	 * @throws IllegalArgumentException if {@code delay < 0}, or
	 *                                  {@code delay + System.currentTimeMillis() < 0},
	 *                                  or {@code period <= 0}
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} is null
	 */
	public TimerTask schedule(Runnable runnable, long delay, long period) {
		TimerTask task = createTask(runnable);
		super.schedule(task, delay, period);
		return task;
	}

	/**
	 * Schedules a task associated to the given runnable for repeated <i>fixed-delay
	 * execution</i>, beginning at the specified time. Subsequent executions take
	 * place at approximately regular intervals, separated by the specified period.
	 *
	 * <p>
	 * In fixed-delay execution, each execution is scheduled relative to the actual
	 * execution time of the previous execution. If an execution is delayed for any
	 * reason (such as garbage collection or other background activity), subsequent
	 * executions will be delayed as well. In the long run, the frequency of
	 * execution will generally be slightly lower than the reciprocal of the
	 * specified period (assuming the system clock underlying
	 * <tt>Object.wait(long)</tt> is accurate). As a consequence of the above, if
	 * the scheduled first time is in the past, it is scheduled for immediate
	 * execution.
	 *
	 * <p>
	 * Fixed-delay execution is appropriate for recurring activities that require
	 * "smoothness." In other words, it is appropriate for activities where it is
	 * more important to keep the frequency accurate in the short run than in the
	 * long run. This includes most animation tasks, such as blinking a cursor at
	 * regular intervals. It also includes tasks wherein regular activity is
	 * performed in response to human input, such as automatically repeating a
	 * character as long as a key is held down.
	 *
	 * @param runnable  The code to execute.
	 * @param firstTime First time at which task is to be executed.
	 * @param period    time in milliseconds between successive task executions.
	 * 
	 * @return The created {@link TimerTask}.
	 * 
	 * @throws IllegalArgumentException if {@code firstTime.getTime() < 0}, or
	 *                                  {@code period <= 0}
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} or {@code firstTime} is null
	 */
	public TimerTask schedule(Runnable runnable, Date firstTime, long period) {
		TimerTask task = createTask(runnable);
		super.schedule(task, firstTime, period);
		return task;
	}

	/**
	 * Schedules a task associated to the given runnable for repeated <i>fixed-rate
	 * execution</i>, beginning after the specified delay. Subsequent executions
	 * take place at approximately regular intervals, separated by the specified
	 * period.
	 *
	 * <p>
	 * In fixed-rate execution, each execution is scheduled relative to the
	 * scheduled execution time of the initial execution. If an execution is delayed
	 * for any reason (such as garbage collection or other background activity), two
	 * or more executions will occur in rapid succession to "catch up." In the long
	 * run, the frequency of execution will be exactly the reciprocal of the
	 * specified period (assuming the system clock underlying
	 * <tt>Object.wait(long)</tt> is accurate).
	 *
	 * <p>
	 * Fixed-rate execution is appropriate for recurring activities that are
	 * sensitive to <i>absolute</i> time, such as ringing a chime every hour on the
	 * hour, or running scheduled maintenance every day at a particular time. It is
	 * also appropriate for recurring activities where the total time to perform a
	 * fixed number of executions is important, such as a countdown timer that ticks
	 * once every second for ten seconds. Finally, fixed-rate execution is
	 * appropriate for scheduling multiple repeating timer tasks that must remain
	 * synchronized with respect to one another.
	 *
	 * @param runnable The code to execute.
	 * @param delay    delay in milliseconds before task is to be executed.
	 * @param period   time in milliseconds between successive task executions.
	 * 
	 * @return The created {@link TimerTask}.
	 * 
	 * @throws IllegalArgumentException if {@code delay < 0}, or
	 *                                  {@code delay + System.currentTimeMillis() < 0},
	 *                                  or {@code period <= 0}
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} is null
	 */
	public TimerTask scheduleAtFixedRate(Runnable runnable, long delay, long period) {
		TimerTask task = createTask(runnable);
		super.scheduleAtFixedRate(task, delay, period);
		return task;
	}

	/**
	 * Schedules a task associated to the given runnable for repeated <i>fixed-rate
	 * execution</i>, beginning at the specified time. Subsequent executions take
	 * place at approximately regular intervals, separated by the specified period.
	 *
	 * <p>
	 * In fixed-rate execution, each execution is scheduled relative to the
	 * scheduled execution time of the initial execution. If an execution is delayed
	 * for any reason (such as garbage collection or other background activity), two
	 * or more executions will occur in rapid succession to "catch up." In the long
	 * run, the frequency of execution will be exactly the reciprocal of the
	 * specified period (assuming the system clock underlying
	 * <tt>Object.wait(long)</tt> is accurate). As a consequence of the above, if
	 * the scheduled first time is in the past, then any "missed" executions will be
	 * scheduled for immediate "catch up" execution.
	 *
	 * <p>
	 * Fixed-rate execution is appropriate for recurring activities that are
	 * sensitive to <i>absolute</i> time, such as ringing a chime every hour on the
	 * hour, or running scheduled maintenance every day at a particular time. It is
	 * also appropriate for recurring activities where the total time to perform a
	 * fixed number of executions is important, such as a countdown timer that ticks
	 * once every second for ten seconds. Finally, fixed-rate execution is
	 * appropriate for scheduling multiple repeating timer tasks that must remain
	 * synchronized with respect to one another.
	 *
	 * @param runnable  The code to execute.
	 * @param firstTime First time at which task is to be executed.
	 * @param period    time in milliseconds between successive task executions.
	 * 
	 * @return The created {@link TimerTask}.
	 * 
	 * @throws IllegalArgumentException if {@code firstTime.getTime() < 0} or
	 *                                  {@code period <= 0}
	 * @throws IllegalStateException    if task was already scheduled or cancelled,
	 *                                  timer was cancelled, or timer thread
	 *                                  terminated.
	 * @throws NullPointerException     if {@code task} or {@code firstTime} is null
	 */
	public TimerTask scheduleAtFixedRate(Runnable runnable, Date firstTime, long period) {
		TimerTask task = createTask(runnable);
		super.scheduleAtFixedRate(task, firstTime, period);
		return task;
	}

	private TimerTask createTask(Runnable runnable) {
		return new TimerTask() {

			@Override
			public void run() {
				runnable.run();
			}
		};
	}
}
