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

import java.io.Serializable
import java.util.*
import java.util.regex.PatternSyntaxException

/**
 * A compiled representation of a regular expression. This is a wrapper
 * for the java.util.regex.Pattern with support for named capturing
 * groups. The named groups are specified with "(?&lt;name&gt;exp)", which
 * is identical to Java 7 named groups.
 *
 * @since 0.1.9
 */
open class Pattern protected constructor(private val namedPattern: String, flags: Int) : Serializable {
    private val pattern: java.util.regex.Pattern
    private var groupNames: List<String?>? = null
    private val groupInfo: Map<String?, MutableList<GroupInfo>?>?

    /**
     * Gets the group index of a named capture group at the
     * specified index. If only one instance of the named
     * group exists, use index 0.
     *
     * @param groupName name of capture group
     * @param index the instance index of the named capture group within
     * the pattern; e.g., index is 2 for the third instance
     * @return group index or -1 if not found
     * @throws IndexOutOfBoundsException if instance index is out of bounds
     */
    /**
     * Gets the group index of a named capture group
     *
     * @param groupName name of capture group
     * @return group index or -1 if not found
     */
    @JvmOverloads
    fun indexOf(groupName: String?, index: Int = 0): Int {
        var idx = -1
        if (groupInfo!!.containsKey(groupName)) {
            val list: List<GroupInfo>? = groupInfo[groupName]
            idx = list!![index].groupIndex()
        }
        return idx
    }

    /**
     * Returns this pattern's match flags
     *
     * @return The match flags specified when this pattern was compiled
     */
    fun flags() = pattern.flags()

    /**
     * Creates a matcher that will match the given input against this pattern.
     *
     * @param input The character sequence to be matched
     * @return A new matcher for this pattern
     */
    fun matcher(input: CharSequence?) =  Matcher(this, input)

    /**
     * Returns the wrapped [java.util.regex.Pattern]
     * @return the pattern
     */
    fun pattern() = pattern

    /**
     * Returns the regular expression from which this pattern was compiled.
     *
     * @return The source of this pattern
     */
    fun standardPattern() = pattern.pattern()

    /**
     * Returns the original regular expression (including named groups)
     *
     * @return The regular expression
     */
    fun namedPattern() = namedPattern

    /**
     * Gets the names of all capture groups
     *
     * @return the list of names
     */
    fun groupNames(): List<String?> {
        if (groupNames == null) {
            groupNames = ArrayList(groupInfo!!.keys)
        }
        return Collections.unmodifiableList(groupNames)
    }

    /**
     * Gets the names and group info (group index and string position
     * within the named pattern) of all named capture groups
     *
     * @return a map of group names and their info
     */
    fun groupInfo() = Collections.unmodifiableMap<String, List<GroupInfo>>(groupInfo!!)

    /**
     * Replaces group-name properties (e.g., **`${named}`**) in
     * a replacement pattern with the equivalent reference that uses the
     * corresponding group index (e.g., **`$2`**). If the string
     * contains literal "$", it must be escaped with slash or else this call
     * will attempt to parse it as a group-name property.
     *
     * This is meant to be used to transform the parameter for:
     *
     *  * [Matcher.replaceAll]
     *  * [Matcher.replaceFirst]
     *  * [Matcher.appendReplacement]
     *
     * @param replacementPattern the input string to be evaluated
     * @return the modified string
     * @throws PatternSyntaxException group name was not found
     */
    fun replaceProperties(replacementPattern: String?): String {
        return replaceGroupNameWithIndex(
            StringBuilder(replacementPattern),
            PROPERTY_PATTERN,
            "$"
        ).toString()
    }

    /**
     * Splits the given input sequence around matches of this pattern.
     *
     *
     * The array returned by this method contains each substring of the
     * input sequence that is terminated by another subsequence that matches
     * this pattern or is terminated by the end of the input sequence. The
     * substrings in the array are in the order in which they occur in the
     * input. If this pattern does not match any subsequence of the input
     * then the resulting array has just one element, namely the input
     * sequence in string form.
     *
     *
     * The limit parameter controls the number of times the pattern is
     * applied and therefore affects the length of the resulting array. If
     * the limit n is greater than zero then the pattern will be applied
     * at most n - 1 times, the array's length will be no greater than n,
     * and the array's last entry will contain all input beyond the last
     * matched delimiter. If n is non-positive then the pattern will be
     * applied as many times as possible and the array can have any length.
     * If n is zero then the pattern will be applied as many times as
     * possible, the array can have any length, and trailing empty strings
     * will be discarded.
     *
     * @param input The character sequence to be split
     * @param limit The result threshold, as described above
     * @return The array of strings computed by splitting the input around
     * matches of this pattern
     */
    fun split(input: CharSequence?, limit: Int) = pattern.split(input, limit)

