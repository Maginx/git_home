package com.google.common.util.concurrent;

import com.google.common.base.Supplier;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Core service and is a singleton instance
 * 1. create task
 * 2. cancel task
 * 3. restart task
 * <p>
 * Created :j69wang <jeremy.wang@nokia.com>
 * Date : 2016/9/13
 */
public class CommonTaskService {

    /**
     * This is max task count
     */
    private final static AtomicInteger poolSize = new AtomicInteger(20);
    private final static ConcurrentHashMap<String, NBITask> taskPool = new ConcurrentHashMap<>();
    private final static CommonTaskService nbiService = new CommonTaskService();
    private final NBITaskDelegate delegate = new NBITaskDelegate();
    /**
     * This reentrant lock for task run function
     */
    private final ReentrantLock lock = new ReentrantLock();

    {
//        delegate.startTimeoutChecking();
    }

    /**
     * create a task, but if the tasks count reaches the pool size, will reject and throw null
     *
     * @param taskName  configure task name
     * @param scheduler configure task's scheduler, you can implement CommonScheduler interface
     * @param callBack  task callback function, will be triggered when task success and failed(including runtime exception)
     * @param timeout   task running getTimeout, when task running time is longer than getTimeout, the daemon task will help cancel this task
     * @return return a new task, you can use it to do one action that you want to
     */
    public NBITask scheduleTask(String taskName, CommonScheduler scheduler, CommonTaskCallback callBack, long timeout, TASK_TYPE taskType) {
        if (taskPool.size() > poolSize.get() && this.containsTask(taskName)) {
//            AbstractScheduledService
            return null;
        }
        if (scheduler == null || scheduler.bookExecutor() == null && scheduler.bookSchedule() == null) {
            return null;
        }
        return this.delegate.createTask(taskName, scheduler, callBack, timeout, taskType);
    }

    /**
     * Task running
     *
     * @param task the task that scheduleTask return to you
     */
    public void runTask(NBITask task) {
        if (task == null) {
            return;
        }

        this.delegate.startTask(task);
    }

    /**
     * Return the singleton instance
     *
     * @return
     */
    public static CommonTaskService getInstance() {
        return nbiService;
    }

    /**
     * cancel a task, this task is you scheduleTask return to you
     *
     * @param task
     */
    public void cancelTask(NBITask task) {
        if (task == null) {
            return;
        }

        System.out.println("Task count : " + taskPool.size());
        this.delegate.stopTask(task);
    }

    public boolean taskIsRunning(NBITask task) {
        return task.isRunning();
    }

    public boolean taskIsTerminate(NBITask task) {
        return task.isTerminated();
    }

    public boolean taskIsFailed(NBITask task) {
        return task.isFailed();
    }

    private CommonTaskService() {
    }

    private boolean containsTask(String taskName) {
        for (int i = 0; i < taskPool.size(); i++) {
            if (taskPool.get(i).taskName == taskName) {
                return true;
            }
        }
        return false;
    }

    /**
     * NBI Schedule is response for running task on which type, there is two types:
     * 1. schedule is a timer task
     * 2. execute is a run-once task
     * You can implement CommonScheduler interface to define the task action
     */
    public abstract static class NBISchedule {

        /**
         * create a fixed delay schedule
         *
         * @param initialDelay initial this task's delay time
         * @param delay        delay time
         * @param unit         time unit define the time type: seconds,millseconde...
         * @return
         */
        public static NBISchedule newFixedDelaySchedule(final long initialDelay, final long delay, final TimeUnit unit) {
            checkNotNull(unit);
            checkArgument(delay > 0, "delay must be > 0, found %s", delay);
            return new NBISchedule() {
                @Override
                public ListenableFuture<?> schedule(AbstractService service, ScheduledExecutorService executor,
                                                    Runnable task) {
                    return MoreExecutors.listeningDecorator(executor).scheduleWithFixedDelay(task, initialDelay, delay, unit);
                }

                @Override
                ListenableFuture<?> execute(ScheduledExecutorService executor, Runnable runnable) {
                    return null;
                }
            };
        }

