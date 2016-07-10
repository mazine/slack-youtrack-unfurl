package mazine.slack.youtrack.unfurl

import allbegray.slack.SlackClientFactory
import allbegray.slack.rtm.Event
import allbegray.slack.type.Attachment
import allbegray.slack.type.Message
import allbegray.slack.webapi.method.chats.ChatPostMessageMethod
import com.fasterxml.jackson.databind.ObjectMapper

fun main(args: Array<String>) {
    if (args.size < 1) {
        println("Usage: java mazine.slack.youtrack.unfurl.BotKt <bot token>")
        System.exit(-1)
    }

    val token = args[0]
    val issueMentionFinders = sequenceOf(
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
                it.findMentionedIssues(message.text ?: "")
            }.map {
                youtrack.requestIssue(it.youtrackURL, it.issueID, it.isExpanded)
            }.toList()

            if (issues.any()) {
                slackClient.postMessage(ChatPostMessageMethod(event.get("channel").asText(), "").apply {
                    this.mapper = mapper
                    this.isAs_user = true
                    this.attachments = issues.map(::asMessageAttachment)
                })
            }

        }
    })

    rtmClient.connect()
}

fun asMessageAttachment(issue: YouTrackResponse): Attachment {
    return Attachment().apply {
        when (issue) {
            is YouTrackResponse.Issue -> {
                this.fallback = "${issue.id} ${issue.summary}"
                if (issue.isExpanded) {
                    this.text = "Assignee: *${issue.assignees.map { it.fullName }.joinToString("*, *")}*, " +
                            "Priority: *${issue.Priority.joinToString("*, *")}*, " +
                            "State: *${issue.State.joinToString("*, *")}*\n" +
                            "${issue.description}"
                    this.addMrkdwn_in("text")
                    this.footer = "Reported by *${issue.reporterFullName}*"
                    this.ts = issue.reportedAt.epochSecond.toInt()
                    this.addMrkdwn_in("footer")
                }
            }
            is YouTrackResponse.Error -> {
                this.fallback = "${issue.id} summary hidden"
            }
        }
        this.title = this.fallback
        this.title_link = issue.url
    }
}