    /**
     * Splits the given input sequence around matches of this pattern.
     *
     * @param input The character sequence to be split
     * @return The array of strings computed by splitting the input around
     * matches of this pattern
     */
    fun split(input: CharSequence?) = pattern.split(input)

    /**
     * Returns a string representation of this pattern
     *
     * @return the string
     */
    override fun toString() = namedPattern

    /**
     * Replaces referenced group names with the reference to the corresponding group
     * index (e.g., **`\k<named>`**} to **`\k2`**};
     * **`${named}`** to **`$2`**}).
     * This assumes the group names have already been parsed from the pattern.
     *
     * @param input the string to evaluate
     * @param pattern the pattern that matches the string to be replaced
     * @param prefix string to prefix to the replacement (e.g., "$" or "\\")
     * @return the modified string (original instance of `input`)
     * @throws PatternSyntaxException group name was not found
     */
    private fun replaceGroupNameWithIndex(
        input: StringBuilder,
        pattern: java.util.regex.Pattern,
        prefix: String
    ): StringBuilder {
        val m = pattern.matcher(input)
        while (m.find()) {
            if (isEscapedChar(input.toString(), m.start())) {
                continue
            }
            var index = indexOf(m.group(INDEX_GROUP_NAME))
            if (index >= 0) {
                index++
            } else {
                throw PatternSyntaxException(
                    "unknown group name",
                    input.toString(),
                    m.start(INDEX_GROUP_NAME)
                )
            }

            // since we're replacing the original string being matched,
            // we have to reset the matcher so that it searches the new
            // string
            input.replace(m.start(), m.end(), prefix + index)
            m.reset(input)
        }
        return input
    }

    /**
     * Builds a `java.util.regex.Pattern` from a given regular expression
     * pattern (which may contain named groups) and flags
     *
     * @param namedPattern the expression to be compiled
     * @param flags Match flags, a bit mask that may include:
     *
     *  * [java.util.regex.Pattern.CASE_INSENSITIVE]
     *  * [java.util.regex.Pattern.MULTILINE]
     *  * [java.util.regex.Pattern.DOTALL]
     *  * [java.util.regex.Pattern.UNICODE_CASE]
     *  * [java.util.regex.Pattern.CANON_EQ]
     *  * [java.util.regex.Pattern.UNIX_LINES]
     *  * [java.util.regex.Pattern.LITERAL]
     *  * [java.util.regex.Pattern.COMMENTS]
     *
     * @return the standard `java.util.regex.Pattern`
     */
    private fun buildStandardPattern(namedPattern: String, flags: Int): java.util.regex.Pattern {
        // replace the named-group construct with left-paren but
        // make sure we're actually looking at the construct (ignore escapes)
        var s = StringBuilder(namedPattern)
        s = replace(
            s,
            NAMED_GROUP_PATTERN,
            "("
        )
        s = replaceGroupNameWithIndex(s, BACKREF_NAMED_GROUP_PATTERN, "\\")
        return java.util.regex.Pattern.compile(s.toString(), flags)
    }

