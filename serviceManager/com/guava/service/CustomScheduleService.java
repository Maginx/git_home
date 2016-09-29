package com.guava.service;

import com.google.common.util.concurrent.AbstractScheduledService;

/**
 * Created by j69wang on 2016/9/23.
 */
public abstract class CustomScheduleService extends AbstractScheduledService implements CustomTimeoutService {
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

    public CustomScheduleService(String serviceName, long timeout) {
        this.serviceName = serviceName;
        this.timeout = timeout;
    }

    public CustomScheduleService(CustomTimeoutService service) {
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
    protected final void runOneIteration() {
        this.runAction();
    }

    @Override
    protected final Scheduler scheduler() {
        return this.CustomServiceScheduler();
    }

}
