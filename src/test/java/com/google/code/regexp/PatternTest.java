/**
 * Copyright (C) 2012 The named-regexp Authors
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
package com.google.code.regexp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link Pattern}
 */
public class PatternTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // REGEX-9, First test needs to check for infinite loop in
    // NamedPattern.compile() (seen in Android) because all other
    // tests rely on it.
    @Test( timeout = 2000 )
    public void testNoInfiniteLoopInNamedPatternCompile() {
        assertNotNull(Pattern.compile("(?<named>x)"));
    }

    @Test
    public void testIndexOfNamedGroup() {
        Pattern p = Pattern.compile("(?<named>x)");
        assertEquals(0, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterUnnamedGroups() {
        Pattern p = Pattern.compile("(a)(b)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterNoncaptureGroups() {
        Pattern p = Pattern.compile("(?:c)(?<named>x)");
        assertEquals(0, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterUnnamedAndNoncaptureGroups() {
        Pattern p = Pattern.compile("(a)(b)(?:c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterAnotherNamedGroup() {
        Pattern p = Pattern.compile("(a)(?<foo>)(?:c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNestedNamedGroup() {
        Pattern p = Pattern.compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))");
        assertEquals(3, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterEscapedParen() {
        Pattern p = Pattern.compile("\\(a\\)\\((b)\\)(?:c)(?<named>x)");
        assertEquals(1, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterSpecialConstruct1() {
        Pattern p = Pattern.compile("(?idsumx-idsumx)(?=b)(?!x)(?<named>x)");
        assertEquals(0, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupBeforeSpecialConstruct1() {
        Pattern p = Pattern.compile("(?<named>x)(?idsumx-idsumx)(?=b)(?!x)");
        assertEquals(0, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupContainingSpecialConstruct() {
        Pattern p = Pattern.compile("\\d{2}/\\d{2}/\\d{4}: EXCEPTION - (?<exception>(?s)(.+(?:Exception|Error)[^\\n]+(?:\\s++at [^\\n]+)++)(?:\\s*\\.{3}[^\\n]++)?\\s*)\\n");
        assertEquals(0, p.indexOf("exception"));
    }

    @Test
    public void testIndexOfNotFound() {
        Pattern p = Pattern.compile("(a)(b)(?:c)(?<named>x)");
        assertEquals(-1, p.indexOf("dummy"));
    }

    @Test
    public void testIndexOfWithPositiveLookbehind() {
        Pattern p = Pattern.compile("(a)(b)(?<=c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithNegativeLookbehind() {
        Pattern p = Pattern.compile("(a)(b)(?<!c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithNegativeLookbehindAtBeginning() {
        Pattern p = Pattern.compile("(?<!a)(b)(c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithPositiveLookbehindAtBeginning() {
        Pattern p = Pattern.compile("(?<=a)(b)(c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithPositiveLookahead() {
        Pattern p = Pattern.compile("(a)(b)(?=c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithNegativeLookahead() {
        Pattern p = Pattern.compile("(a)(b)(?!c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithFlags() {
        Pattern p = Pattern.compile("(a)(b)(?idsumx)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithFlagsAndExtraNoCapture() {
        Pattern p = Pattern.compile("(a)(b)(?idsumx:Z)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAtBeginning() {
        Pattern p = Pattern.compile("(?<named>x)(a)(b)(?:c)");
        assertEquals(0, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAtMiddle() {
        Pattern p = Pattern.compile("(a)(?<named>x)(b)(?:c)");
        assertEquals(1, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithMultipleGroupsWithSameName() {
        Pattern p = Pattern.compile("(a)(?<named>x)(b)(?:c)(?<named>y)");
        assertEquals(3, p.indexOf("named", 1));
    }

    @Test
    public void testIndexOfWithInvalidPositiveInstanceIndex() {
        Pattern p = Pattern.compile("(a)(?<named>x)(b)(?:c)(?<named>y)");
        thrown.expect(IndexOutOfBoundsException.class);
        thrown.expectMessage("Index: 10000000, Size: 2");
        assertEquals(-1, p.indexOf("named", 10000000));
    }

    @Test
    public void testIndexOfWithInvalidNegativeInstanceIndex() {
        Pattern p = Pattern.compile("(a)(?<named>x)(b)(?:c)(?<named>y)");
        // Negative index causes ArrayIndexOutOfBoundsException (which
        // is a subclass of IndexOutOfBoundsException)
        thrown.expect(ArrayIndexOutOfBoundsException.class);
        thrown.expectMessage("-100");
        assertEquals(-1, p.indexOf("named", -100));
    }

    @Test
    public void testNamedPatternGetsOriginalPattern() {
        final String ORIG_PATT = "(a)(b)(?:c)(?<named>x)";
        Pattern p = Pattern.compile(ORIG_PATT);
        assertEquals(ORIG_PATT, p.namedPattern());
    }

    @Test
    public void testStandardPatternGetsOrigWithoutNamed() {
        final String ORIG_PATT = "(a)(b)(?:c)(?<named>x)";
        final String PATT_W_NO_NAMED_GRPS = "(a)(b)(?:c)(x)";
        Pattern p = Pattern.compile(ORIG_PATT);
        assertEquals(PATT_W_NO_NAMED_GRPS, p.standardPattern());
    }

    @Test
    public void testNamedPatternAfterFlagsAndLookarounds() {
        final String ORIG_PATT = "(?idsumx-idsumx)(?=b)(?!x)(?<named>x)";
        Pattern p = Pattern.compile(ORIG_PATT);
        assertEquals(ORIG_PATT, p.namedPattern());
    }

    @Test
    public void testNamedPatternAfterEscapedParen() {
        final String ORIG_PATT = "\\(a\\)\\((b)\\)(?:c)(?<named>x)";
        Pattern p = Pattern.compile(ORIG_PATT);
        assertEquals(ORIG_PATT, p.namedPattern());
    }

    @Test
    public void testGroupNames() {
        final String PATT = "(foo)(?<X>a)(?<Y>b)(?<Z>c)(bar)";
        Pattern p = Pattern.compile(PATT);
        assertNotNull(p.groupNames());
        assertEquals(3, p.groupNames().size());
        assertEquals("X", p.groupNames().get(0));
        assertEquals("Y", p.groupNames().get(1));
        assertEquals("Z", p.groupNames().get(2));
    }

    @Test
    public void testGroupInfoMapHasNamesAsKeys() {
        final String PATT = "(foo)(?<X>a)(?<Y>b)(bar)(?<Z>c)(?<Z>d)"; // two groups named "Z"
        Pattern p = Pattern.compile(PATT);
        Map<String,List<GroupInfo> > map = p.groupInfo();
        assertNotNull(map);
        assertEquals(3, map.size());
        assertTrue(map.containsKey("X"));
        assertTrue(map.containsKey("Y"));
        assertTrue(map.containsKey("Z"));
    }

    @Test
    public void testGroupInfoMapHasCorrectPosAndGroupIndex() {
        final String PATT = "(foo)(?<X>a)(?<Y>b)(bar)(?<Z>c)(?<Z>d)"; // two groups named "Z"
        Pattern p = Pattern.compile(PATT);
        Map<String,List<GroupInfo> > map = p.groupInfo();
        assertNotNull(map);

        GroupInfo[] inf = (GroupInfo[])map.get("X").toArray(new GroupInfo[0]);
        assertEquals(1, inf.length);
        assertEquals(PATT.indexOf("(?<X>"), inf[0].pos());
        assertEquals(1, inf[0].groupIndex());

        GroupInfo[] inf2 = (GroupInfo[])map.get("Y").toArray(new GroupInfo[0]);
        assertEquals(1, inf2.length);
        assertEquals(PATT.indexOf("(?<Y>"), inf2[0].pos());
        assertEquals(2, inf2[0].groupIndex());

        // test both Z groups
        GroupInfo[] inf3 = (GroupInfo[])map.get("Z").toArray(new GroupInfo[0]);
        assertEquals(2, inf3.length);
        int posZ = PATT.indexOf("(?<Z>");
        assertEquals(posZ, inf3[0].pos());
        assertEquals(4, inf3[0].groupIndex());
        assertEquals(PATT.indexOf("(?<Z>", posZ+1), inf3[1].pos());
        assertEquals(5, inf3[1].groupIndex());
    }

    @Test(expected = PatternSyntaxException.class)
    public void testEscapedLeftParenCausesPatternException() {
        final String PATT = "\\(?<name>abc)";
        Pattern.compile(PATT);
    }

    @Test
    public void testIgnoresPatternWithEscapedParens() {
        final String PATT = "\\(?<name>abc\\)";
        Pattern p = Pattern.compile(PATT);
        assertEquals(PATT, p.standardPattern());
    }

    @Test
    public void testTakesPatternWithEscapedEscape() {
        // it looks like an escaped parenthesis, but the escape char is
        // itself escaped and is thus a literal
        final String PATT = "\\\\(?<name>abc)";
        Pattern p = Pattern.compile(PATT);
        assertEquals("\\\\(abc)", p.standardPattern());
    }

    @Test
    public void testIgnoresPatternWithOddNumberEscapes() {
        final String PATT = "\\\\\\(?<name>abc\\)";
        Pattern p = Pattern.compile(PATT);
        assertEquals(PATT, p.standardPattern());
    }

    @Test
    public void testTakesPatternWithOddNumberEscapesButWithSpace() {
        final String PATT = "\\ \\\\(?<name>abc)";
        Pattern p = Pattern.compile(PATT);
        assertEquals("\\ \\\\(abc)", p.standardPattern());
    }

    @Test
    public void testCompileRegexWithFlags() {
        final String PATT = "(?<name>abc) # comment 1";
        int flags = java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.COMMENTS;
        Pattern p = Pattern.compile(PATT, flags);
        assertEquals(PATT, p.namedPattern());
        assertEquals(flags, p.flags());
    }

    @Test
    public void testSplitGetsArrayOfTextAroundMatches() {
        Pattern p = Pattern.compile("(a)(b)(?:c)(?<named>x)");
        assertArrayEquals(new String[]{"foo ", " bar "}, p.split("foo abcx bar abcx"));
        // when the limit is specified, the last element contains
        // the remainder of the string
        assertArrayEquals(new String[]{"foo ", " bar abcx"}, p.split("foo abcx bar abcx", 2));
    }

    @Test
    public void testEqualsNullGetsFalse() {
        Pattern p = Pattern.compile("(a)(b)(?:c)(?<named>x)");
        assertFalse(p.equals(null));
    }

    @Test
    public void testEqualsDiffDataTypeGetsFalse() {
        Pattern p = Pattern.compile("(a)(b)(?:c)(?<named>x)");
        assertFalse(p.equals(new Object()));
    }

    @Test
    public void testEqualsWithSamePatternAndFlagsGetsTrue() {
        Pattern p1 = Pattern.compile("(a)(b)(?:c)(?<named>x)");
        Pattern p2 = Pattern.compile("(a)(b)(?:c)(?<named>x)");
        assertTrue(p1.equals(p2));
    }

    @Test
    public void testEqualsWithSamePatternButDiffFlagsGetsFalse() {
        Pattern p1 = Pattern.compile("(a)(b)(?:c)(?<named>x)");
        Pattern p2 = Pattern.compile("(a)(b)(?:c)(?<named>x)", java.util.regex.Pattern.CASE_INSENSITIVE);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsWithSameFlagsButDiffPatternGetsFalse() {
        Pattern p1 = Pattern.compile("(a)(b)(?:c)(?<named>x)", java.util.regex.Pattern.DOTALL);
        Pattern p2 = Pattern.compile("(?<named>x)", java.util.regex.Pattern.DOTALL);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsGetsTrueForSameInstance() {
        Pattern p = Pattern.compile("(a)(b)(?:c)(?<named>x)");
        assertTrue(p.equals(p));
    }

    @Test
    public void testToString() {
        String s = Pattern.compile("(a)(b)(?:c)(?<named>x)").toString();
        assertNotNull(s);
        assertTrue(s.trim().length() > 0);
    }

    @Test
    public void testCompileWithBackrefGetsStandardPatternWithCorrectGroupIndex() {
        Pattern p = Pattern.compile("(?<foo>xyz)(?<bar>\\d+)abc\\k<bar>");
        assertEquals("(xyz)(\\d+)abc\\2", p.standardPattern());
    }

    @Test
    public void testCompileWithUnknownBackref() {
        thrown.expect(PatternSyntaxException.class);
        thrown.expectMessage("unknown group name near index 11\n" +
                             "(xyz)abc\\k<bar>\n" +
                             "           ^");
        Pattern.compile("(?<foo>xyz)abc\\k<bar>");
    }

    @Test
    public void testCompileWithEscapedBackref() {
        // escaped backrefs are not translated
        Pattern p = Pattern.compile("(?<foo>xyz)(?<bar>\\d+)abc\\\\k<bar>");
        assertEquals("(xyz)(\\d+)abc\\\\k<bar>", p.standardPattern());
    }
}
