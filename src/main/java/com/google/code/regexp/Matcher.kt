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

import java.util.*

/**
 * An engine that performs match operations on a character sequence by
 * interpreting a [Pattern]. This is a wrapper for [java.util.regex.Matcher].
 *
 * @since 0.1.9
 */
class Matcher : MatchResult {
    private var matcher: java.util.regex.Matcher
    private var parentPattern: Pattern

    internal constructor(parentPattern: Pattern, matcher: java.util.regex.Matcher) {
        this.parentPattern = parentPattern
        this.matcher = matcher
    }

    @Deprecated(
        """Use {@link #Matcher(Pattern parentPattern, java.util.regex.Matcher matcher)}
     
      JDK9 removes the ability to cast a MatchResult to a Matcher,
      resulting in a runtime error. There appears to be no feasible
      way to perform this conversion ourselves with only the given
      parameters, so this constructor is now deprecated in favor
      of the new constructor that passes in the original matcher."""
    )
    internal constructor(parentPattern: Pattern, matcher: java.util.regex.MatchResult) {
        this.parentPattern = parentPattern
        this.matcher = matcher as java.util.regex.Matcher // runtime error here in JDK9
    }

    internal constructor(parentPattern: Pattern, input: CharSequence?) {
        this.parentPattern = parentPattern
        matcher = parentPattern.pattern().matcher(input)
    }

    /**
     * Returns the pattern that is interpreted by this matcher.
     *
     * @return the pattern
     */
    fun standardPattern() = matcher.pattern()

    /**
     * Returns the named pattern that is interpreted by this matcher.
     *
     * @return the pattern
     */
    fun namedPattern() = parentPattern

    /**
     * Changes the Pattern that this Matcher uses to find matches with
     *
     * @param newPattern the new pattern
     * @return this Matcher
     */
    fun usePattern(newPattern: Pattern?): Matcher {
        requireNotNull(newPattern) { "newPattern cannot be null" }
        parentPattern = newPattern
        matcher.usePattern(newPattern.pattern())
        return this
    }

    /**
     * Resets this matcher
     *
     * @return this Matcher
     */
    fun reset(): Matcher {
        matcher.reset()
        return this
    }

    /**
     * Resets this matcher with a new input sequence
     *
     *
     * Resetting a matcher discards all of its explicit state information
     * and sets its append position to zero. The matcher's region is set to
     * the default region, which is its entire character sequence. The
     * anchoring and transparency of this matcher's region boundaries are
     * unaffected
     *
     * @param input The new input character sequence
     * @return this Matcher
     */
    fun reset(input: CharSequence?): Matcher {
        matcher.reset(input)
        return this
    }

    /**
     * Attempts to match the entire region against the pattern.
     *
     *
     * If the match succeeds then more information can be obtained via
     * the start, end, and group methods.
     *
     * @return `true` if, and only if, the entire region sequence
     * matches this matcher's pattern
     */
    fun matches() = matcher.matches()

    /**
     * Returns the match state of this matcher as a MatchResult. The result
     * is unaffected by subsequent operations performed upon this matcher.
     *
     * @return a MatchResult with the state of this matcher
     */
    fun toMatchResult() = Matcher(parentPattern, matcher)

    /**
     * Attempts to find the next subsequence of the input sequence that matches
     * the pattern.
     *
     *
     * This method starts at the beginning of this matcher's region, or,
     * if a previous invocation of the method was successful and the matcher
     * has not since been reset, at the first character not matched by the
     * previous match.
     *
     *
     * If the match succeeds then more information can be obtained via the
     * start, end, and group methods.
     *
     * @return <tt>true</tt> if, and only if, a subsequence of the input
     * sequence matches this matcher's pattern
     */
    fun find() = matcher.find()

    /**
     * Resets this matcher and then attempts to find the next subsequence of
     * the input sequence that matches the pattern, starting at the specified
     * index.
     *
     *
     * If the match succeeds then more information can be obtained via the
     * start, end, and group methods, and subsequent invocations of the find()
     * method will start at the first character not matched by this match.
     *
     * @param start the starting index
     * @return `true` if, and only if, a subsequence of the input
     * sequence starting at the given index matches this matcher's pattern
     * @throws IndexOutOfBoundsException If start is less than zero or if start
     * is greater than the length of the input sequence.
     */
    fun find(start: Int) = matcher.find(start)