        /**
         * create a run-once schedule
         *
         * @return
         */
        public static NBISchedule execute() {
            return new NBISchedule() {
                @Override
                ListenableFuture<?> schedule(AbstractService service, ScheduledExecutorService executor, Runnable runnable) {
                    return null;
                }

                @Override
                ListenableFuture<?> execute(ScheduledExecutorService executor, Runnable runnable) {
                    return MoreExecutors.listeningDecorator(executor).submit(runnable);
                }
            };
        }

        /**
         * create a fixed rate schedule
         *
         * @param initialDelay initial this task's delay time
         * @param delay        delay time
         * @param unit         time unit define the time type: seconds,millseconde...
         * @return
         */
        public static NBISchedule newFixedRateSchedule(final long initialDelay, final long period, final TimeUnit unit) {
            checkNotNull(unit);
            checkArgument(period > 0, "period must be > 0, found %s", period);
            return new NBISchedule() {
                @Override
                public ListenableFuture<?> schedule(AbstractService service, ScheduledExecutorService executor,
                                                    Runnable task) {
                    return MoreExecutors.listeningDecorator(executor).scheduleAtFixedRate(task, initialDelay, period, unit);
                }

                @Override
                ListenableFuture<?> execute(ScheduledExecutorService executor, Runnable runnable) {
                    return MoreExecutors.listeningDecorator(executor).submit(runnable);
                }
            };
        }

        /**
         * timer scheduler
         *
         * @param service
         * @param executor
         * @param runnable
         * @return
         */
        abstract ListenableFuture<?> schedule(AbstractService service, ScheduledExecutorService executor, Runnable runnable);

        /**
         * run-once scheduler
         *
         * @param executor
         * @param runnable
         * @return
         */
        abstract ListenableFuture<?> execute(ScheduledExecutorService executor, Runnable runnable);

        private NBISchedule() {
        }
    }

    /**
     * Two task type
     * 1. Timer
     * 2. run-once
     */
    enum TASK_TYPE {
        TIMER,
        COMMON
    }

    /**
     * This is a delegate class and take charge of dispatching tasks and trace task state
     */
    private final class NBITaskDelegate extends AbstractService {

        private ScheduledExecutorService executorService;

        private void addOnMonitor(NBITask task) {
            Futures.addCallback(task.listenableFuture, task.futureCallback);
        }

        private NBITaskDelegate() {

        }

        /**
         * start a daemon task to monitor the other tasks getTimeout
         */
        private void startTimeoutChecking() {
            NBITask task;
            TimeoutScheduler timeout = new TimeoutScheduler();

            task = scheduleTask("getTimeout thread",
                    timeout,
                    new CommonTaskCallback() {
                        @Override
                        public void onSuccess(Object result) {

                        }

                        @Override
                        public void onFailure(Throwable t) {
                            System.out.println("getTimeout thread is failed");
                            t.printStackTrace();
                        }
                    }, 0, TASK_TYPE.TIMER);
            runTask(task);
        }

        /**
         * create a task
         *
         * @param taskName
         * @param scheduler
         * @param callBack
         * @param timeout
         * @return
         */
        private NBITask createTask(String taskName, CommonScheduler scheduler, CommonTaskCallback callBack, long timeout, TASK_TYPE taskType) {
            NBITask task = new NBITask();
            task.taskName = taskName;
            task.futureCallback = callBack;
            task.nbiAction = scheduler;
            task.timeout = timeout;
            task.taskType = taskType;
            taskPool.putIfAbsent(taskName, task);
            return task;
        }

        /**
         * start a task
         *
         * @param task
         */
        private synchronized void startTask(NBITask task) {
            executorService = MoreExecutors.renamingDecorator(createExecutorService(task), new Supplier<String>() {
                @Override
                public String get() {
                    return task.taskName;
                }
            });

            executorService.submit(() -> {
                try {
//                    AbstractScheduledService
                    if (task.taskType == TASK_TYPE.COMMON) {
                        task.listenableFuture = task.nbiAction.bookExecutor().execute(executorService, task);
                    }
                    if (task.taskType == TASK_TYPE.TIMER) {
                        task.listenableFuture = task.nbiAction.bookSchedule().schedule(delegate, executorService, task);
                    }
                    this.addOnMonitor(task);
                } catch (Throwable t) {
                    if (task.cancel(false)) {
                        taskPool.remove(task.taskName, task);
                    }
                }
            });
        }

