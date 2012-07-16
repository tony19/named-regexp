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

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests {@link NamedMatcher}
 */
public class NamedMatcherTest {
	
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
	public void testStartPositionWithGroupName() {
		NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
    	NamedMatcher m = p.matcher("abcfooxyz");
    	m.find();
    	assertEquals(3, m.start("named"));
	}
	
	@Test
	public void testStartPositionWithGroupIndex() {
		NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
    	NamedMatcher m = p.matcher("abcfooxyz");
    	m.find();
    	assertEquals(1, m.start(2)); // 2 = index of (b)
	}
	
	@Test
	public void testEndPositionWithGroupName() {
		NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
    	NamedMatcher m = p.matcher("abcfooxyz");
    	m.find();
    	assertEquals(6, m.end("named"));
	}
	
	@Test
	public void testEndPositionWithGroupIndex() {
		NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
    	NamedMatcher m = p.matcher("abcfooxyz");
    	m.find();
    	assertEquals(2, m.end(2)); // 2 = index of (b)
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
    
	@Test
	public void testOrderedGroupsHasMatchesInOrder() {
		NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>foo)");
    	NamedMatcher m = p.matcher("abcfoo");
    	m.find();
    	List<String> matches = m.orderedGroups();
    	assertEquals(3, matches.size());
    	assertEquals("a", matches.get(0));
    	assertEquals("b", matches.get(1));
    	assertEquals("foo", matches.get(2));
	}
}
