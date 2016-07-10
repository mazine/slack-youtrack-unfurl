package mazine.slack.youtrack.unfurl

import allbegray.slack.SlackClientFactory
import allbegray.slack.rtm.Event
import allbegray.slack.type.Attachment
import allbegray.slack.type.Message
import allbegray.slack.webapi.method.chats.ChatPostMessageMethod
import com.fasterxml.jackson.databind.ObjectMapper

fun main(args: Array<String>) {
    val token = "token"
    val issueMentionFinders = listOf(
            IssueMentionFinder("https://youtrack.jetbrains.com"),
            IssueMentionFinder("https://jobs.myjetbrains.com/youtrack")
    )

    val mapper = ObjectMapper()
    val rtmClient = SlackClientFactory.createSlackRealTimeMessagingClient(token, mapper)
    val slackClient = SlackClientFactory.createWebApiClient(token)
    val youtrack = YouTrack()

    rtmClient.addListener(Event.MESSAGE, { event ->
        val message = mapper.convertValue(event, Message::class.java)

        if (message.subtype == null) {
            val issues = issueMentionFinders.flatMap {
                val url = it.youtrackURL
                it.findMentionedIssues(message.text ?: "").map { issueID ->
                    youtrack.requestIssue(url, issueID)
                }
            }

            if (issues.isNotEmpty()) {
                slackClient.postMessage(ChatPostMessageMethod(event.get("channel").asText(), "").apply {
                    this.mapper = mapper
                    this.isAs_user = true
                    this.attachments = issues.map { issue ->
                        Attachment().apply {
                            when (issue) {
                                is YouTrackResponse.Issue -> {
                                    this.fallback = "${issue.id} ${issue.summary}"
                                    this.text =
                                            "Assignee: *${issue.assignees.map { it.fullName }.joinToString("*, *")}*, " +
                                                    "Priority: *${issue.Priority.joinToString("*, *")}*, " +
                                                    "State: *${issue.State.joinToString("*, *")}*\n" +
                                                    "${issue.description}"
                                    this.addMrkdwn_in("text")
                                    this.footer = "Reported by *${issue.reporterFullName}*"
                                    this.ts = issue.reportedAt.epochSecond.toInt()
                                    this.addMrkdwn_in("footer")
                                }
                                is YouTrackResponse.Error -> {
                                    this.fallback = "${issue.id} summary hidden"
                                }
                            }
                            this.title = this.fallback
                            this.title_link = issue.url
                        }
                    }
                })
            }

        }
    })

    rtmClient.connect()
}