    /**
     * Compares the keys and values of two group-info maps
     *
     * @param a the first map to compare
     * @param b the other map to compare
     * @return `true` if the first map contains all of the other map's keys and values; `false` otherwise
     */
    private fun groupInfoMatches(
        a: Map<String?, MutableList<GroupInfo>?>?,
        b: Map<String?, MutableList<GroupInfo>?>?
    ): Boolean {
        if (a == null && b == null) {
            return true
        }
        var isMatch = false
        if (a != null && b != null) {
            if (a.isEmpty() && b.isEmpty()) {
                isMatch = true
            } else if (a.size == b.size) {
                for ((key, thisList) in a) {
                    val otherList: List<GroupInfo>? = b[key]
                    isMatch = otherList != null
                    if (!isMatch) {
                        break
                    }
                    isMatch = otherList!!.containsAll(thisList!!) && thisList.containsAll(otherList)
                    if (!isMatch) {
                        break
                    }
                }
            }
        }
        return isMatch
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other !is Pattern) {
            return false
        }
        val groupNamesMatch = groupNames == null && other.groupNames == null ||
                groupNames != null && !Collections.disjoint(groupNames!!, other.groupNames!!)
        val groupInfoMatch = groupNamesMatch && groupInfoMatches(groupInfo, other.groupInfo)
        return (groupNamesMatch
                && groupInfoMatch
                && namedPattern == other.namedPattern && pattern.flags() == other.pattern.flags())
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    override fun hashCode(): Int {
        var hash = namedPattern.hashCode() xor pattern.hashCode()
        if (groupInfo != null) {
            hash = hash xor groupInfo.hashCode()
        }
        if (groupNames != null) {
            hash = hash xor groupNames.hashCode()
        }
        return hash
    }

