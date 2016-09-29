package com.google.common.util.concurrent;

import com.google.common.util.concurrent.Service.Listener;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by j69wang on 2016/9/9.
 */
public class Application {

    public static void main(String[] args) throws Exception {
        ExecutorService services = Executors.newFixedThreadPool(3);


        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future executorServiceFuture = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                System.out.println("Single thread executor is running ... ");
                Thread.currentThread().sleep(1000 * 3);
                throw new RuntimeException("runtime exception test");
//                return "Single thread executor done";
            }
        });

        services.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (executorServiceFuture.isDone()) {
                        System.out.println("Single thread executor is done  ... ");
                        try {
                            System.out.println("Future get a result : " + executorServiceFuture.get().toString());
                            executorServiceFuture.cancel(false);
                        } catch (InterruptedException e) {
                            System.out.println("Single thread executor has InterruptedException  ... ");
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            System.out.println("Single thread executor has ExecutionException  ... ");
                            e.printStackTrace();
                        } finally {
                            break;
                        }
                    }
                }
            }
        });


        //region Listenable Future

        ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(10));
        ListenableFuture listenableExecutorServiceFuture = listeningExecutorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                System.out.println("[Guava] Single thread executor is running ... ");
                Thread.currentThread().sleep(1000 * 3);
                throw new InterruptedException("[Guava] interrupt exception test");
//                return "Single thread executor done";
            }
        });

        // 1.add listener
        listenableExecutorServiceFuture.addListener(() -> {
            if (executorServiceFuture.isDone()) {
                System.out.println("[Guava] Single thread executor is done  ... ");
                try {
                    System.out.println("[Guava] Future get a result : " + executorServiceFuture.get().toString());
                    executorServiceFuture.cancel(false);
                } catch (InterruptedException e) {
                    System.out.println("[Guava] Single thread executor has InterruptedException  ... ");
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    System.out.println("[Guava] Single thread executor has ExecutionException  ... ");
                    e.printStackTrace();
                }
            }
        }, MoreExecutors.newDirectExecutorService());


        // 2. add callback
//        Futures.addCallback(listenableExecutorServiceFuture, new FutureCallback() {
//            @Override
//            public void onSuccess(Object result) {
//                System.out.println("[Guava] Future get a result : " + result.toString());
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                t.printStackTrace();
//                System.out.println("[Guava] Single thread executor is failed  ... ");
//
//            }
//        });
//        listenableExecutorServiceFuture.cancel(false);
        //endregion


        Service.Listener listener = new Service.Listener() {
            @Override
            public void starting() {
                System.out.println("starting1 ... ");
            }

            @Override
            public void running() {
                System.out.println("running1 ... ");
            }

            @Override
            public void stopping(Service.State from) {
                System.out.println("stopping1 ... " + from);
            }

            @Override
            public void terminated(Service.State from) {
                System.out.println("terminated1 ... " + from);
            }

            @Override
            public void failed(Service.State from, Throwable failure) {
                System.out.println("Failed1 : from " + from);
                failure.printStackTrace();
            }
        };


//        Thread.sleep(10000000);


//
//        CommonTaskService threadService = CommonTaskService.getInstance();
//
//        CommonTaskService.NBITask task = threadService.scheduleTask("first-job", new CommonScheduler() {
//            @Override
//            public void runOneIteration() throws Exception {
//                System.out.println("first-job  ... ");
//                Thread.sleep(20 * 1000);
////                throw new RuntimeException("test");
//            }
//
//            @Override
//            public CommonTaskService.NBISchedule bookSchedule() {
//                return CommonTaskService.NBISchedule.newFixedDelaySchedule(1, 2, TimeUnit.SECONDS);
////                return null;
//            }
//
//            @Override
//            public void terminate() {
//                System.out.println("first-job is terminate ... ");
//            }
//
//            @Override
//            public void startUp() {
//                System.out.println("first-job is start up ... ");
//            }
//
//            @Override
//            public CommonTaskService.NBISchedule bookExecutor() {
//                return null;
//            }
//
//        }, new CommonTaskCallback() {
//            @Override
//            public void onSuccess(Object result) {
//                System.out.println("first-job success ");
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                t.printStackTrace();
//                System.out.println("first-job failed ");
//            }
//        }, 1, CommonTaskService.TASK_TYPE.TIMER);
//
//        CommonTaskService.NBITask task2 = threadService.scheduleTask("second-job", new CommonScheduler() {
//            @Override
//            public void runOneIteration() throws Exception {
//                System.out.println("second-job  ... ");
//                Thread.sleep(5 * 1000);
////                throw new RuntimeException("test");
//            }
//
//            @Override
//            public CommonTaskService.NBISchedule bookSchedule() {
//                return CommonTaskService.NBISchedule.newFixedDelaySchedule(1, 2, TimeUnit.SECONDS);
////                return null;
//            }
//
//            @Override
//            public void terminate() {
//                System.out.println("second-job is terminate ... ");
//            }
//
//            @Override
//            public void startUp() {
//                System.out.println("second-job is start up ... ");
//            }
//
//            @Override
//            public CommonTaskService.NBISchedule bookExecutor() {
//                return null;
//            }
//
//        }, new CommonTaskCallback() {
//            @Override
//            public void onSuccess(Object result) {
//                System.out.println("first-job success ");
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                t.printStackTrace();
//                System.out.println("first-job failed ");
//            }
//        }, 1, CommonTaskService.TASK_TYPE.TIMER);
//
//        taskStatus(task);
//        threadService.runTask(task);
//        taskStatus(task2);
//        threadService.runTask(task2);
//        Thread.sleep(10000);
//        taskStatus(task);
//        taskStatus(task2);
////        List<Service.Listener> listeners = new ArrayList<>();
////        listeners.add(listener);
////        CommonTaskService.getInstance().configureListeners(listeners);
////
////        CommonTaskService.getInstance().submit(() -> System.out.println("running"), new FutureCallback() {
////            @Override
////            public void onSuccess(Object result) {
////                System.out.println("Future success");
////            }
////
////            @Override
////            public void onFailure(Throwable t) {
////                System.out.println("Future failed");
////            }
////        });
////
////        CommonTaskService service = new CommonTaskService();
////        service.addListener(new Service.Listener() {
////            @Override
////            public void starting() {
////                System.out.println("starting1 ... ");
////            }
////
////            @Override
////            public void running() {
////                System.out.println("running1 ... ");
////            }
////
////            @Override
////            public void stopping(Service.State from) {
////                System.out.println("stopping1 ... ");
////            }
////
////            @Override
////            public void terminated(Service.State from) {
////                System.out.println("terminated1 ... ");
////            }
////
////            @Override
////            public void failed(Service.State from, Throwable failure) {
////                System.out.println("Failed1 !");
////                failure.printStackTrace();
////            }
////        }, executorService);
//
////        service.startAsync().awaitRunning();
////        System.out.println("服务状态为:" + service.state());
//
//
//        try {
//            Thread.sleep(10 * 1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
////        threadService.cancelTask(task);
////        threadService.runTask(1);
//    }
//
//    private static void taskStatus(CommonTaskService.NBITask task) {
////        System.out.println("task is running : " + CommonTaskService.getInstance().taskIsRunning(task));
////        System.out.println("task is terminate: " + CommonTaskService.getInstance().taskIsTerminate(task));
////        System.out.println("task is failed : " + CommonTaskService.getInstance().taskIsFailed(task));
//    }
        //endregion
    }
}
