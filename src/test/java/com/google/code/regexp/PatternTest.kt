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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.regex.PatternSyntaxException

/**
 * Tests [Pattern]
 */
class PatternTest {
    @Rule @JvmField
    var thrown = ExpectedException.none()

    // REGEX-9, First test needs to check for infinite loop in
    // NamedPattern.compile() (seen in Android) because all other
    // tests rely on it.
    @Test(timeout = 2000)
    fun testNoInfiniteLoopInNamedPatternCompile() {
        Assert.assertNotNull(compile("(?<named>x)"))
    }

    @Test
    fun testIndexOfAcceptsClassName() {
        val p = compile("(?<com.example.foo>x)")
        Assert.assertEquals(0, p.indexOf("com.example.foo").toLong())
    }

    @Test
    fun testIndexOfAcceptsNameWithSpacesAndPunctuation() {
        val p =
            compile("(?<  Lorem ipsum dolor sit amet, consectetur adipisicing elit>x)")
        Assert.assertEquals(
            0,
            p.indexOf("  Lorem ipsum dolor sit amet, consectetur adipisicing elit").toLong()
        )
    }

    @Test
    fun testIndexOfAcceptsNameWithClosingAngleBracket() {
        val p =
            compile("(?<foo bar > should not grab this bracket> x)")
        Assert.assertEquals(0, p.indexOf("foo bar ").toLong())
    }

    @Test
    fun testIndexOfAcceptsNameWithNewLines() {
        val p =
            compile("(?<Lorem ipsum dolor sit amet,\n consectetur adipisicing elit>x)")
        Assert.assertEquals(
            0,
            p.indexOf("Lorem ipsum dolor sit amet,\n consectetur adipisicing elit").toLong()
        )
    }

    @Test
    fun testIndexOfNameWithUnicodeChars() {
        val p = compile("(?<gefräßig>x)")
        Assert.assertEquals(0, p.indexOf("gefräßig").toLong())
    }

