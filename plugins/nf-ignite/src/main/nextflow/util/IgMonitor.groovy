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
        log?.debug("configured IgMonitor for url ${monitoringEndpoint}")
    }

    IgMonitor(String monitoringEndpoint) {
        this.monitoringEndpoint = new URL(monitoringEndpoint)
        log?.debug("configured IgMonitor for url ${monitoringEndpoint}")
    }

    void sendUpdate(IgMonitoringUpdate update) {
        log?.debug("Sending monitoring update for task ${update.task.taskId} from node ${update.nodeId}")

        String payload

        try {

            payload = buildRequest(update)
            log?.debug("build request payload: ${payload}")
        } catch (Exception e) {
            log?.error("cannot convert update to string ${update}", e)
            return
        }

        try {
            post(payload)
        } catch (Exception e) {
            log?.error("Monitoring update failed", e)
        }

    }

    String buildRequest(IgMonitoringUpdate update) {
        JsonOutput.toJson(new IgMonitoringUpdateDto(update))
    }
    void post(String body) {
        log?.debug("preparing POST request")
        try {
            HttpURLConnection post = monitoringEndpoint.openConnection()

            log?.debug("opened connection to ${monitoringEndpoint}")
            post.setRequestMethod("POST")
            post.setRequestProperty("accept", "application/json")
            post.setDoOutput(true)

            log?.debug("writing body: ${body}")
            post.getOutputStream().write(body.getBytes("UTF-8"))

            if (post.responseCode == 201) {
                log?.debug("post successful")
            } else {
                log?.error("could not send POST to ${post?.getURL()?.toString()}: ${post.responseMessage}")
            }
        } catch (Exception e) {
            log?.error("unexpected error during POST", e)
        }
    }
}
