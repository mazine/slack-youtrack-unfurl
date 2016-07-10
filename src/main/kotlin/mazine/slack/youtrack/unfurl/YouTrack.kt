package mazine.slack.youtrack.unfurl

import org.glassfish.jersey.client.JerseyClientBuilder
import javax.ws.rs.client.Client
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class YouTrack(val jerseyClient: Client = JerseyClientBuilder.createClient()) {

    fun requestIssue(youtrackURL: String, id: String, isExpanded: Boolean, accessToken: String? = null): YouTrackResponse {
        val response = jerseyClient.target(youtrackURL).
                path("rest").path("issue").path(id).
                request(MediaType.APPLICATION_JSON_TYPE).
                apply {
                    if (accessToken != null) {
                        header("Authorization", "Bearer $accessToken")
                    }
                }.
                get(Response::class.java)
        return if (response.status == Response.Status.OK.statusCode) {
            @Suppress("UNCHECKED_CAST")
            YouTrackResponse.Issue(youtrackURL, response.readEntity(Map::class.java) as Map<String, *>, isExpanded)
        } else {
            YouTrackResponse.Error(youtrackURL, id, response.statusInfo.reasonPhrase)
        }
    }

}
