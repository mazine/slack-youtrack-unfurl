package mazine.slack.youtrack.unfurl


data class IssueMention(val youtrackURL: String, val issueID: String, val isExpanded: Boolean)

class IssueMentionFinder(val youtrackURL: String) {
    val issueRegex = Regex("""\Q${youtrackURL.replace("\\", "\\\\")}\E/issue/([\w\d-]+\-\d+)(\+?)""")

    fun findMentionedIssues(text: String) = issueRegex.findAll(text).map {
        val (issueID, expanded) = it.destructured
        IssueMention(youtrackURL, issueID, expanded.isNotEmpty())
    }
}
