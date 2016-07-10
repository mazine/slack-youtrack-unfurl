package mazine.slack.youtrack.unfurl

import java.time.Instant
import java.time.LocalDateTime


sealed class YouTrackResponse(val youtrackURL: String) {
    abstract val id: String
    val url: String
        get() = "$youtrackURL/issue/$id"

    class Issue(youtrackURL: String, val json: Map<String, *>) : YouTrackResponse(youtrackURL) {
        override val id: String by json
        val field: List<Map<String, *>> by json
        val fieldsMap = field.map {
            IssueField(it)
        }.map {
            it.name to it.value
        }.toMap()
        val summary: String? by fieldsMap
        val description: String? by fieldsMap.withDefault { "" }
        val reporterFullName: String? by fieldsMap.withDefault { "Nobody" }
        private val created: String by fieldsMap.withDefault { LocalDateTime.now() }
        val reportedAt: Instant
            get() = Instant.ofEpochMilli(created.toLong())

        private val Assignee: List<Map<String, *>> by fieldsMap.withDefault { emptyList<Map<String, *>>() }
        val assignees: Sequence<User>
            get() = Assignee.asSequence().map { User(it) }

        val Priority: List<String> by fieldsMap.withDefault { emptyList<String>() }

        val State: List<String> by fieldsMap.withDefault { emptyList<String>() }



        override fun toString(): String {
            return "Issue(id='$id', summary='$summary')"
        }
    }

    class Error(youtrackURL: String, override val id: String, val message: String) : YouTrackResponse(youtrackURL) {
        override fun toString(): String {
            return "Error(message='$message')"
        }
    }
}

class IssueField(json: Map<String, *>) {
    val name: String by json
    val value: Any? by json
}

class User(json: Map<String, *>) {
    val value: String? by json
    val fullName: String? by json
}


