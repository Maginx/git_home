package com.guava.service;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.Service;

/**
 * Created by j69wang on 2016/9/23.
 */
interface CustomTimeoutService extends Service {

    void setStartTime(long startTime);

    long getTimeout();

    long getStartTime();

    String serviceName();

    // run action for nbi service
    void runAction();

    // if you want a schedule service, this function is needed
    AbstractScheduledService.Scheduler CustomServiceScheduler();

}
