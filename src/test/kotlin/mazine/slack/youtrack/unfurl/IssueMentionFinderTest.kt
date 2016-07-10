package mazine.slack.youtrack.unfurl

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class IssueMentionFinderTest {

    @Test
    fun findMentionedIssues() {
        val finder = IssueMentionFinder("https://youtrack.jetbrains.com")

        val actualResults = finder.findMentionedIssues("""
            https://youtrack.jetbrains.com/issue/JPS-3852 - Save & Auto-save function on the pages of editing
            https://youtrack.jetbrains.com/issue/JPS-3855+ - Common look and feel for screens with General info (Settings)
            https://youtrack.jetbrains.com/issue/JPS-3990 - Improve search at the roles tab: for user/group/project pages
        """)

        assertArrayEquals(arrayOf(
                IssueMention("https://youtrack.jetbrains.com", "JPS-3852", false),
                IssueMention("https://youtrack.jetbrains.com", "JPS-3855", true),
                IssueMention("https://youtrack.jetbrains.com", "JPS-3990", false)
        ), actualResults.toList().toTypedArray())
    }

}