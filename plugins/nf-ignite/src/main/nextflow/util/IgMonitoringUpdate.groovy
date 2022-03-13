package nextflow.util

import nextflow.Session
import nextflow.executor.IgBaseTask
import nextflow.scheduler.Protocol.Resources

class IgMonitoringUpdate {
    IgBaseTask task
    Resources nodeResources
    Session session
    String nodeId
    int queueSize
    int activeTasks
}
