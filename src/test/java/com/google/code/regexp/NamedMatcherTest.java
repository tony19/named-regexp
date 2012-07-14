package com.google.code.regexp;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;

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
    public void testNamedGroups() {
        int k = 0;
        for (Map.Entry<String, NamedMatcher> entry : goodMatchers.entries()) {
            List<String> groupNames = Patterns.groupNames.get(k++);
                assertTrue(entry.getValue() + " does not match " + entry.getKey(), entry.getValue().matches());
            NamedMatcher matcher = entry.getValue();
            Map<String, String> namedGroups = matcher.namedGroups();
            assertEquals("Group count is not right", matcher.groupCount(), namedGroups.size());
            assertTrue("Unknown matching groups", groupNames.containsAll(namedGroups.keySet()));
            int i = 0;
            for (String group : namedGroups.values()) {
                assertEquals("Group does not match", matcher.group(1 + i++), group);
            }
        }
    }

    @Test
    public void testGroupString() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testStartString() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testEndString() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testFind() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testToMatchResult() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testUsePattern() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testReset() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testResetCharSequence() {
        fail("Not yet implemented"); // TODO
    }

}
