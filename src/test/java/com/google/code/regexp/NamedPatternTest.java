package com.google.code.regexp;

import static org.junit.Assert.*;

import org.junit.Test;

public class NamedPatternTest {

    @Test
    public void testUnnamedGroupCount() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(c)(?<named>x)");
    	assertEquals(3, p.unnamedGroupCount());
    }
    
    @Test
    public void testUnnamedGroupCountWithNoncaptureGroups() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
    	assertEquals(2, p.unnamedGroupCount());
    }
    
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
    public void testIndexOfNotFound() {
    	NamedPattern p = NamedPattern.compile("(a)(b)(?:c)(?<named>x)");
    	assertEquals(-1, p.indexOf("dummy"));
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
}
