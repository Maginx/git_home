package com.guava.service;


import com.google.common.util.concurrent.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by j69wang on 2016/9/23.
 */
public class ManagerService {
    public static ManagerService service = new ManagerService();
    public static ListeningScheduledExecutorService defaultScheduleExecutor;
    private static Map<String, CustomTimeoutService> services = new ConcurrentHashMap<>();

    {
        monitorServices();
    }

    public CustomTimeoutService restartService(CustomTimeoutService service) {
        System.out.println("current services count : " + services.size());
        CustomTimeoutService backupService = getBackupService(service);
        System.out.println("They are same ? " + backupService.equals(service));
        System.out.println("Backup service status is  " + backupService.state());
        services.putIfAbsent(backupService.serviceName(), backupService);
        return backupService;
    }

    public void registerService(final CustomTimeoutService nbiService) {
        System.out.println("----------------------------------------------------");
        services.putIfAbsent(nbiService.serviceName(), nbiService);
        System.out.println(nbiService.serviceName() + " is registered on monitor.");
    }

    public boolean unRegisterService(CustomTimeoutService scheduleService) {
        if (!services.containsKey(scheduleService.serviceName())) {
            return false;
        }
        return services.remove(scheduleService.serviceName(), scheduleService);
    }

    public static ManagerService getInstance() {
        return service;
    }

    private ManagerService() {
    }

    private void monitorServices() {
        defaultScheduleExecutor = initializeDefaultScheduleServicePool();
        ListenableFuture defaultExecutorFuture = defaultScheduleExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                StringBuilder logInformation = new StringBuilder();
                Iterator<String> keys = services.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    CustomTimeoutService nbiService = services.get(key);
                    if (IsServiceExpired(nbiService)) continue;
                    if (HasTimeoutConfiguration(nbiService)) continue;
                    if (IsServiceStarting(nbiService)) continue;
                    checkServiceTimeout(logInformation, nbiService);
                }
                System.out.println(logInformation.toString());
            }
        }, 1, 500, TimeUnit.MILLISECONDS);

        monitorDefaultScheduleService(defaultExecutorFuture);
    }

    private boolean IsServiceStarting(CustomTimeoutService nbiService) {
        if (nbiService.state() == Service.State.STARTING) {
            nbiService.setStartTime(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    private boolean HasTimeoutConfiguration(CustomTimeoutService nbiService) {
        if (nbiService.getTimeout() <= 0 || nbiService.getStartTime() <= 0 ||
                nbiService.state() == Service.State.NEW ||
                nbiService.state() == Service.State.STOPPING) {
            return true;
        }
        return false;
    }

    private boolean IsServiceExpired(CustomTimeoutService nbiService) {
        if (nbiService.state() == Service.State.TERMINATED || nbiService.state() == Service.State.FAILED) {
            unRegisterService(nbiService);
            return true;
        }
        return false;
    }

    private void checkServiceTimeout(StringBuilder logInformation, CustomTimeoutService nbiService) {
        if (System.currentTimeMillis() - nbiService.getStartTime() >= nbiService.getTimeout()) {
            nbiService.stopAsync().awaitTerminated();
            unRegisterService(nbiService);
            logInformation.append("[" + nbiService.serviceName() + "] running timeout for ");
            logInformation.append("start : " + nbiService.getStartTime());
            logInformation.append("current time : " + System.currentTimeMillis());
            logInformation.append("Time out : " + nbiService.getTimeout());
        }
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

    private CustomScheduleService getBackupService(final CustomTimeoutService service) {
        CustomScheduleService backupScheduleService = new CustomScheduleService(service) {
            @Override
            public void runAction() {
                System.out.println("Backup service is running ... ");
                service.runAction();
            }

            @Override
            public Scheduler CustomServiceScheduler() {
                return service.CustomServiceScheduler();
            }
        };

        backupScheduleService.addListener(getBackupServiceListener(service), MoreExecutors.directExecutor());
        return backupScheduleService;
    }

    private Service.Listener getBackupServiceListener(final CustomTimeoutService service) {
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
            }

            @Override
            public void failed(Service.State from, Throwable failure) {
                System.out.println(service.serviceName() + " failed ... ");
                failure.printStackTrace();
            }
        };
    }

    private static ListeningScheduledExecutorService initializeDefaultScheduleServicePool() {
        return MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(1));
    }

    public static void main(String[] args) {


        Map myMap = new HashMap<String, String>();
        myMap.put("1", "1");
        myMap.put("2", "1");
        myMap.put("3", "1");
        Iterator<String> it = myMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            myMap.put("new" + key, "new3");
            myMap.remove("1");
            myMap.remove("2");
//            myMap.remove("3");
        }
        System.out.println("HashMap after iterator: " + myMap);

        ManagerService service = ManagerService.getInstance();
        CustomScheduleService scheduleService = getService("schedule-service");
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
//                CustomTimeoutService backupService = service.restartService(scheduleService);
//                backupService.startAsync().awaitRunning();
//                scheduleService.failureCause();
            }
        };

        service.registerService(scheduleService);
        scheduleService.addListener(listener, MoreExecutors.directExecutor());
        scheduleService.startAsync().awaitRunning();

    }

    public static CustomScheduleService getService(String serviceName) {
        return new CustomScheduleService(serviceName, 1000 * 4) {
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
            public Scheduler CustomServiceScheduler() {
                return Scheduler.newFixedDelaySchedule(1, 10, TimeUnit.SECONDS);
            }
        };
    }
}
