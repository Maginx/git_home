package com.google.common.util.concurrent;

/**
 * Created : j69wang <jeremy.wang@nokia.com>
 * Date : 2016/9/20
 */
interface CommonScheduler {

    void runOneIteration() throws Exception;

    void terminate();

    void startUp();

    CommonTaskService.NBISchedule bookExecutor();

    CommonTaskService.NBISchedule bookSchedule();

}
