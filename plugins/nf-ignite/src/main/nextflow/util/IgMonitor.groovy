package nextflow.util

import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j

import java.net.*
import groovy.json.JsonSlurper
import nextflow.Session
import nextflow.executor.IgBaseTask
import nextflow.scheduler.Protocol.Resources

@Slf4j
class IgMonitor {
    private static String MONITORING_ENDPOINT

    private URL monitorApi

    private JsonBuilder json

    IgMonitor(Map config) {
        // TODO - setup Ignite config section
        this.monitorApi = new URL((String)config.get(MONITORING_ENDPOINT))

    }

    IgMonitor(String monitoringEndpoint) {
        this.monitorApi = new URL(monitoringEndpoint)
    }

    void sendUpdate(IgMonitoringUpdate update) {
        log?.debug("Sending monitoring update for task ${update.task.taskId} from node ${update.nodeId}")

        def payload = new JsonBuilder(buildRequest(update))
                                .toString()
        try {
            post(payload)
        } catch (Exception e) {
            log?.error("Monitoring update failed", e)
        }

    }

    private void post(String body) {
        HttpURLConnection post = monitorApi.openConnection()
        post.setRequestMethod("POST")
        post.setRequestProperty("accept", "application/json")
        post.setDoOutput(true)

        post.with {
            it.outputStream.withWriter { outputStreamWriter ->
                outputStreamWriter << body
            }
        }

        if (post.responseCode == 200) {
            log?.debug("post successful")
        } else {
            log?.error("could not send POST to ${post?.getURL()?.toString()}: ${post.responseMessage}")
        }
    }

    private String buildRequest(IgMonitoringUpdate update) {
        // TODO - create proper api request object
        return ""
    }
}