    companion object {
        /**
         * Determines if a de-serialized file is compatible with this class.
         *
         * Maintainers must change this value if and only if the new version
         * of this class is not compatible with old versions. See Sun docs
         * for [](http://java.sun.com/products/jdk/1.1/docs/guide)
         * /serialization/spec/version.doc.html> details.
         *
         * Not necessary to include in first version of the class, but
         * included here as a reminder of its importance.
         */
        private const val serialVersionUID = 1L

        /** Pattern to match group names  */
        private const val NAME_PATTERN = "[^!=].*?"

        /** Pattern to match named capture groups in a pattern string  */
        private val NAMED_GROUP_PATTERN = java.util.regex.Pattern.compile(
            "\\(\\?<($NAME_PATTERN)>",
            java.util.regex.Pattern.DOTALL
        )

        /** Pattern to match back references for named capture groups  */
        private val BACKREF_NAMED_GROUP_PATTERN = java.util.regex.Pattern.compile(
            "\\\\k<($NAME_PATTERN)>",
            java.util.regex.Pattern.DOTALL
        )

        /** Pattern to match properties for named capture groups in a replacement string  */
        private val PROPERTY_PATTERN = java.util.regex.Pattern.compile(
            "\\$\\{($NAME_PATTERN)\\}",
            java.util.regex.Pattern.DOTALL
        )

        /** index of group within patterns above where group name is captured  */
        private const val INDEX_GROUP_NAME = 1

        /** [java.util.regex.Pattern.UNIX_LINES]  */
        const val UNIX_LINES = java.util.regex.Pattern.UNIX_LINES

        /** [java.util.regex.Pattern.CASE_INSENSITIVE]  */
        const val CASE_INSENSITIVE = java.util.regex.Pattern.CASE_INSENSITIVE

        /** [java.util.regex.Pattern.COMMENTS]  */
        const val COMMENTS = java.util.regex.Pattern.COMMENTS

        /** [java.util.regex.Pattern.MULTILINE]  */
        const val MULTILINE = java.util.regex.Pattern.MULTILINE

        /** [java.util.regex.Pattern.LITERAL]  */
        const val LITERAL = java.util.regex.Pattern.LITERAL

        /** [java.util.regex.Pattern.DOTALL]  */
        const val DOTALL = java.util.regex.Pattern.DOTALL

        /** [java.util.regex.Pattern.UNICODE_CASE]  */
        const val UNICODE_CASE = java.util.regex.Pattern.UNICODE_CASE

        /** [java.util.regex.Pattern.CANON_EQ]  */
        const val CANON_EQ = java.util.regex.Pattern.CANON_EQ

        /**
         * Compiles the given regular expression into a pattern
         *
         * @param regex the expression to be compiled
         * @return the pattern
         */
        fun compile(regex: String): Pattern {
            return Pattern(regex, 0)
        }

        /**
         * Compiles the given regular expression into a pattern with the given flags
         *
         * @param regex the expression to be compiled
         * @param flags Match flags, a bit mask that may include:
         *
         *  * [java.util.regex.Pattern.CASE_INSENSITIVE]
         *  * [java.util.regex.Pattern.MULTILINE]
         *  * [java.util.regex.Pattern.DOTALL]
         *  * [java.util.regex.Pattern.UNICODE_CASE]
         *  * [java.util.regex.Pattern.CANON_EQ]
         *  * [java.util.regex.Pattern.UNIX_LINES]
         *  * [java.util.regex.Pattern.LITERAL]
         *  * [java.util.regex.Pattern.COMMENTS]
         *
         * @return the pattern
         */
        @JvmStatic
        fun compile(regex: String, flags: Int): Pattern {
            return Pattern(regex, flags)
        }

        /**
         * Determines if the character at the specified position
         * of a string is escaped
         *
         * @param s string to evaluate
         * @param pos the position of the character to evaluate
         * @return true if the character is escaped; otherwise false
         */
        private fun isEscapedChar(s: String, pos: Int): Boolean {
            return isSlashEscapedChar(
                s,
                pos
            ) || isQuoteEscapedChar(s, pos)
        }

        /**
         * Determines if the character at the specified position
         * of a string is escaped with a backslash
         *
         * @param s string to evaluate
         * @param pos the position of the character to evaluate
         * @return true if the character is escaped; otherwise false
         */
        private fun isSlashEscapedChar(s: String, pos: Int): Boolean {

            // Count the backslashes preceding this position. If it's
            // even, there is no escape and the slashes are just literals.
            // If it's odd, one of the slashes (the last one) is escaping
            // the character at the given position.
            var pos = pos
            var numSlashes = 0
            while (pos > 0 && s[pos - 1] == '\\') {
                pos--
                numSlashes++
            }
            return numSlashes % 2 != 0
        }

        /**
         * Determines if the character at the specified position
         * of a string is quote-escaped (between \\Q and \\E)
         *
         * @param s string to evaluate
         * @param pos the position of the character to evaluate
         * @return true if the character is quote-escaped; otherwise false
         */
        private fun isQuoteEscapedChar(s: String, pos: Int): Boolean {
            var openQuoteFound = false
            var closeQuoteFound = false

            // find last non-escaped open-quote
            val s2 = s.substring(0, pos)
            var posOpen = pos
            while (s2.lastIndexOf("\\Q", posOpen - 1).also { posOpen = it } != -1) {
                if (!isSlashEscapedChar(s2, posOpen)) {
                    openQuoteFound = true
                    break
                }
            }
            if (openQuoteFound) {
                // search remainder of string (after open-quote) for a close-quote;
                // no need to check that it's slash-escaped because it can't be
                // (the escape character itself is part of the literal when quoted)
                if (s2.indexOf("\\E", posOpen) != -1) {
                    closeQuoteFound = true
                }
            }
            return openQuoteFound && !closeQuoteFound
        }

        /**
         * Determines if a string's character is within a regex character class
         *
         * @param s string to evaluate
         * @param pos the position of the character to evaluate
         * @return true if the character is inside a character class; otherwise false
         */
        private fun isInsideCharClass(s: String, pos: Int): Boolean {
            var openBracketFound = false
            var closeBracketFound = false

            // find last non-escaped open-bracket
            val s2 = s.substring(0, pos)
            var posOpen = pos
            while (s2.lastIndexOf('[', posOpen - 1).also { posOpen = it } != -1) {
                if (!isEscapedChar(s2, posOpen)) {
                    openBracketFound = true
                    break
                }
            }
            if (openBracketFound) {
                // search remainder of string (after open-bracket) for a close-bracket
                val s3 = s.substring(posOpen, pos)
                var posClose = -1
                while (s3.indexOf(']', posClose + 1).also { posClose = it } != -1) {
                    if (!isEscapedChar(s3, posClose)) {
                        closeBracketFound = true
                        break
                    }
                }
            }
            return openBracketFound && !closeBracketFound
        }

        /**
         * Determines if the parenthesis at the specified position
         * of a string is for a non-capturing group, which is one of
         * the flag specifiers (e.g., (?s) or (?m) or (?:pattern).
         * If the parenthesis is followed by "?", it must be a non-
         * capturing group unless it's a named group (which begins
         * with "?<"). Make sure not to confuse it with the lookbehind
         * construct ("?<=" or "?<!").
         *
         * @param s string to evaluate
         * @param pos the position of the parenthesis to evaluate
         * @return true if the parenthesis is non-capturing; otherwise false
         */
        private fun isNoncapturingParen(s: String, pos: Int): Boolean {

            //int len = s.length();
            var isLookbehind = false

            // code-coverage reports show that pos and the text to
            // check never exceed len in this class, so it's safe
            // to not test for it, which resolves uncovered branches
            // in Cobertura

            /*if (pos >= 0 && pos + 4 < len)*/run {
                val pre = s.substring(pos, pos + 4)
                isLookbehind = pre == "(?<=" || pre == "(?<!"
            }
            return  /*(pos >= 0 && pos + 2 < len) &&*/s[pos + 1] == '?' &&
                    (isLookbehind || s[pos + 2] != '<')
        }

        /**
         * Counts the open-parentheses to the left of a string position,
         * excluding escaped parentheses
         *
         * @param s string to evaluate
         * @param pos ending position of string; characters to the left
         * of this position are evaluated
         * @return number of open parentheses
         */
        private fun countOpenParens(s: String, pos: Int): Int {
            val p = java.util.regex.Pattern.compile("\\(")
            val m = p.matcher(s.subSequence(0, pos))
            var numParens = 0
            while (m.find()) {
                // ignore parentheses inside character classes: [0-9()a-f]
                // which are just literals
                if (isInsideCharClass(s, m.start())) {
                    continue
                }

                // ignore escaped parens
                if (isEscapedChar(s, m.start())) continue
                if (!isNoncapturingParen(s, m.start())) {
                    numParens++
                }
            }
            return numParens
        }

        /**
         * Parses info on named capture groups from a pattern
         *
         * @param namedPattern regex the regular expression pattern to parse
         * @return list of group info for all named groups
         */
        fun extractGroupInfo(namedPattern: String): Map<String?, MutableList<GroupInfo>?> {
            val groupInfo: MutableMap<String?, MutableList<GroupInfo>?> =
                LinkedHashMap()
            val matcher =
                NAMED_GROUP_PATTERN.matcher(namedPattern)
            while (matcher.find()) {
                val pos = matcher.start()

                // ignore escaped paren
                if (isEscapedChar(namedPattern, pos)) continue
                val name = matcher.group(INDEX_GROUP_NAME)
                val groupIndex = countOpenParens(namedPattern, pos)
                var list: MutableList<GroupInfo>?
                list = if (groupInfo.containsKey(name)) {
                    groupInfo[name]
                } else {
                    ArrayList()
                }
                list!!.add(GroupInfo(groupIndex, pos))
                groupInfo[name] = list
            }
            return groupInfo
        }

        /**
         * Replaces strings matching a pattern with another string. If the string
         * to be replaced is escaped with a slash, it is skipped.
         *
         * @param input the string to evaluate
         * @param pattern the pattern that matches the string to be replaced
         * @param replacement the string to replace the target
         * @return the modified string (original instance of `input`)
         */
        private fun replace(
            input: StringBuilder,
            pattern: java.util.regex.Pattern,
            replacement: String
        ): StringBuilder {
            val m = pattern.matcher(input)
            while (m.find()) {
                if (isEscapedChar(input.toString(), m.start())) {
                    continue
                }

                // since we're replacing the original string being matched,
                // we have to reset the matcher so that it searches the new
                // string
                input.replace(m.start(), m.end(), replacement)
                m.reset(input)
            }
            return input
        }
    }

    /**
     * Constructs a named pattern with the given regular expression and flags
     *
     * @param regex the expression to be compiled
     * @param flags Match flags, a bit mask that may include:
     *
     *  * [java.util.regex.Pattern.CASE_INSENSITIVE]
     *  * [java.util.regex.Pattern.MULTILINE]
     *  * [java.util.regex.Pattern.DOTALL]
     *  * [java.util.regex.Pattern.UNICODE_CASE]
     *  * [java.util.regex.Pattern.CANON_EQ]
     *  * [java.util.regex.Pattern.UNIX_LINES]
     *  * [java.util.regex.Pattern.LITERAL]
     *  * [java.util.regex.Pattern.COMMENTS]
     *
     */
    init {

        // group info must be parsed before building the standard pattern
        // because the pattern relies on group info to determine the indexes
        // of named back-references
        groupInfo = extractGroupInfo(namedPattern)
        pattern = buildStandardPattern(namedPattern, flags)
    }
}