    @Test
    fun testIndexOfNamedGroup() {
        val p = compile("(?<named>x)")
        Assert.assertEquals(0, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterUnnamedGroups() {
        val p = compile("(a)(b)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterNoncaptureGroups() {
        val p = compile("(?:c)(?<named>x)")
        Assert.assertEquals(0, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterUnnamedAndNoncaptureGroups() {
        val p = compile("(a)(b)(?:c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterAnotherNamedGroup() {
        val p = compile("(a)(?<foo>)(?:c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNestedNamedGroup() {
        val p =
            compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))")
        Assert.assertEquals(3, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterEscapedParen() {
        val p =
            compile("\\(a\\)\\((b)\\)(?:c)(?<named>x)")
        Assert.assertEquals(1, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterSpecialConstruct1() {
        val p =
            compile("(?idsumx-idsumx)(?=b)(?!x)(?<named>x)")
        Assert.assertEquals(0, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupBeforeSpecialConstruct1() {
        val p =
            compile("(?<named>x)(?idsumx-idsumx)(?=b)(?!x)")
        Assert.assertEquals(0, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupContainingSpecialConstruct() {
        val p =
            compile("\\d{2}/\\d{2}/\\d{4}: EXCEPTION - (?<exception>(?s)(.+(?:Exception|Error)[^\\n]+(?:\\s++at [^\\n]+)++)(?:\\s*\\.{3}[^\\n]++)?\\s*)\\n")
        Assert.assertEquals(0, p.indexOf("exception").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterNonEscapedParenInCharacterClass() {
        val p =
            compile("(a)(?<foo>[()])(?:c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterEscapedParensInCharacterClass() {
        val p =
            compile("(a)(?<foo>[\\(\\)])(?:c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterEscapedOpenParenInCharacterClass() {
        val p =
            compile("(a)(?<foo>[\\()])(?:c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterEscapedCloseParenInCharacterClass() {
        val p =
            compile("(a)(?<foo>[(\\)])(?:c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterSlashedParensInCharacterClass() {
        // double-slashes in a character class are literal slashes, not escapes
        val p =
            compile("(a)(?<foo>[\\\\(\\\\)])(?:c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterSlashedOpenParenInCharacterClass() {
        // double-slashes in a character class are literal slashes, not escapes
        val p =
            compile("(a)(?<foo>[\\\\()])(?:c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterSlashedCloseParenInCharacterClass() {
        // double-slashes in a character class are literal slashes, not escapes
        val p =
            compile("(a)(?<foo>[(\\\\)])(?:c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterNonEscapedParenInCharClassWithEscapedCloseBracket() {
        val p =
            compile("(a)(?<foo>[\\]()])(?:c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterNonEscapedParenAfterEscapedOpenBracket() {
        // since the open-bracket is escaped, it doesn't create a character class,
        // so the parentheses inside the "foo" group is a capturing group (that
        // currently captures nothing but still valid regex and thus counted)
        val p =
            compile("(a)(?<foo>\\[()])(?:c)(?<named>x)")
        Assert.assertEquals(3, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNotFound() {
        val p = compile("(a)(b)(?:c)(?<named>x)")
        Assert.assertEquals(-1, p.indexOf("dummy").toLong())
    }

    @Test
    fun testIndexOfWithPositiveLookbehind() {
        val p = compile("(a)(b)(?<=c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfWithNegativeLookbehind() {
        val p = compile("(a)(b)(?<!c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfWithNegativeLookbehindAtBeginning() {
        val p = compile("(?<!a)(b)(c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfWithPositiveLookbehindAtBeginning() {
        val p = compile("(?<=a)(b)(c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfWithPositiveLookahead() {
        val p = compile("(a)(b)(?=c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfWithNegativeLookahead() {
        val p = compile("(a)(b)(?!c)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfWithFlags() {
        val p = compile("(a)(b)(?idsumx)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfWithFlagsAndExtraNoCapture() {
        val p = compile("(a)(b)(?idsumx:Z)(?<named>x)")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAtBeginning() {
        val p = compile("(?<named>x)(a)(b)(?:c)")
        Assert.assertEquals(0, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAtMiddle() {
        val p = compile("(a)(?<named>x)(b)(?:c)")
        Assert.assertEquals(1, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfWithMultipleGroupsWithSameName() {
        val p =
            compile("(a)(?<named>x)(b)(?:c)(?<named>y)")
        Assert.assertEquals(3, p.indexOf("named", 1).toLong())
    }

    @Test
    fun testIndexOfWithInvalidPositiveInstanceIndex() {
        val p =
            compile("(a)(?<named>x)(b)(?:c)(?<named>y)")
        thrown.expect(IndexOutOfBoundsException::class.java)
        thrown.expectMessage("10000000")
        Assert.assertEquals(-1, p.indexOf("named", 10000000).toLong())
    }

    @Test
    fun testIndexOfWithInvalidNegativeInstanceIndex() {
        val p =
            compile("(a)(?<named>x)(b)(?:c)(?<named>y)")
        thrown.expect(IndexOutOfBoundsException::class.java)
        thrown.expectMessage("-100")
        Assert.assertEquals(-1, p.indexOf("named", -100).toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterQuoteEscapedBracket() {
        // open-bracket escaped, so it's not a character class
        val p = compile("(a)(b)\\Q[\\E(?<named>c)\\]")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterSlashEscapedBracket() {
        // open-bracket escaped, so it's not a character class
        val p = compile("(a)(b)\\[(?<named>c)\\]")
        Assert.assertEquals(2, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupAfterQuoteEscapedPattern() {
        // The quote-escaped string looks like a real regex pattern, but
        // it's a literal string, so ignore it. The pattern after that
        // should still be found
        val p =
            compile("(?<foo>a)\\Q(?<bar>b)(?<baz>c)(d)  [  \\E(?<named>e)  \\Q]\\E")
        Assert.assertEquals(1, p.indexOf("named").toLong())
    }

    @Test
    fun testIndexOfNamedGroupInEscapedQuote() {
        // since quote-escape is itself escaped, it's actually a literal \Q and \E
        val p = compile("(a)\\\\Q(?<named>\\d+)\\\\E")
        Assert.assertEquals(1, p.indexOf("named").toLong())
    }

    @Test
    fun testInvalidCloseQuoteEscapeSequence() {
        // when \E present, \Q must also be present, so the following is invalid syntax
        thrown.expect(PatternSyntaxException::class.java)
        compile("(a)\\\\Q(?<named>d)\\E")
    }

    @Test
    fun testNamedPatternGetsOriginalPattern() {
        val ORIG_PATT = "(a)(b)(?:c)(?<named>x)"
        val p = compile(ORIG_PATT)
        Assert.assertEquals(ORIG_PATT, p.namedPattern())
    }

    @Test
    fun testStandardPatternGetsOrigWithoutNamed() {
        val ORIG_PATT = "(a)(b)(?:c)(?<named>x)"
        val PATT_W_NO_NAMED_GRPS = "(a)(b)(?:c)(x)"
        val p = compile(ORIG_PATT)
        Assert.assertEquals(PATT_W_NO_NAMED_GRPS, p.standardPattern())
    }

    @Test
    fun testNamedPatternAfterFlagsAndLookarounds() {
        val ORIG_PATT = "(?idsumx-idsumx)(?=b)(?!x)(?<named>x)"
        val p = compile(ORIG_PATT)
        Assert.assertEquals(ORIG_PATT, p.namedPattern())
    }

    @Test
    fun testNamedPatternAfterEscapedParen() {
        val ORIG_PATT = "\\(a\\)\\((b)\\)(?:c)(?<named>x)"
        val p = compile(ORIG_PATT)
        Assert.assertEquals(ORIG_PATT, p.namedPattern())
    }

    @Test
    fun testGroupNames() {
        val PATT = "(foo)(?<X>a)(?<Y>b)(?<Z>c)(bar)"
        val p = compile(PATT)
        Assert.assertNotNull(p.groupNames())
        Assert.assertEquals(3, p.groupNames().size.toLong())
        Assert.assertEquals("X", p.groupNames()[0])
        Assert.assertEquals("Y", p.groupNames()[1])
        Assert.assertEquals("Z", p.groupNames()[2])
    }

    @Test
    fun testGroupInfoMapHasNamesAsKeys() {
        val PATT = "(foo)(?<X>a)(?<Y>b)(bar)(?<Z>c)(?<Z>d)" // two groups named "Z"
        val p = compile(PATT)
        val map = p.groupInfo()
        Assert.assertNotNull(map)
        Assert.assertEquals(3, map.size.toLong())
        Assert.assertTrue(map.containsKey("X"))
        Assert.assertTrue(map.containsKey("Y"))
        Assert.assertTrue(map.containsKey("Z"))
    }

    @Test
    fun testGroupInfoMapHasCorrectPosAndGroupIndex() {
        val PATT = "(foo)(?<X>a)(?<Y>b)(bar)(?<Z>c)(?<Z>d)" // two groups named "Z"
        val p = compile(PATT)
        val map = p.groupInfo()
        Assert.assertNotNull(map)
        val inf = map["X"]!!.toTypedArray()
        Assert.assertEquals(1, inf.size.toLong())
        Assert.assertEquals(PATT.indexOf("(?<X>"), inf[0].pos())
        Assert.assertEquals(1, inf[0].groupIndex())
        val inf2 = map["Y"]!!.toTypedArray()
        Assert.assertEquals(1, inf2.size.toLong())
        Assert.assertEquals(PATT.indexOf("(?<Y>"), inf2[0].pos())
        Assert.assertEquals(2, inf2[0].groupIndex())

        // test both Z groups
        val inf3 = map["Z"]!!.toTypedArray()
        Assert.assertEquals(2, inf3.size)
        val posZ = PATT.indexOf("(?<Z>")
        Assert.assertEquals(posZ, inf3[0].pos())
        Assert.assertEquals(4, inf3[0].groupIndex())
        Assert.assertEquals(PATT.indexOf("(?<Z>", posZ + 1), inf3[1].pos())
        Assert.assertEquals(5, inf3[1].groupIndex())
    }

    @Test(expected = PatternSyntaxException::class)
    fun testEscapedLeftParenCausesPatternException() {
        val PATT = "\\(?<name>abc)"
        compile(PATT)
    }

    @Test
    fun testIgnoresPatternWithEscapedParens() {
        val PATT = "\\(?<name>abc\\)"
        val p = compile(PATT)
        Assert.assertEquals(PATT, p.standardPattern())
    }

    @Test
    fun testTakesPatternWithEscapedEscape() {
        // it looks like an escaped parenthesis, but the escape char is
        // itself escaped and is thus a literal
        val PATT = "\\\\(?<name>abc)"
        val p = compile(PATT)
        Assert.assertEquals("\\\\(abc)", p.standardPattern())
    }

    @Test
    fun testIgnoresPatternWithOddNumberEscapes() {
        val PATT = "\\\\\\(?<name>abc\\)"
        val p = compile(PATT)
        Assert.assertEquals(PATT, p.standardPattern())
    }

    @Test
    fun testTakesPatternWithOddNumberEscapesButWithSpace() {
        val PATT = "\\ \\\\(?<name>abc)"
        val p = compile(PATT)
        Assert.assertEquals("\\ \\\\(abc)", p.standardPattern())
    }

    @Test
    fun testCompileRegexWithFlags() {
        val PATT = "(?<name>abc) # comment 1"
        val flags = Pattern.CASE_INSENSITIVE or Pattern.COMMENTS
        val p = compile(PATT, flags)
        Assert.assertEquals(PATT, p.namedPattern())
        Assert.assertEquals(flags, p.flags())
    }

    @Test
    fun testSplitGetsArrayOfTextAroundMatches() {
        val p = compile("(a)(b)(?:c)(?<named>x)")
        Assert.assertArrayEquals(arrayOf("foo ", " bar "), p.split("foo abcx bar abcx"))
        // when the limit is specified, the last element contains
        // the remainder of the string
        Assert.assertArrayEquals(arrayOf("foo ", " bar abcx"), p.split("foo abcx bar abcx", 2))
    }

    @Test
    fun testEqualsNullGetsFalse() {
        val p = compile("(a)(b)(?:c)(?<named>x)")
        Assert.assertFalse(p.equals(null))
    }

    @Test
    fun testEqualsDiffDataTypeGetsFalse() {
        val p = compile("(a)(b)(?:c)(?<named>x)")
        Assert.assertFalse(p.equals(Any()))
    }

    @Test
    fun testEqualsWithSamePatternAndFlagsGetsTrue() {
        val p1 = compile("(a)(b)(?:c)(?<named>x)")
        val p2 = compile("(a)(b)(?:c)(?<named>x)")
        Assert.assertTrue(p1.equals(p2))
    }

    @Test
    fun testEqualsWithSamePatternButDiffFlagsGetsFalse() {
        val p1 = compile("(a)(b)(?:c)(?<named>x)")
        val p2 = compile(
            "(a)(b)(?:c)(?<named>x)",
            Pattern.CASE_INSENSITIVE
        )
        Assert.assertFalse(p1.equals(p2))
    }

    @Test
    fun testEqualsWithSameFlagsButDiffPatternGetsFalse() {
        val p1 =
            compile("(a)(b)(?:c)(?<named>x)", Pattern.DOTALL)
        val p2 =
            compile("(?<named>x)", Pattern.DOTALL)
        Assert.assertFalse(p1.equals(p2))
    }

    @Test
    fun testEqualsGetsTrueForSameInstance() {
        val p = compile("(a)(b)(?:c)(?<named>x)")
        Assert.assertTrue(p.equals(p))
    }

    @Test
    fun testToString() {
        val s = compile("(a)(b)(?:c)(?<named>x)").toString()
        Assert.assertNotNull(s)
        Assert.assertTrue(s.trim { it <= ' ' }.length > 0)
    }

    @Test
    fun testCompileWithBackrefGetsStandardPatternWithCorrectGroupIndex() {
        val p =
            compile("(?<foo>xyz)(?<bar>\\d+)abc\\k<bar>")
        Assert.assertEquals("(xyz)(\\d+)abc\\2", p.standardPattern())
    }

    @Test
    fun testCompileWithUnknownBackref() {
        thrown.expect(PatternSyntaxException::class.java)
        thrown.expectMessage(containsString("unknown group name near index 11"))
        compile("(?<foo>xyz)abc\\k<bar>")
    }

    @Test
    fun testCompileWithEscapedBackref() {
        // escaped backrefs are not translated
        val p =
            compile("(?<foo>xyz)(?<bar>\\d+)abc\\\\k<bar>")
        Assert.assertEquals("(xyz)(\\d+)abc\\\\k<bar>", p.standardPattern())
    }

    @Test
    fun testCompileBackrefAcceptsClassName() {
        val GROUP_NAME = "com.example.foo"
        val p =
            compile("(?<foo>xyz)(?<$GROUP_NAME>\\d+)abc\\k<$GROUP_NAME>")
        Assert.assertEquals("(xyz)(\\d+)abc\\2", p.standardPattern())
    }

    @Test
    fun testCompileBackrefAcceptsNameWithSpacesAndPunctuation() {
        val GROUP_NAME = "  Lorem ipsum dolor sit amet, consectetur adipisicing elit"
        val p =
            compile("(?<foo>xyz)(?<$GROUP_NAME>\\d+)abc\\k<$GROUP_NAME>")
        Assert.assertEquals("(xyz)(\\d+)abc\\2", p.standardPattern())
    }

    @Test
    fun testCompileBackrefTakesFirstClosingAngleBracket() {
        val GROUP_NAME = "foo bar  >"
        val p =
            compile("(?<foo>xyz)(?<$GROUP_NAME>\\d+)abc\\k<$GROUP_NAME>")
        // The first closing bracket encountered is used. The second becomes a literal,
        // so we check for it in the standard pattern (two actually).
        Assert.assertEquals("(xyz)(>\\d+)abc\\2>", p.standardPattern())
    }

    @Test
    fun testCompileBackrefAcceptsNameWithNewLines() {
        val GROUP_NAME = "Lorem ipsum dolor sit amet,\n consectetur adipisicing elit"
        val p =
            compile("(?<foo>xyz)(?<$GROUP_NAME>\\d+)abc\\k<$GROUP_NAME>")
        Assert.assertEquals("(xyz)(\\d+)abc\\2", p.standardPattern())
    }

    @Test
    fun testCompileBackrefAcceptsNameWithUnicodeChars() {
        val GROUP_NAME = "gefräßig"
        val p =
            compile("(?<foo>xyz)(?<$GROUP_NAME>\\d+)abc\\k<$GROUP_NAME>")
        Assert.assertEquals("(xyz)(\\d+)abc\\2", p.standardPattern())
    }
}