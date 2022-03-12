package nextflow.util

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import nextflow.Session
import nextflow.executor.IgBaseTask
import nextflow.scheduler.Protocol.Resources

class IgMonitor {
    private static String MONITORING_ENDPOINT

    private URL monitorApi

    private JsonBuilder json

    IgMonitor(Map config) {
        this.monitorApi = new URL(config.get(MONITORING_ENDPOINT))

    }
    void sendUpdate(IgMonitoringUpdate update) {
        def payload = new JsonBuilder(update).toString()
        def post = monitorApi.openConnection()
        post.set

    }
}
