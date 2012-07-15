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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;

/**
 * Tests {@link NamedMatcher}
 */
public class NamedMatcherTest {

    private List<NamedPattern> patterns = newArrayList();

    private Multimap<String, NamedMatcher>
            goodMatchers = HashMultimap.create(),
            badMatchers = HashMultimap.create();

    @Before
    public void setUp() throws Exception {
        for (String pattern : Patterns.patterns) {
            patterns.add(NamedPattern.compile(pattern));
        }

        int i = 0;
        for (List<String> inputs : Patterns.goodInputs) {
            NamedPattern pattern = patterns.get(i++);
            for (String input : inputs) {
                goodMatchers.put(input, pattern.matcher(input));
            }
        }

        i = 0;
        for (List<String> inputs : Patterns.badInputs) {
            NamedPattern pattern = patterns.get(i++);
            for (String input : inputs) {
                badMatchers.put(input, pattern.matcher(input));
            }
        }

    }

    @Test
    public void testStandardPattern() {
        int i = 0;
        for (NamedPattern pattern : patterns) {
            assertEquals("Standard pattern does not match", Patterns.standardPatterns.get(i++), pattern.standardPattern());
        }
    }

    @Test
    public void testNamedPattern() {
        int i = 0;
        for (NamedPattern pattern : patterns) {
            assertEquals("Named pattern does not match", Patterns.patterns.get(i++), pattern.namedPattern());
        }
    }

    @Test
    public void testMatchesGoodInput() {
        for (Map.Entry<String, NamedMatcher> entry : goodMatchers.entries()) {
            assertTrue(entry.getValue() + " does not match " + entry.getKey(), entry.getValue().matches());
        }
    }

    @Test
    public void testMatchesBadInput() {
        for (Map.Entry<String, NamedMatcher> entry : badMatchers.entries()) {
            assertFalse(entry.getValue() + " matches " + entry.getKey() + " but shouldn't", entry.getValue().matches());
        }
    }

    @Test
    public void testOrderedGroups() {
        for (Map.Entry<String, NamedMatcher> entry : goodMatchers.entries()) {
            assertTrue(entry.getValue() + " does not match " + entry.getKey(), entry.getValue().matches());
            NamedMatcher matcher = entry.getValue();
            List<String> orderedGroups = matcher.orderedGroups();
            assertEquals("Group count is not right", matcher.groupCount(), orderedGroups.size());
            int i = 0;
            for (String group : orderedGroups) {
                assertEquals("Group does not match", matcher.group(1 + i++), group);
            }
        }
    }

    @Test
    public void testFindSuccess() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
    	NamedMatcher m = p.matcher("abcfoo");
    	assertTrue(m.find());
    }

    @Test
    public void testFindFail() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
    	NamedMatcher m = p.matcher("hello");
    	assertFalse(m.find());
    }
    
    @Test
    public void testToMatchResult() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
    	NamedMatcher m = p.matcher("abcfoo");
    	
    	m.find();
    	NamedMatchResult r = m.toMatchResult();
    	assertNotNull(r);
    	
    	assertEquals("foo", r.group("named"));
    	
    	assertEquals(0, r.start());
    	assertEquals(3, r.start("named"));
    	assertEquals(6, r.end());
    	assertEquals(6, r.end("named"));
    }

    @Test
    public void testUsePattern() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
    	NamedMatcher m = p.matcher("xyzabcxabcx");
    	m.find();
    	assertEquals(3, m.start());
    	m.find();
    	assertEquals(7, m.start());
    	
    	// no more matches, m.find() should return false
    	assertFalse(m.find());
    	
    	m.usePattern(NamedPattern.compile("xy(?<named>z)"));
    	m.reset();
    	m.find();
    	assertEquals(0, m.start());
    }

    @Test
    public void testReset() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
    	NamedMatcher m = p.matcher("abcxabcx");
    	
    	// advance find to last match
    	while(m.find());
    	
    	// resetting should force m.find() to search from beginning
    	m.reset();
    	
    	m.find();
    	assertEquals(0, m.start());
    }

    @Test
    public void testResetCharSequence() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
    	NamedMatcher m = p.matcher("abcxabcx");
    	
    	// make sure at least one match is found and then advance
    	// find to the end
    	assertTrue(m.find());
    	while(m.find());
    	
    	m.reset("dummy.*pattern");
    	
    	assertFalse(m.find());
    	
    	// move matching pattern to a diff position than original
    	m.reset("hello world abcx foo bar");
    	assertTrue(m.find());
    	assertEquals(12, m.start());
    }

    @Test(expected = IllegalStateException.class)
    public void testNoMatchesForNamedGroup() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
    	NamedMatcher m = p.matcher("abcd");
    	assertFalse(m.find());
    	
    	// throws IllegalStateException("No match found")
    	assertEquals(null, m.group("named"));
    }
    
    @Test
    public void testNamedGroupAfterUnnamedAndNoncaptureGroups() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
    	NamedMatcher m = p.matcher("abcx");
    	m.find();
    	assertEquals("x", m.group("named"));
    }
    
    @Test
    public void testNamedGroupAfterUnnamedGroups() {
    	NamedPattern p = NamedPattern.compile("(?:c)(?<named>x)");
    	NamedMatcher m = p.matcher("abcx");
    	m.find();
    	assertEquals("x", m.group("named"));
    }
    
    @Test
    public void testNamedGroupAfterNoncaptureGroups() {
    	NamedPattern p = NamedPattern.compile("(?:c)(?<named>x)");
    	NamedMatcher m = p.matcher("abcx");
    	m.find();
    	assertEquals("x", m.group("named"));
    }
    
    @Test
    public void testNamedGroupOnly() {
    	NamedPattern p = NamedPattern.compile("(?<named>x)");
    	NamedMatcher m = p.matcher("abcx");
    	m.find();
    	assertEquals("x", m.group("named"));
    }
    
    @Test
    public void testMatchNamedGroupAfterAnotherNamedGroup() {
    	NamedPattern p = NamedPattern.compile("(a)(?<foo>b)(?:c)(?<named>x)");
    	NamedMatcher m = p.matcher("abcx");
    	m.find();
    	assertEquals("x", m.group("named"));
    }
    
    @Test
    public void testIndexOfNestedNamedGroup() {
    	NamedPattern p = NamedPattern.compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))");
    	NamedMatcher m = p.matcher("abcdx");
    	m.find();
    	assertEquals("x", m.group("named"));
    }
}
