package com.guava.service;


import com.google.common.util.concurrent.*;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by j69wang on 2016/9/23.
 */
public class ManagerService {
    public static ManagerService service = new ManagerService();
    public static ConcurrentHashMap<String, CommonService> services = new ConcurrentHashMap<>();
    public static ListeningScheduledExecutorService defaultScheduleExecutor;

    {
        monitorServices();
    }

    public void registerService(final CommonService nbiService) {
        if (services.containsKey(nbiService.serviceName())) {
            System.out.println("[ " + nbiService.serviceName() + " ] exists in service manager " + nbiService.state());
            return;
        }
        System.out.println("----------------------------------------------------");
        services.put(nbiService.serviceName(), nbiService);
        System.out.println(nbiService.serviceName() + " is registered on monitor.");
    }

    public boolean unRegisterService(CommonService scheduleService) {
        if (!services.containsKey(scheduleService.serviceName())) {
            return false;
        }
        services.remove(scheduleService.serviceName(), scheduleService);
        return true;
    }

    public void monitorServices() {
        defaultScheduleExecutor = initializeDefaultScheduleServicePool();
        ListenableFuture defaultExecutorFuture = defaultScheduleExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Iterator<String> keys = services.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    CommonService nbiService = services.get(key);
                    if (nbiService.state() == Service.State.TERMINATED || nbiService.state() == Service.State.FAILED) {
                        unRegisterService(nbiService);
                        continue;
                    }
                    if (nbiService.getTimeout() <= 0 || nbiService.getStartTime() <= 0 ||
                            nbiService.state() == Service.State.NEW ||
                            nbiService.state() == Service.State.STOPPING) {
                        continue;
                    }
                    if (nbiService.state() == Service.State.STARTING) {
                        nbiService.setStartTime(System.currentTimeMillis());
                        continue;
                    }
                    if (System.currentTimeMillis() - nbiService.getStartTime() >= nbiService.getTimeout()) {
                        nbiService.stopAsync();
                        unRegisterService(nbiService);
                        System.out.println("[" + key + "] running timeout for ");
                        System.out.println("start : " + nbiService.getStartTime());
                        System.out.println("current time : " + System.currentTimeMillis());
                        System.out.println("Time out : " + nbiService.getTimeout());
                    }
                }
            }
        }, 1, 500, TimeUnit.MILLISECONDS);

        monitorDefaultScheduleService(defaultExecutorFuture);
    }

    public static ManagerService getInstance() {
        return service;
    }

    private ManagerService() {
    }

    private void monitorDefaultScheduleService(ListenableFuture future) {
        Futures.addCallback(future, new FutureCallback() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onFailure(Throwable t) {
                monitorServices();
            }
        });
    }

    public CommonService restartService(CommonService service) {
        System.out.println("current services count : " + services.size());
        CommonService backupService = new CommonScheduleService(service) {
            @Override
            public void runAction() {
                System.out.println("Backup service is running ... ");
                service.runAction();
            }

            @Override
            public Scheduler NBIServiceScheduler() {
                return service.NBIServiceScheduler();
            }
        };
        System.out.println("They are same ? " + backupService.equals(service));
        System.out.println("Backup service status is  " + backupService.state());
        backupService.addListener(getListener(backupService), MoreExecutors.directExecutor());
        services.put(backupService.serviceName(), backupService);
        return backupService;
    }

    private static ListeningScheduledExecutorService initializeDefaultScheduleServicePool() {
        return MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(1));
    }

    public static void main(String[] args) {
        ManagerService service = ManagerService.getInstance();
        CommonScheduleService scheduleService = getService("schedule-service");
        Service.Listener listener = new Service.Listener() {
            @Override
            public void starting() {
                System.out.println(scheduleService.serviceName() + " starting ... " + scheduleService.state());
            }

            @Override
            public void running() {
                System.out.println(scheduleService.serviceName() + " running ... " + scheduleService.state());
            }

            @Override
            public void stopping(Service.State from) {
                System.out.println(scheduleService.serviceName() + " stopping ... ");
            }

            @Override
            public void terminated(Service.State from) {
                System.out.println(scheduleService.serviceName() + " terminated ... ");
//                System.out.println(scheduleService.failureCause());
            }

            @Override
            public void failed(Service.State from, Throwable failure) {
                System.out.println("current services count : " + services.size());
                System.out.println(scheduleService.serviceName() + " failed ... ");
                failure.printStackTrace();
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                CommonService backupService = service.restartService(scheduleService);
//                backupService.startAsync().awaitRunning();
//                scheduleService.failureCause();
            }
        };

        service.registerService(scheduleService);
        scheduleService.addListener(listener, MoreExecutors.directExecutor());
        scheduleService.startAsync().awaitRunning();

    }

    public static CommonScheduleService getService(String serviceName) {
        return new CommonScheduleService(serviceName, 1000 * 4) {
            @Override
            public void runAction() {
                try {
                    Thread.currentThread().sleep(1000 * 5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("[ " + serviceName + " ] is working ... ");
//                throw new RuntimeException("interrupt exception");
            }

            @Override
            public Scheduler NBIServiceScheduler() {
                return Scheduler.newFixedDelaySchedule(1, 10, TimeUnit.SECONDS);
            }
        };
    }

    public static Service.Listener getListener(CommonService service) {
        return new Service.Listener() {

            @Override
            public void starting() {
                System.out.println(service.serviceName() + " starting ... " + service.state());
            }

            @Override
            public void running() {
                System.out.println(service.serviceName() + " running ... " + service.state());
            }

            @Override
            public void stopping(Service.State from) {
                System.out.println(service.serviceName() + " stopping ... ");
            }

            @Override
            public void terminated(Service.State from) {
                System.out.println(service.serviceName() + " terminated ... ");
//                System.out.println(service.failureCause().fillInStackTrace());
            }

            @Override
            public void failed(Service.State from, Throwable failure) {
                System.out.println(service.serviceName() + " failed ... ");
                failure.printStackTrace();
            }
        };
    }
}