    /**
     * Attempts to match the input sequence, starting at the beginning of the
     * region, against the pattern.
     *
     *
     * Like the matches method, this method always starts at the beginning
     * of the region; unlike that method, it does not require that the entire
     * region be matched.
     *
     *
     * If the match succeeds then more information can be obtained via the
     * start, end, and group methods.
     *
     * @return `true` if, and only if, a prefix of the input sequence
     * matches this matcher's pattern
     */
    fun lookingAt() = matcher.lookingAt()

    /**
     * Implements a non-terminal append-and-replace step.
     *
     * @param sb The target string buffer
     * @param replacement The replacement string
     * @return The target string buffer
     */
    fun appendReplacement(sb: StringBuffer?, replacement: String?): Matcher {
        matcher.appendReplacement(sb, parentPattern.replaceProperties(replacement))
        return this
    }

    /**
     * Implements a terminal append-and-replace step.
     *
     * @param sb The target string buffer
     * @return The target string buffer
     */
    fun appendTail(sb: StringBuffer?) = matcher.appendTail(sb)

    /**
     * Returns the input subsequence matched by the previous match.
     *
     * @return The (possibly empty) subsequence matched by the previous match,
     * in string form
     */
    override fun group() = matcher.group()

    /**
     * Returns the input subsequence captured by the given group during the
     * previous match operation.
     *
     * @param group The index of a capturing group in this matcher's pattern
     * @return the subsequence
     * @throws IllegalStateException If no match has yet been attempted, or
     * if the previous match operation failed
     */
    override fun group(group: Int) = matcher.group(group)

    /**
     * Returns the number of capturing groups in this matcher's pattern.
     *
     * @return The number of capturing groups in this matcher's pattern
     */
    override fun groupCount() = matcher.groupCount()

    /**
     * Gets a list of the matches in the order in which they occur
     * in a matching input string
     *
     * @return the matches
     */
    override fun orderedGroups(): List<String> {
        val groupCount = groupCount()
        val groups: MutableList<String> = ArrayList(groupCount)
        for (i in 1..groupCount) {
            groups.add(group(i))
        }
        return groups
    }

    /**
     * Returns the input subsequence captured by the named group during
     * the previous match operation.
     *
     * @param groupName name of the capture group
     * @return the subsequence
     * @throws IndexOutOfBoundsException if group name not found
     */
    override fun group(groupName: String): String {
        val idx = groupIndex(groupName)
        if (idx < 0) {
            throw IndexOutOfBoundsException("No group \"$groupName\"")
        }
        return group(idx)
    }

    /**
     * Finds all named groups that exist in the input string. This resets the
     * matcher and attempts to match the input against the pre-specified
     * pattern.
     *
     * @return a list of maps, each containing name-value matches
     * (empty if no match found).
     *
     * Example:
     * pattern:  (?&lt;dote&gt;\d+).(?&lt;day&gt;\w+)
     * input:    1 Sun foo bar 2 Mon foo
     * output:   [{"date":"1", "day":"Sun"}, {"date":"2", "day":"Mon"}]
     */
    override fun namedGroups(): List<Map<String?, String>> {
        val result: MutableList<Map<String?, String>> =
            ArrayList()
        val groupNames = parentPattern.groupNames()
        if (groupNames!!.isEmpty()) {
            return result
        }
        var nextIndex = 0
        while (matcher.find(nextIndex)) {
            val matches: MutableMap<String?, String> =
                LinkedHashMap()
            for (groupName in groupNames) {
                val groupValue = matcher.group(groupIndex(groupName))
                matches[groupName] = groupValue
                nextIndex = matcher.end()
            }
            result.add(matches)
        }
        return result
    }

    /**
     * Gets the index of a named capture group
     *
     * @param groupName name of capture group
     * @return the group index
     */
    private fun groupIndex(groupName: String?): Int {
        // idx+1 because capture groups start 1 in the matcher
        // while the pattern returns a 0-based index of the
        // group name within the list of names
        val idx = parentPattern.indexOf(groupName)
        return if (idx > -1) idx + 1 else -1
    }

    /**
     * Returns the start index of the previous match.
     *
     * @return the start index
     */
    override fun start() = matcher.start()

