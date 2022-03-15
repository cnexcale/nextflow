package nextflow.util

import nextflow.executor.IgBaseTask
import nextflow.scheduler.Protocol.Resources

class IgMonitoringUpdate {
    String nodeId
    String clusterGroup
    IgBaseTask task
    String event
    Resources nodeResources
    Resources currentResources
    int activeTasks
    int pendingTasks
    Throwable error
}

class IgMonitoringUpdateDto {

    class ResourceInfo {
        Resources total
        Resources current

        ResourceInfo(Resources total, Resources current) {
            this.total = total
            this.current = current
        }
    }

    String nodeId
    String clusterGroup
    String taskName
    String event
    ResourceInfo resources
    int pendingTasks
    int activeTasks
    String error


    IgMonitoringUpdateDto() {
        // for unit testing purpose only
    }

    IgMonitoringUpdateDto(IgMonitoringUpdate update) {
        nodeId = update.nodeId
        clusterGroup = update.clusterGroup
        event = update.event
        taskName = update.task.taskName
        pendingTasks = update.pendingTasks
        activeTasks = update.activeTasks
        error = update.error?.toString()
        resources = new ResourceInfo(update.nodeResources, update.currentResources)
    }
}
