/**
 * Copyright (C) 2012-2013 The named-regexp Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.regexp

import com.google.code.regexp.Pattern.Companion.compile
import org.hamcrest.Matchers.containsString
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.regex.PatternSyntaxException

/**
 * Tests [Matcher]
 */
class MatcherTest {
    var M1: Matcher? = null
    var M2: Matcher? = null

    @Rule @JvmField
    var thrown = ExpectedException.none()

    @Before
    fun beforeTest() {
        M1 = P.matcher(INPUT)
        M2 = P.matcher(INPUT)
    }

    @Test
    fun testFindSucceedsInFindingTaret() {
        Assert.assertTrue(P.matcher("abcfoo").find())
    }

    @Test
    fun testFindFailsToFindTarget() {
        Assert.assertFalse(P.matcher("hello").find())
    }

    @Test
    fun testStartPositionWithGroupName() {
        val m = P.matcher("abcfooxyz")
        m.find()
        Assert.assertEquals(3, m.start("named"))
    }

    @Test
    fun testStartPositionWithGroupIndex() {
        val m = P.matcher("abcfooxyz")
        m.find()
        Assert.assertEquals(1, m.start(2)) // 2 = index of (b)
    }

    @Test
    fun testEndPositionWithGroupName() {
        val m = P.matcher("abcfooxyz")
        m.find()
        Assert.assertEquals(6, m.end("named"))
    }

    @Test
    fun testEndPositionWithGroupIndex() {
        val m = P.matcher("abcfooxyz")
        m.find()
        Assert.assertEquals(2, m.end(2)) // 2 = index of (b)
    }

    @Test
    fun testToMatchResult() {
        val m = P.matcher("abcfoo")
        m.find()
        val r: MatchResult = m.toMatchResult()
        Assert.assertNotNull(r)
        Assert.assertEquals("foo", r.group("named"))
        Assert.assertEquals(0, r.start().toLong())
        Assert.assertEquals(3, r.start("named").toLong())
        Assert.assertEquals(6, r.end().toLong())
        Assert.assertEquals(6, r.end("named").toLong())
    }

    @Test
    fun testUsePatternSetsUnderlyingPattern() {
        val m = P.matcher("xyzabcfooabcfoo")
        m.find()
        Assert.assertEquals(3, m.start())
        m.find()
        Assert.assertEquals(9, m.start())

        // no more matches, m.find() should return false
        Assert.assertFalse(m.find())

        // change the pattern so that it matches the chars
        // at the beginning of the string; make sure it
        // doesn't match the previous pattern (which is
        // in the middle of the string)
        m.usePattern(compile("xy(?<named>z)"))
        m.reset()
        m.find()
        Assert.assertEquals(0, m.start())
    }

    @Test(timeout = 5000)
    fun testResetForcesFindFromBeginning() {
        val m = P.matcher("abcfooabcfoo")

        // advance find to last match; the test's timeout
        // protects from accidental infinite loop
        while (m.find());

        // resetting should force m.find() to search from beginning
        m.reset()
        m.find()
        Assert.assertEquals(0, m.start())
    }

    @Test(timeout = 5000)
    fun testResetCharSequence() {
        val m = P.matcher("abcfooabcx")

        // make sure at least one match is found and then advance
        // find to the end; the test's timeout protects from
        // infinite loop
        Assert.assertTrue(m.find())
        while (m.find());
        m.reset("dummy.*pattern")
        Assert.assertFalse(m.find())

        // move matching pattern to a diff position than original
        m.reset("hello world abcfoo foo bar")
        Assert.assertTrue(m.find())
        Assert.assertEquals(12, m.start())
    }

    @Test
    fun testNoMatchesForNamedGroup() {
        val m = P.matcher("abcd")
        Assert.assertFalse(m.find())

        // throws IllegalStateException("No match found")
        thrown.expect(IllegalStateException::class.java)
        thrown.expectMessage("No match found")
        m.group("named")
    }

    @Test
    fun testNoMatchesForInvalidGroupName() {
        val m = P.matcher("abcfoo")
        Assert.assertTrue(m.find())

        // throws IndexOutOfBoundsException: No group "nonexistentName"
        thrown.expect(IndexOutOfBoundsException::class.java)
        thrown.expectMessage("No group \"nonexistentName\"")
        m.group("nonexistentName")
    }