    /**
     * Returns the start index of the subsequence captured by the given
     * group during the previous match operation.
     *
     * @param group the index of the capture group
     * @return the index
     */
    override fun start(group: Int) = matcher.start(group)

    /**
     * Returns the start index of the subsequence captured by the given
     * named group during the previous match operation.
     *
     * @param groupName the name of the capture group
     * @return the index
     */
    override fun start(groupName: String?) = start(groupIndex(groupName))

    /**
     * Returns the offset after the last character matched.
     *
     * @return the offset
     */
    override fun end() = matcher.end()

    /**
     * Returns the offset after the last character of the subsequence
     * captured by the given group during the previous match operation.
     *
     * @param group the index of the capture group
     * @return the offset
     */
    override fun end(group: Int) = matcher.end(group)

    /**
     * Returns the offset after the last character of the subsequence
     * captured by the given named group during the previous match operation.
     *
     * @param groupName the name of the capture group
     * @return the offset
     */
    override fun end(groupName: String?) = end(groupIndex(groupName))

    /**
     * Sets the limits of this matcher's region.
     *
     * @param start The index to start searching at (inclusive)
     * @param end The index to end searching at (exclusive)
     * @return this Matcher
     */
    fun region(start: Int, end: Int): Matcher {
        matcher.region(start, end)
        return this
    }

    /**
     * Reports the end index (exclusive) of this matcher's region. The searches
     * this matcher conducts are limited to finding matches within regionStart
     * (inclusive) and regionEnd (exclusive).
     *
     * @return the ending point of this matcher's region
     */
    fun regionEnd() = matcher.regionEnd()

    /**
     * Reports the start index of this matcher's region. The searches this
     * matcher conducts are limited to finding matches within regionStart
     * (inclusive) and regionEnd (exclusive).
     *
     * @return The starting point of this matcher's region
     */
    fun regionStart() = matcher.regionStart()

    /**
     * Returns true if the end of input was hit by the search engine in the
     * last match operation performed by this matcher.
     *
     * @return true iff the end of input was hit in the last match; false otherwise
     */
    fun hitEnd() = matcher.hitEnd()

    /**
     * Returns true if more input could change a positive match into a negative one.
     *
     * @return true iff more input could change a positive match into a negative one.
     */
    fun requireEnd() = matcher.requireEnd()

    /**
     * Queries the anchoring of region bounds for this matcher.
     *
     * @return true iff this matcher is using anchoring bounds, false otherwise.
     */
    fun hasAnchoringBounds() = matcher.hasAnchoringBounds()

    /**
     * Queries the transparency of region bounds for this matcher.
     *
     * @return true iff this matcher is using transparent bounds, false otherwise
     */
    fun hasTransparentBounds() = matcher.hasTransparentBounds()

    /**
     * Replaces every subsequence of the input sequence that matches the pattern
     * with the given replacement string.
     *
     * @param replacement The replacement string
     * @return The string constructed by replacing each matching subsequence by
     * the replacement string, substituting captured subsequences as needed
     */
    fun replaceAll(replacement: String?): String {
        val r = parentPattern.replaceProperties(replacement)
        return matcher.replaceAll(r)
    }

    /**
     * Replaces the first subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     *
     * @param replacement The replacement string
     * @return The string constructed by replacing the first matching subsequence
     * by the replacement string, substituting captured subsequences as needed
     */
    fun replaceFirst(replacement: String?) =
        matcher.replaceFirst(parentPattern.replaceProperties(replacement))

    /**
     * Sets the anchoring of region bounds for this matcher.
     *
     * @param b a boolean indicating whether or not to use anchoring bounds.
     * @return this Matcher
     */
    fun useAnchoringBounds(b: Boolean): Matcher {
        matcher.useAnchoringBounds(b)
        return this
    }

    /**
     * Sets the transparency of region bounds for this matcher.
     *
     * @param b a boolean indicating whether to use opaque or transparent regions
     * @return this Matcher
     */
    fun useTransparentBounds(b: Boolean): Matcher {
        matcher.useTransparentBounds(b)
        return this
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is Matcher) {
            return false
        }
        val other = obj
        return if (parentPattern != other.parentPattern) {
            false
        } else matcher == other.matcher
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    override fun hashCode() = parentPattern.hashCode() xor matcher.hashCode()

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    override fun toString() = matcher.toString()
}