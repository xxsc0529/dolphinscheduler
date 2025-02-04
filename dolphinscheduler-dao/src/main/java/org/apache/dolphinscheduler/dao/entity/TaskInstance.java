/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * task instance
 */
@Data
@TableName("t_ds_task_instance")
public class TaskInstance implements Serializable {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * task name
     */
    private String name;

    /**
     * task type
     */
    private String taskType;

    private int processInstanceId;

    private String processInstanceName;

    private Long projectCode;

    private long taskCode;

    private int taskDefinitionVersion;

    @TableField(exist = false)
    private String processDefinitionName;

    /**
     * process instance name
     */
    @TableField(exist = false)
    private int taskGroupPriority;

    /**
     * state
     */
    private TaskExecutionStatus state;

    /**
     * task first submit time.
     */
    private Date firstSubmitTime;

    /**
     * task submit time
     */
    private Date submitTime;

    /**
     * task start time
     */
    private Date startTime;

    /**
     * task end time
     */
    private Date endTime;

    /**
     * task host
     */
    private String host;

    /**
     * task shell execute path and the resource down from hdfs
     * default path: $base_run_dir/processInstanceId/taskInstanceId/retryTimes
     */
    private String executePath;

    /**
     * task log path
     * default path: $base_run_dir/processInstanceId/taskInstanceId/retryTimes
     */
    private String logPath;

    /**
     * retry times
     */
    private int retryTimes;

    /**
     * alert flag
     */
    private Flag alertFlag;

    /**
     * process instance
     */
    @TableField(exist = false)
    private WorkflowInstance workflowInstance;

    /**
     * process definition
     */
    @TableField(exist = false)
    private WorkflowDefinition processDefine;

    /**
     * task definition
     */
    @TableField(exist = false)
    private TaskDefinition taskDefine;

    /**
     * process id
     */
    private int pid;

    /**
     * appLink
     */
    private String appLink;

    /**
     * flag
     */
    private Flag flag;

    /**
     * task is cache: yes/no
     */
    private Flag isCache;

    /**
     * cache_key
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String cacheKey;

    /**
     * duration
     */
    @TableField(exist = false)
    private String duration;

    /**
     * max retry times
     */
    private int maxRetryTimes;

    /**
     * task retry interval, unit: minute
     */
    private int retryInterval;

    /**
     * task intance priority
     */
    private Priority taskInstancePriority;

    /**
     * process intance priority
     */
    @TableField(exist = false)
    private Priority processInstancePriority;

    /**
     * workerGroup
     */
    private String workerGroup;

    /**
     * environment code
     */
    private Long environmentCode;

    /**
     * environment config
     */
    private String environmentConfig;

    /**
     * executor id
     */
    private int executorId;

    /**
     * varPool string
     */
    private String varPool;

    private String executorName;

    /**
     * delay execution time.
     */
    private int delayTime;

    /**
     * task params
     */
    private String taskParams;

    /**
     * dry run flag
     */
    private int dryRun;
    /**
     * task group id
     */
    private int taskGroupId;

    /**
     * cpu quota
     */
    private Integer cpuQuota;

    /**
     * max memory
     */
    private Integer memoryMax;

    /**
     * task execute type
     */
    private TaskExecuteType taskExecuteType;

    /**
     * test flag
     */
    private int testFlag;

    public void init(String host, Date startTime, String executePath) {
        this.host = host;
        this.startTime = startTime;
        this.executePath = executePath;
    }

    public boolean isTaskComplete() {

        return this.getState().isSuccess()
                || this.getState().isKill()
                || (this.getState().isFailure() && !taskCanRetry())
                || this.getState().isForceSuccess();
    }

    public boolean isFirstRun() {
        return endTime == null;
    }

    /**
     * determine if a task instance can retry
     * if subProcess,
     *
     * @return can try result
     */
    public boolean taskCanRetry() {
        if (TaskTypeUtils.isSubWorkflowTask(getTaskType())) {
            return false;
        }
        if (this.getState() == TaskExecutionStatus.NEED_FAULT_TOLERANCE) {
            return true;
        }
        return this.getState() == TaskExecutionStatus.FAILURE && (this.getRetryTimes() < this.getMaxRetryTimes());
    }

    /**
     * whether the retry interval is timed out
     *
     * @return Boolean
     */
    public boolean retryTaskIntervalOverTime() {
        if (getState() != TaskExecutionStatus.FAILURE) {
            return true;
        }
        if (getMaxRetryTimes() == 0 || getRetryInterval() == 0) {
            return true;
        }
        Date now = new Date();
        long failedTimeInterval = DateUtils.differSec(now, getEndTime());
        // task retry does not over time, return false
        return TimeUnit.MINUTES.toSeconds(getRetryInterval()) < failedTimeInterval;
    }

}
