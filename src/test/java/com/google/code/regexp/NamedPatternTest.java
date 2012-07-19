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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.junit.Test;

/**
 * Tests {@link NamedPattern}
 */
public class NamedPatternTest {
    
    @Test
    public void testIndexOfNamedGroup() {
    	NamedPattern p = NamedPattern.compile("(?<named>x)");
    	assertEquals(0, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfNamedGroupAfterUnnamedGroups() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?<named>x)");
    	assertEquals(2, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfNamedGroupAfterNoncaptureGroups() {
    	NamedPattern p = NamedPattern.compile("(?:c)(?<named>x)");
    	assertEquals(0, p.indexOf("named"));
    }
        
    @Test
    public void testIndexOfNamedGroupAfterUnnamedAndNoncaptureGroups() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
    	assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterAnotherNamedGroup() {
    	NamedPattern p = NamedPattern.compile("(a)(?<foo>)(?:c)(?<named>x)");
    	assertEquals(2, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfNestedNamedGroup() {
    	NamedPattern p = NamedPattern.compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))");
    	assertEquals(3, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfNamedGroupAfterEscapedParen() {
    	NamedPattern p = NamedPattern.compile("\\(a\\)\\((b)\\)(?:c)(?<named>x)");
    	assertEquals(1, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterSpecialConstruct1() {
    	NamedPattern p = NamedPattern.compile("(?idsumx-idsumx)(?=b)(?!x)(?<named>x)");
    	assertEquals(0, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfNamedGroupBeforeSpecialConstruct1() {
    	NamedPattern p = NamedPattern.compile("(?<named>x)(?idsumx-idsumx)(?=b)(?!x)");
    	assertEquals(0, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfNamedGroupContainingSpecialConstruct() {
      	NamedPattern p = NamedPattern.compile("\\d{2}/\\d{2}/\\d{4}: EXCEPTION - (?<exception>(?s)(.+(?:Exception|Error)[^\\n]+(?:\\s++at [^\\n]+)++)(?:\\s*\\.{3}[^\\n]++)?\\s*)\\n");
       	assertEquals(0, p.indexOf("exception"));
    }
    
    @Test
    public void testIndexOfNotFound() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
    	assertEquals(-1, p.indexOf("dummy"));
    }
    
    @Test
    public void testIndexOfWithPositiveLookbehind() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?<=c)(?<named>x)");
    	assertEquals(2, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfWithNegativeLookbehind() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?<!c)(?<named>x)");
    	assertEquals(2, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfWithPositiveLookahead() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?=c)(?<named>x)");
    	assertEquals(2, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfWithNegativeLookahead() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?!c)(?<named>x)");
    	assertEquals(2, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfWithFlags() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?idsumx)(?<named>x)");
    	assertEquals(2, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfWithFlagsAndExtraNoCapture() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?idsumx:Z)(?<named>x)");
    	assertEquals(2, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfNamedGroupAtBeginning() {
    	NamedPattern p = NamedPattern.compile("(?<named>x)(a)(b)(?:c)");
    	assertEquals(0, p.indexOf("named"));
    }
    
    @Test
    public void testIndexOfNamedGroupAtMiddle() {
    	NamedPattern p = NamedPattern.compile("(a)(?<named>x)(b)(?:c)");
    	assertEquals(1, p.indexOf("named"));
    }
    
    @Test
    public void testNamedPatternGetsOriginalPattern() {
    	final String ORIG_PATT = "(a)(b)(?:c)(?<named>x)";
    	NamedPattern p = NamedPattern.compile(ORIG_PATT);
    	assertEquals(ORIG_PATT, p.namedPattern());
    }
    
    @Test
    public void testStandardPatternGetsOrigWithoutNamed() {
    	final String ORIG_PATT = "(a)(b)(?:c)(?<named>x)";
    	final String PATT_W_NO_NAMED_GRPS = "(a)(b)(?:c)(x)"; 
    	NamedPattern p = NamedPattern.compile(ORIG_PATT);
    	assertEquals(PATT_W_NO_NAMED_GRPS, p.standardPattern());
    }
    
    @Test
    public void testNamedPatternAfterFlagsAndLookarounds() {
    	final String ORIG_PATT = "(?idsumx-idsumx)(?=b)(?!x)(?<named>x)";
    	NamedPattern p = NamedPattern.compile(ORIG_PATT);
    	assertEquals(ORIG_PATT, p.namedPattern());
    }
    
    @Test
    public void testNamedPatternAfterEscapedParen() {
    	final String ORIG_PATT = "\\(a\\)\\((b)\\)(?:c)(?<named>x)";
    	NamedPattern p = NamedPattern.compile(ORIG_PATT);
    	assertEquals(ORIG_PATT, p.namedPattern());
    }
    
    @Test
    public void testGroupNames() {
    	final String PATT = "(foo)(?<X>a)(?<Y>b)(?<Z>c)(bar)";
    	NamedPattern p = NamedPattern.compile(PATT);
    	assertNotNull(p.groupNames());
    	assertEquals(3, p.groupNames().size());
    	assertEquals("X", p.groupNames().get(0));
    	assertEquals("Y", p.groupNames().get(1));
    	assertEquals("Z", p.groupNames().get(2));
    }
    
    @Test
    public void testGroupInfoMapHasNamesAsKeys() {
    	final String PATT = "(foo)(?<X>a)(?<Y>b)(bar)(?<Z>c)(?<Z>d)"; // two groups named "Z"
    	NamedPattern p = NamedPattern.compile(PATT);
    	Map<String,List<GroupInfo>> map = p.groupInfo();
    	assertNotNull(map);
    	assertEquals(3, map.size());
    	assertTrue(map.containsKey("X"));
    	assertTrue(map.containsKey("Y"));
    	assertTrue(map.containsKey("Z"));
    }
    
    @Test
    public void testGroupInfoMapHasCorrectPosAndGroupIndex() {
    	final String PATT = "(foo)(?<X>a)(?<Y>b)(bar)(?<Z>c)(?<Z>d)"; // two groups named "Z"
    	NamedPattern p = NamedPattern.compile(PATT);
    	Map<String,List<GroupInfo>> map = p.groupInfo();
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
    	NamedPattern.compile(PATT);
    }
    
    @Test
    public void testIgnoresPatternWithEscapedParens() {
    	final String PATT = "\\(?<name>abc\\)";
    	NamedPattern p = NamedPattern.compile(PATT);
    	assertEquals(PATT, p.standardPattern());
    }
    
    @Test
    public void testTakesPatternWithEscapedEscape() {
    	// it looks like an escaped parenthesis, but the escape char is 
    	// itself escaped and is thus a literal
    	final String PATT = "\\\\(?<name>abc)";
    	NamedPattern p = NamedPattern.compile(PATT);
    	assertEquals("\\\\(abc)", p.standardPattern());
    }
}