        /**
         * stop a task
         *
         * @param task
         */
        private synchronized void stopTask(NBITask task) {
            if (task.cancel(false)) {
                taskPool.remove(task);
            }
            System.out.println("Task count : " + taskPool.size());
        }

        /**
         * initialize the service thread count
         * thread count can be configure at xml file
         * need to fix the thread count , now is 15
         *
         * @return
         */
        private ScheduledExecutorService createExecutorService(NBITask task) {
            executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return MoreExecutors.newThread("thread-" + task.taskName, r);
                }
            });
            return MoreExecutors.renamingDecorator(executorService, () -> task.taskName + "-service");
        }

        @Override
        protected final void doStart() {
            System.out.println("NBIThreadPool start");
        }

        @Override
        protected final void doStop() {
            System.out.println("NBIThreadPool stopped");
        }
    }

    /**
     * Running the tasks
     */
    public final class NBITask implements Runnable {
        /**
         * trace task status
         */
        private volatile Service.State state = Service.State.NEW;
        String taskName;
        long startTime;
        long timeout;
        /**
         * task call back function
         */
        FutureCallback futureCallback;
        /**
         * help to handle task
         */
        ListenableFuture listenableFuture;
        /**
         * define the task run type
         */
        CommonScheduler nbiAction;
        TASK_TYPE taskType;

        private NBITask() {
        }

        @Override
        public void run() {
//            lock.lock();
            try {
                if (this.listenableFuture != null && this.listenableFuture.isCancelled()) {
                    return;
                }
                this.nbiAction.startUp();
                this.startTime = System.currentTimeMillis();
                this.state = Service.State.RUNNING;
                this.nbiAction.runOneIteration();
                this.state = Service.State.TERMINATED;
                this.nbiAction.terminate();
            } catch (Throwable t) {
                try {
                    this.cancel(false);
                } catch (Exception ignored) {
                    System.out.println("Error while attempting to shut down the task after failure.");
                }
            } finally {
//                lock.unlock();
            }
        }

        private synchronized boolean isRunning() {
            return this.state == Service.State.RUNNING;
        }

        private synchronized boolean isTerminated() {
            return this.state == Service.State.TERMINATED;
        }

        private synchronized boolean isFailed() {
            return this.state == Service.State.FAILED;
        }

        private synchronized boolean cancel(boolean isForce) {
            if (isFailed()) {
                return true;
            }
            boolean result = false;
            lock.lock();
            try {
                this.state = Service.State.FAILED;
                if (isForce) {
                    result = this.listenableFuture.cancel(true);
                } else {
                    result = this.listenableFuture.cancel(false);
                }
                System.out.println("task is cancel");
            } catch (Exception ignored) {
                System.out.println("Error while attempting to shut down the task after failure.");
            } finally {
                lock.unlock();
            }
            return result;
        }
    }

    private class TimeoutScheduler implements CommonScheduler {
        @Override
        public void runOneIteration() {
            for (int index = 0; index < taskPool.size(); index++) {
                CommonTaskService.NBITask task = taskPool.get(index);
                if (task.timeout <= 0) {
                    continue;
                }
                if (System.currentTimeMillis() - task.startTime > task.timeout) {
                    CommonTaskService.getInstance().cancelTask(task);
                }
            }
        }

        @Override
        public CommonTaskService.NBISchedule bookSchedule() {
            return CommonTaskService.NBISchedule.newFixedDelaySchedule(500, 500, TimeUnit.MILLISECONDS);
        }

        @Override
        public void terminate() {

        }

        @Override
        public void startUp() {

        }

        @Override
        public NBISchedule bookExecutor() {
            return null;
        }
    }
}