    @Test
    fun testNamedGroupAfterUnnamedAndNoncaptureGroups() {
        val p = compile("(a)(b)(?:c)(?<named>x)")
        val m = p.matcher("abcx")
        m.find()
        Assert.assertEquals("x", m.group("named"))
    }

    @Test
    fun testNamedGroupAfterUnnamedGroups() {
        val p = compile("(?:c)(?<named>x)")
        val m = p.matcher("abcx")
        m.find()
        Assert.assertEquals("x", m.group("named"))
    }

    @Test
    fun testNamedGroupAfterNoncaptureGroups() {
        val p = compile("(?:c)(?<named>x)")
        val m = p.matcher("abcx")
        m.find()
        Assert.assertEquals("x", m.group("named"))
    }

    @Test
    fun testNamedGroupAfterParensInCharacterClass() {
        val p = compile("(?:c)[(d-f0-9)]+(?<named>x)")
        val m = p.matcher("cdef5678x")
        m.find()
        Assert.assertEquals("x", m.group("named"))
    }

    @Test
    fun testNamedGroupAfterEscapedOpenParenInCharacterClass() {
        val p = compile("(?:c)[\\(d-f0-9)]+(?<named>x)")
        val m = p.matcher("cdef5678x")
        m.find()
        Assert.assertEquals("x", m.group("named"))
    }

    @Test
    fun testNamedGroupAfterEscapedCloseParenInCharacterClass() {
        val p = compile("(?:c)[(d-f0-9\\)]+(?<named>x)")
        val m = p.matcher("cdef5678x")
        m.find()
        Assert.assertEquals("x", m.group("named"))
    }

    @Test
    fun testNamedGroupAfterEscapedOpenBracket() {
        // since open-bracket is escaped, it doesn't create a character class
        val p =
            compile("(?:c)\\[([d-f0-9]+)](?<named>x)")
        val m = p.matcher("c[def5678]x")
        m.find()
        Assert.assertEquals("x", m.group("named"))
    }

    @Test
    fun testNamedGroupAfterCharClassThatHasEscapedCloseBracket() {
        // parser should be able to tell that the escaped close-bracket
        // is not closing the character class; and thus the following paren
        // is inside the character class (making it a literal)
        val p = compile("(?:c)[\\](d-f0-9)]+(?<named>x)")
        val m = p.matcher("cdef5678x")
        m.find()
        Assert.assertEquals("x", m.group("named"))
    }

    @Test
    fun testNamedGroupOnly() {
        val p = compile("(?<named>x)")
        val m = p.matcher("abcx")
        m.find()
        Assert.assertEquals("x", m.group("named"))
    }

    @Test
    fun testMatchNamedGroupAfterAnotherNamedGroup() {
        val p = compile("(a)(?<foo>b)(?:c)(?<named>x)")
        val m = p.matcher("abcx")
        m.find()
        Assert.assertEquals("x", m.group("named"))
    }

