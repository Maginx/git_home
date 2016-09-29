package com.guava.service;

import com.google.common.util.concurrent.AbstractScheduledService;

/**
 * Created by j69wang on 2016/9/23.
 */
public abstract class CommonScheduleService extends AbstractScheduledService implements CommonService {
    private long timeout;
    private String serviceName;
    private long startTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public String serviceName() {
        return serviceName;
    }

    public CommonScheduleService(String serviceName, long timeout) {
        this.serviceName = serviceName;
        this.timeout = timeout;
    }

    public CommonScheduleService(CommonService service) {
        this(service.serviceName(), service.getTimeout());
    }

    @Override
    protected void startUp() {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    protected void shutDown() {
        this.startTime = 0;
    }

    @Override
    protected void runOneIteration() {
        this.runAction();
    }

    @Override
    protected Scheduler scheduler() {
        return this.NBIServiceScheduler();
    }

    public static void main(String[] args) throws InterruptedException {
//        CommonScheduleService s = new CommonScheduleService() {
//            @Override
//            public String serviceName() {
//                return "schedule-service";
//            }
//
//            @Override
//            protected void runOneIteration() throws Exception {
//
//                System.out.println("This is schedule service " +  this.isRunning());
//                throw new InterruptedException("test");
//            }
//
//            @Override
//            protected Scheduler scheduler() {
//                return Scheduler.newFixedDelaySchedule(1, 10, TimeUnit.SECONDS);
//            }
//        };
//        Service.Listener listener = new Service.Listener() {
//            @Override
//            public void starting() {
//
//                System.out.println(s.serviceName() + " starting ... " + s.state() + s.isRunning());
//            }
//
//            @Override
//            public void running() {
//                System.out.println(s.serviceName() + " running ... " + s.state() + s.isRunning());
//            }
//
//            @Override
//            public void stopping(Service.State from) {
//                System.out.println(s.serviceName() + " stopping ... ");
//            }
//
//            @Override
//            public void terminated(Service.State from) {
//                System.out.println(s.serviceName() + " terminated ... ");
//            }
//
//            @Override
//            public void failed(Service.State from, Throwable failure) {
//                System.out.println(s.serviceName() + " failed ... ");
//                failure.printStackTrace();
//            }
//        };
//        s.addListener(listener, MoreExecutors.directExecutor());
//        s.startAsync();
//        try {
//            if(s.state() == State.RUNNING) {
//                s.awaitTerminated(9, TimeUnit.SECONDS);
//            }
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }catch(Exception e1){
//            e1.printStackTrace();
//        }
    }
}
