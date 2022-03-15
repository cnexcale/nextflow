package nextflow.util

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j

@Slf4j
class IgMonitor {

    public static String CFG_MONITORING_ENDPOINT = "cluster.monitoringEndpoint"

    private URL monitoringEndpoint

    private JsonBuilder json

    IgMonitor(Map config) {
        this.monitoringEndpoint = new URL((String)config.navigate(CFG_MONITORING_ENDPOINT))
    }

    IgMonitor(String monitoringEndpoint) {
        this.monitoringEndpoint = new URL(monitoringEndpoint)
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

    String buildRequest(IgMonitoringUpdate update) {
        def updateDto = new IgMonitoringUpdateDto(update)

        JsonOutput.toJson(updateDto)
    }
    private void post(String body) {
        HttpURLConnection post = monitoringEndpoint.openConnection()
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


}
