package nextflow.util

import nextflow.executor.IgBaseTask
import nextflow.scheduler.Protocol.Resources

import java.time.Instant

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

    static class ResourceInfo {
        int cpus
        long memory
        long disk

        ResourceInfo(Resources res) {
            cpus = res.cpus
            memory = res.memory.toBytes()
            disk = res.disk.toBytes()
        }
    }

    String nodeId
    String clusterGroup
    String hostName

    String sessionId
    String taskName
    String event
    String timeStamp

    ResourceInfo totalResources
    ResourceInfo currentResources

    int pendingTasks
    int activeTasks

    String error


    IgMonitoringUpdateDto() {
        // for unit testing purpose only
    }

    IgMonitoringUpdateDto(IgMonitoringUpdate update) {
        nodeId = update.nodeId
        clusterGroup = update.clusterGroup
        hostName = InetAddress.localHost.hostName

        sessionId = update.task.sessionId.toString()
        taskName = update.task.taskName
        event = update.event
        timeStamp = Instant.now().toString()
        totalResources = new ResourceInfo(update.nodeResources)
        currentResources = new ResourceInfo(update.currentResources)
        pendingTasks = update.pendingTasks
        activeTasks = update.activeTasks
        error = update.error?.toString()

    }

}