    @Test
    fun testIndexOfNestedNamedGroup() {
        val p =
            compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))")
        val m = p.matcher("abcdx")
        m.find()
        Assert.assertEquals("x", m.group("named"))
    }

    @Test
    fun testOrderedGroupsHasMatchesInOrder() {
        val p = compile("(a)(b)(?:c)(?<named>foo)")
        val m = p.matcher("abcfoo")
        m.find()
        val matches = m.orderedGroups()
        Assert.assertEquals(3, matches.size.toLong())
        Assert.assertEquals("a", matches[0])
        Assert.assertEquals("b", matches[1])
        Assert.assertEquals("foo", matches[2])
    }

    @Test
    fun testNamedGroupsDoesNotThrowIndexOutOfBounds() {
        // NamedMatcher.namedGroups() is used to get a map of
        // group names to group values. This should ignore unnamed
        // groups (exclude them from the map), but the unnamed
        // groups were throwing off the function, causing it to
        // fetch a named group at a non-existent index.
        // See Issue #1
        val p =
            compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))")
        val m = p.matcher("abcdx")
        try {
            m.namedGroups()
            // verified here: IndexOutOfBoundsException did not occur
        } catch (e: IndexOutOfBoundsException) {
            Assert.fail("IndexOutOfBoundsException should have been fixed")
        }
    }

    @Test
    fun testNamedGroupsGetsOnlyNamedGroups() {
        val p =
            compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))")
        val m = p.matcher("abcdxyz")
        val list = m.namedGroups()
        Assert.assertEquals(1, list.size.toLong())
        val map = list[0]
        Assert.assertEquals(3, map.size.toLong())
        Assert.assertEquals("b", map["foo"])
        Assert.assertEquals("dx", map["bar"])
        Assert.assertEquals("x", map["named"])
    }

    @Test
    fun testNamedGroupsWithNoMatchGetsEmptyMap() {
        val p =
            compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))")
        val m = p.matcher("nada")
        val list = m.namedGroups()
        Assert.assertEquals(0, list.size.toLong())
    }

    @Test
    fun testStandardPatternGetsOrigWithoutNamed() {
        val PATT_W_NO_NAMED_GRPS = "(a)(b)(?:c)(foo)"
        val m = P.matcher("abcfoo")
        Assert.assertEquals(PATT_W_NO_NAMED_GRPS, m.standardPattern().pattern())
    }

    @Test
    fun testNamedPatternCallGetsOriginalInstance() {
        Assert.assertEquals(P, P.matcher("abcfoo").namedPattern())
    }

    @Test
    fun testMatchesCallReturnsTrueForMatch() {
        Assert.assertTrue(P.matcher("abcfoo").matches())
    }

    @Test
    fun testMatchesCallReturnsFalseForMismatch() {
        Assert.assertFalse(P.matcher("foo").matches())
    }

    @Test
    fun testFindCallReturnsTrueForMatchFromBeginning() {
        Assert.assertTrue(P.matcher("abcfoo").find(0))
    }

    @Test
    fun testFindCallReturnsFalseForMismatchFromBeginning() {
        Assert.assertFalse(P.matcher("foo").find(0))
    }

    @Test
    fun testFindCallReturnsTrueForMatchFromMiddle() {
        Assert.assertTrue(P.matcher("Lorem ipsum abcfoo dolor sit amet").find(5))
    }

    @Test
    fun testFindCallReturnsFalseForMismatchFromMiddle() {
        Assert.assertFalse(P.matcher("Lorem ipsum abcXfoo dolor sit amet").find(5))
    }

    @Test
    fun testFindCallReturnsTrueForMatchFromEnd() {
        Assert.assertTrue(P.matcher("Lorem ipsum abcfoo").find(12))
    }

    @Test
    fun testFindCallReturnsFalseForMismatchFromEnd() {
        Assert.assertFalse(P.matcher("Lorem ipsum abcXfoo").find(12))
    }

    @Test
    fun testLookingAtCallReturnsTrueWhenAtMatchingText() {
        Assert.assertTrue(P.matcher("abcfoo Lorem ipsum").lookingAt())
    }

    @Test
    fun testLookingAtCallReturnsFalseWhenAtMismatchingText() {
        Assert.assertFalse(P.matcher("Lorem abcx ipsum").lookingAt())
    }

    @Test
    fun testEqualsReturnsTrueForSameMatcher() {
        Assert.assertTrue(M1!!.equals(M1))
    }

    @Test
    fun testEqualsReturnsFalseForTwoMatchersWithIdenticalValues() {
        Assert.assertFalse(M1!!.equals(M2))
    }

    @Test
    fun testEqualsReturnsFalseForTwoMatchersWithDifferentValues() {
        val m2 = P.matcher("foo bar")
        Assert.assertFalse(M1!!.equals(m2))
    }

    @Test
    fun testEqualsReturnsFalseForTwoMatcherWithDifferentParentPatterns() {
        val p2 = compile("(a)(b)(?:c)(?<named>x)")
        val m1 = P.matcher("Lorem abcx ipsum")
        val m2 = p2.matcher("Lorem abcx ipsum")
        Assert.assertFalse(m1.equals(m2))
    }

    @Test
    fun testEqualsReturnsFalseWhenComparedWithNull() {
        Assert.assertFalse(M1!!.equals(null))
    }

    @Test
    fun testEqualsReturnsFalseWhenComparedWithDifferentDataType() {
        Assert.assertFalse(M1!!.equals(Any()))
    }

    @Test
    fun testHashCodeGetsUniqueHashForTwoMatchersWithIdenticalValues() {
        Assert.assertFalse(M1.hashCode() == M2.hashCode())
    }

    @Test
    fun testHashCodeGetsUniqueHashForTwoMatchersWithDifferentValues() {
        val m2 = P.matcher("foo bar")
        Assert.assertFalse(M1.hashCode() == m2.hashCode())
    }

    @Test
    fun testHashCodeGetsUniqueHashForTwoMatchersWithDifferentParentPatterns() {
        val p2 = compile("foo bar")
        val m2 = p2.matcher("Lorem abcx ipsum")
        Assert.assertFalse(M1.hashCode() == m2.hashCode())
    }

    @Test
    fun testUsePatternNullThrowsException() {
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("newPattern cannot be null")
        M1!!.usePattern(null)
    }

    @Test
    fun testAppendReplacementReturnsOrigInstance() {
        M1!!.find()
        Assert.assertEquals(M1, M1!!.appendReplacement(StringBuffer("foo"), "bar"))
    }

    @Test
    fun testAppendReplacementReplacesMatchAndAppendsToBuffer() {
        // M1.find() must be called before appendReplacement(), which replaces
        // the found match with a given string and appends the result to a
        // given buffer. Note the result is a substring of the full string,
        // from the beginning of the string up to the last character of the
        // replacement string.
        val sb = StringBuffer("origText ")
        M1!!.find()
        M1!!.appendReplacement(sb, "foo")
        Assert.assertEquals("origText Lorem foo", sb.toString())
        M1!!.find()
        M1!!.appendReplacement(sb, "bar")
        Assert.assertEquals("origText Lorem foo ipsum bar", sb.toString())
    }

    @Test
    fun testAppendReplacementWithNamedRefs() {
        val sb = StringBuffer("origText ")
        M1!!.find()
        M1!!.appendReplacement(sb, "\${named}foo \${named}bar")
        Assert.assertEquals("origText Lorem foofoo foobar", sb.toString())
    }

    @Test
    fun testAppendReplacementWithInvalidNamedRefs() {
        thrown.expect(PatternSyntaxException::class.java)
        thrown.expectMessage(containsString("unknown group name near index 2"))
        M1!!.appendReplacement(StringBuffer(), "\${nonexistentName} foobar!")
    }

    @Test
    fun testAppendTailAppendsRemainderToBuffer() {
        val sb = StringBuffer("origText ")
        M1!!.find()
        M1!!.appendReplacement(sb, "foo")

        // appendTail() should append the rest of the matcher
        // string to the buffer. Even if the string contains
        // a match, the match must not be replaced.
        M1!!.appendTail(sb)
        Assert.assertEquals("origText Lorem foo ipsum abcfoo", sb.toString())
    }

    @Test
    fun testGroupGetsTheMatchingText() {
        M1!!.find()
        Assert.assertEquals("abcfoo", M1!!.group())
    }

    @Test
    fun testRegionInvalidStartIndexThrowsException() {
        thrown.expect(IndexOutOfBoundsException::class.java)
        thrown.expectMessage("start")
        M1!!.region(-100, 1)
        M1!!.region(1000000000, 1)
    }

    @Test
    fun testRegionInvalidEndIndexThrowsException() {
        thrown.expect(IndexOutOfBoundsException::class.java)
        thrown.expectMessage("end")
        M1!!.region(0, -100)
        M1!!.region(0, 1000000000)
    }

    @Test
    fun testRegionSetsLimitsForMatcher() {
        val firstPos = INPUT.indexOf("abcfoo")
        val lastPos = INPUT.lastIndexOf("abcfoo")
        val m = M1!!.region(firstPos + 1, INPUT.length)
        Assert.assertTrue(m.find())
        Assert.assertTrue(firstPos != lastPos)
        Assert.assertEquals(lastPos, m.start())
        Assert.assertEquals(lastPos + "abcfoo".length.toInt(), m.end())
    }

    @Test
    fun testRegionStartGetsStartPosOfRegion() {
        Assert.assertEquals(2, M1!!.region(2, 3).regionStart())
    }

    @Test
    fun testRegionEndGetsEndPosOfRegion() {
        Assert.assertEquals(3, M1!!.region(2, 3).regionEnd())
    }

    @Test(timeout = 5000)
    fun testHitEndGetsTrueWhenNoMoreMatches() {
        // the test's timeout protects from infinite loop
        while (M1!!.find());
        Assert.assertTrue(M1!!.hitEnd())
    }

    @Test
    fun testHitEndGetsFalseWhenMoreMatches() {
        Assert.assertFalse(M1!!.hitEnd())
    }

    @Test
    fun testRequireEnd() {
        // requireEnd is normally false (see grepcode OpenJDK)
        Assert.assertFalse(M1!!.requireEnd())
    }

    @Test
    fun testHasTransparentBoundsTrueWhenUseTransTrue() {
        Assert.assertTrue(M1!!.useTransparentBounds(true).hasTransparentBounds())
    }

    @Test
    fun testHasTransparentBoundsFalseWhenUseTransFalse() {
        Assert.assertFalse(M1!!.useTransparentBounds(false).hasTransparentBounds())
    }

    @Test
    fun testUseTransparentBounds() {
        val text = "Madagascar is best seen by car or bike."
        val m =
            compile("\\b(?<target>car)\\b").matcher(text)

        // Set starting bound to char 7 in "Madagascar" (which is "car")
        // and then try to find the "car" word toward the end. Without
        // transparent bounds, the search will find "car" at the beginning
        // because the matcher can't see beyond the bounds to determine
        // that this is not a word boundary (from "\\b" in regex).
        m.region(7, text.length).find()
        Assert.assertEquals(7, m.start())
        m.reset().useTransparentBounds(true).find()
        Assert.assertEquals(27, m.start())
    }

    @Test
    fun testHasAnchoringBoundsTrueWhenUseAnchorTrue() {
        Assert.assertTrue(M1!!.useAnchoringBounds(true).hasAnchoringBounds())
    }

    @Test
    fun testHasAnchoringBoundsFalseWhenUseAnchorFalse() {
        Assert.assertFalse(M1!!.useAnchoringBounds(false).hasAnchoringBounds())
    }

    @Test
    fun testUseAnchoringBounds() {
        val text = "The fox jumped over the white picket fence."
        val m =
            compile("^(?<target>fox jumped) over").matcher(text)

        // setting the starting region to "fox" will make
        // the matcher think that it's found the target
        // since "fox" is at the beginning of its search
        // region (only true when anchoring bounds false)
        m.region(4, text.length)
        Assert.assertTrue(m.find())
        Assert.assertEquals(4, m.start())

        // setting the anchoring bounds to false lets the
        // matcher realize the target is not actually at
        // the beginning of the string
        Assert.assertFalse(m.reset().useAnchoringBounds(false).find())
    }

    @Test
    fun testReplaceAll() {
        Assert.assertEquals("Lorem xyz ipsum xyz", M1!!.replaceAll("xyz"))
    }

    @Test
    fun testReplaceAllWithNamedRefs() {
        Assert.assertEquals("Lorem foo@foo# ipsum foo@foo#", M1!!.replaceAll("\${named}@\${named}#"))
    }

    @Test
    fun testReplaceAllWithInvalidNamedRefs() {
        thrown.expect(PatternSyntaxException::class.java)
        thrown.expectMessage(containsString("unknown group name near index 2"))
        M1!!.replaceAll("\${nonexistentName} foobar!")
    }

    @Test
    fun testReplaceFirst() {
        Assert.assertEquals("Lorem xyz ipsum abcfoo", M1!!.replaceFirst("xyz"))
    }

    @Test
    fun testReplaceFirstWithNamedRefs() {
        Assert.assertEquals("Lorem foo@foo# ipsum abcfoo", M1!!.replaceFirst("\${named}@\${named}#"))
    }

    @Test
    fun testReplaceFirstWithInvalidNamedRefs() {
        thrown.expect(PatternSyntaxException::class.java)
        thrown.expectMessage(containsString("unknown group name near index 2"))
        M1!!.replaceFirst("\${nonexistentName} foobar!")
    }

    @Test
    fun testToString() {
        Assert.assertNotNull(M1.toString())
        Assert.assertTrue(M1.toString().trim { it <= ' ' }.length > 0)
    }

    @Test
    fun testBackrefMatches() {
        val p =
            compile("(?<a>xyz)(?<num>\\d+)abc\\k<num>def")
        val m = p.matcher("xyz12345abc12345def")
        Assert.assertTrue(m.find())
        Assert.assertEquals("12345", m.group("num"))
    }

    @Test
    fun testBackrefNoMatch() {
        val p =
            compile("(?<a>xyz)(?<num>\\d+)abc\\k<num>def")
        // this should not match because the 2nd number is not equal
        // to the first captured number
        Assert.assertFalse(p.matcher("xyz12345abc123456def").find())
    }

    @Test
    fun testParenFoundAfterQuoteEscapedBracket() {
        // Open-bracket is quote-escaped so it's not a character class;
        // process it as a literal. Previously, we saw the bracket as a
        // character class, which messed up the group indexes in the pattern
        // as reported in Issue #2.
        val p =
            compile("(?<T0>\\Q[\\E)(?<T1>\\d+)(?<T2>-)(?<T3>\\d+)(?<T4>\\])")
        val m = p.matcher("[1-0]")
        Assert.assertTrue(m.find())
        Assert.assertEquals("[", m.group("T0"))
        Assert.assertEquals("1", m.group("T1"))
        Assert.assertEquals("-", m.group("T2"))
        Assert.assertEquals("0", m.group("T3"))
        Assert.assertEquals("]", m.group("T4"))
    }

    @Test
    fun testRealPatternFoundAfterQuoteEscapedPattern() {
        // The quote-escaped string looks like a real regex pattern, but
        // it's a literal string, so ignore it. The pattern after that
        // should still be found
        val p =
            compile("\\Q(?<foo>\\d+)  [  \\E(?<name>abc\\d+)  \\Q]\\E")
        val m = p.matcher("(?<foo>\\d+)  [  abc123  ]")
        Assert.assertTrue(m.find())
        Assert.assertEquals("abc123", m.group("name"))
    }

    @Test
    fun testQuoteEscapedPatternDoesNotCreateNamedGroup() {
        val p =
            compile("\\Q(?<foo>\\d+)\\E (?<name>abc\\d+)")
        val m = p.matcher("(?<foo>\\d+) abc123")
        Assert.assertTrue(m.find())
        thrown.expect(IndexOutOfBoundsException::class.java)
        thrown.expectMessage("No group \"foo\"")
        m.group("foo")
    }

    @Test
    fun testNamedGroupFoundInEscapedQuote() {
        // since quote-escape is itself escaped, it's actually a literal \Q and \E
        val p = compile("(abc)\\\\Q(?<named>\\d+)\\\\E")
        val m = p.matcher("abc\\Q123\\E")
        Assert.assertTrue(m.find())
        Assert.assertEquals("123", m.group("named"))
    }

    @Test
    fun testNamedGroupsGetsAllMatchesInSingleGroup() {
        val pattern = compile("(?<digit>\\d)(\\w)")
        val matcher = pattern.matcher("2foo3bar4")
        val groups =
            matcher.namedGroups()
        Assert.assertEquals(2, groups.size.toLong())
        Assert.assertEquals("2", groups[0]["digit"])
        Assert.assertEquals("3", groups[1]["digit"])
    }

    @Test
    fun testNamedGroupsGetsAllMatchesInMultipleGroups() {
        val pattern =
            compile("(?<dayOfYear>\\d+).(?<dayName>\\w+)")
        val matcher =
            pattern.matcher("1 Sunday foo bar 2 Monday foo bar 3 Tuesday foo bar 4 Wednesday foo bar 5 Thursday foo bar 6 Friday foo bar  7 Saturday foo bar 8 Sunday foo bar 9 Monday foo bar 10 Tuesday foo bar ")
        val groups =
            matcher.namedGroups()
        val DAYS = arrayOf(
            "Sunday",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday",
            "Monday",
            "Tuesday"
        )
        Assert.assertEquals(DAYS.size.toLong(), groups.size.toLong())
        for (i in DAYS.indices) {
            Assert.assertEquals((i + 1).toString(), groups[i]["dayOfYear"])
            Assert.assertEquals(DAYS[i], groups[i]["dayName"])
        }
    }

    // Specify 1 second timeout to account for potential infinite loop (Issue #9)
    @Test(timeout = 1000)
    fun testNamedGroupsReturnsEmptyListWhenNoGroupPresent() {
        val pattern = compile("\\d+ no groups")
        val matcher = pattern.matcher("123 no groups")
        val groups =
            matcher.namedGroups()
        Assert.assertTrue(groups.isEmpty())
    }

    companion object {
        const val INPUT = "Lorem abcfoo ipsum abcfoo"
        const val PATT = "(a)(b)(?:c)(?<named>foo)"
        val P = compile(PATT)
    }
}