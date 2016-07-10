package mazine.slack.youtrack.unfurl


class IssueMentionFinder(val youtrackURL: String) {
    val issueRegex = Regex("""\Q${youtrackURL.replace("\\", "\\\\")}\E/issue/([\w\d-]+\-\d+)""")

    fun findMentionedIssues(text: String) = issueRegex.findAll(text).map {
        val (issueID) = it.destructured
        issueID
    }.toList()
}