/**
 * Copyright (C) 2026 The named-regexp Authors
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

/**
 * A compiled representation of a regular expression. This is a wrapper
 * for the java.util.regex.Pattern with support for named capturing
 * groups. The named groups are specified with "(?&lt;name&gt;exp)", which
 * is identical to Java 7 named groups.
 *
 * @since 0.1.9
 */
public class Pattern implements Serializable {

    /**
     * Determines if a de-serialized file is compatible with this class.
     *
     * Maintainers must change this value if and only if the new version
     * of this class is not compatible with old versions. See Sun docs
     * for <a href=http://java.sun.com/products/jdk/1.1/docs/guide
     * /serialization/spec/version.doc.html> details. </a>
     *
     * Not necessary to include in first version of the class, but
     * included here as a reminder of its importance.
     */
    private static final long serialVersionUID = 1L;

    /** {@link java.util.regex.Pattern#UNIX_LINES} */
    public static final int UNIX_LINES = java.util.regex.Pattern.UNIX_LINES;

    /** {@link java.util.regex.Pattern#CASE_INSENSITIVE} */
    public static final int CASE_INSENSITIVE = java.util.regex.Pattern.CASE_INSENSITIVE;

    /** {@link java.util.regex.Pattern#COMMENTS} */
    public static final int COMMENTS = java.util.regex.Pattern.COMMENTS;

    /** {@link java.util.regex.Pattern#MULTILINE} */
    public static final int MULTILINE = java.util.regex.Pattern.MULTILINE;

    /** {@link java.util.regex.Pattern#LITERAL} */
    public static final int LITERAL = java.util.regex.Pattern.LITERAL;

    /** {@link java.util.regex.Pattern#DOTALL} */
    public static final int DOTALL = java.util.regex.Pattern.DOTALL;

    /** {@link java.util.regex.Pattern#UNICODE_CASE} */
    public static final int UNICODE_CASE = java.util.regex.Pattern.UNICODE_CASE;

    /** {@link java.util.regex.Pattern#CANON_EQ} */
    public static final int CANON_EQ = java.util.regex.Pattern.CANON_EQ;

    private java.util.regex.Pattern pattern;
    private String namedPattern;
    private List<String> groupNames;
    private Map<String,List<GroupInfo> > groupInfo;

    /**
     * Constructs a named pattern with the given regular expression and flags
     *
     * @param regex the expression to be compiled
     * @param flags Match flags, a bit mask that may include:
     * <ul>
     *   <li>{@link java.util.regex.Pattern#CASE_INSENSITIVE}</li>
     *   <li>{@link java.util.regex.Pattern#MULTILINE}</li>
     *   <li>{@link java.util.regex.Pattern#DOTALL}</li>
     *   <li>{@link java.util.regex.Pattern#UNICODE_CASE}</li>
     *   <li>{@link java.util.regex.Pattern#CANON_EQ}</li>
     *   <li>{@link java.util.regex.Pattern#UNIX_LINES}</li>
     *   <li>{@link java.util.regex.Pattern#LITERAL}</li>
     *   <li>{@link java.util.regex.Pattern#COMMENTS}</li>
     * </ul>
     */
    protected Pattern(String regex, int flags) {
        namedPattern = regex;

        // group info must be parsed before building the standard pattern
        // because the pattern relies on group info to determine the indexes
        // of named back-references
        groupInfo = extractGroupInfo(regex);
        pattern = buildStandardPattern(regex, flags);
    }

    /**
     * Compiles the given regular expression into a pattern
     *
     * @param regex the expression to be compiled
     * @return the pattern
     */
    public static Pattern compile(String regex) {
        return new Pattern(regex, 0);
    }

    /**
     * Compiles the given regular expression into a pattern with the given flags
     *
     * @param regex the expression to be compiled
     * @param flags Match flags, a bit mask that may include:
     * <ul>
     *   <li>{@link java.util.regex.Pattern#CASE_INSENSITIVE}</li>
     *   <li>{@link java.util.regex.Pattern#MULTILINE}</li>
     *   <li>{@link java.util.regex.Pattern#DOTALL}</li>
     *   <li>{@link java.util.regex.Pattern#UNICODE_CASE}</li>
     *   <li>{@link java.util.regex.Pattern#CANON_EQ}</li>
     *   <li>{@link java.util.regex.Pattern#UNIX_LINES}</li>
     *   <li>{@link java.util.regex.Pattern#LITERAL}</li>
     *   <li>{@link java.util.regex.Pattern#COMMENTS}</li>
     * </ul>
     * @return the pattern
     */
    public static Pattern compile(String regex, int flags) {
        return new Pattern(regex, flags);
    }

    /**
     * Gets the group index of a named capture group
     *
     * @param groupName name of capture group
     * @return group index or -1 if not found
     */
    public int indexOf(String groupName) {
        return indexOf(groupName, 0);
    }

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
    public int indexOf(String groupName, int index) {
        int idx = -1;
        if (groupInfo.containsKey(groupName)) {
            List<GroupInfo> list = groupInfo.get(groupName);
            idx = list.get(index).groupIndex();
        }
        return idx;
    }

    /**
     * Returns this pattern's match flags
     *
     * @return The match flags specified when this pattern was compiled
     */
    public int flags() {
        return pattern.flags();
    }

    /**
     * Creates a matcher that will match the given input against this pattern.
     *
     * @param input The character sequence to be matched
     * @return A new matcher for this pattern
     */
    public Matcher matcher(CharSequence input) {
        return new Matcher(this, input);
    }

    /**
     * Returns the wrapped {@link java.util.regex.Pattern}
     * @return the pattern
     */
    public java.util.regex.Pattern pattern() {
        return pattern;
    }

    /**
     * Returns the regular expression from which this pattern was compiled.
     *
     * @return The source of this pattern
     */
    public String standardPattern() {
        return pattern.pattern();
    }

    /**
     * Returns the original regular expression (including named groups)
     *
     * @return The regular expression
     */
    public String namedPattern() {
        return namedPattern;
    }

    /**
     * Gets the names of all capture groups
     *
     * @return the list of names
     */
    public List<String> groupNames() {
        if (groupNames == null) {
            groupNames = new ArrayList<String>(groupInfo.keySet());
        }
        return Collections.unmodifiableList(groupNames);
    }

    /**
     * Gets the names and group info (group index and string position
     * within the named pattern) of all named capture groups
     *
     * @return a map of group names and their info
     */
    public Map<String, List<GroupInfo> > groupInfo() {
        return Collections.unmodifiableMap(groupInfo);
    }

    /**
     * Replaces group-name properties (e.g., <b><code>${named}</code></b>) in
     * a replacement pattern with the equivalent reference that uses the
     * corresponding group index (e.g., <b><code>$2</code></b>). If the string
     * contains literal "$", it must be escaped with slash or else this call
     * will attempt to parse it as a group-name property.
     *
     * This is meant to be used to transform the parameter for:
     *  <ul>
     *  <li>{@link Matcher#replaceAll(String)}</li>
     *  <li>{@link Matcher#replaceFirst(String)}</li>
     *  <li>{@link Matcher#appendReplacement(StringBuffer, String)}</li>
     *  </ul>
     * @param replacementPattern the input string to be evaluated
     * @return the modified string
     * @throws PatternSyntaxException group name was not found
     */
    public String replaceProperties(String replacementPattern) {
        return replaceGroupNameWithIndex(replacementPattern, "${", '}', "$");
    }

    /**
     * Splits the given input sequence around matches of this pattern.
     *
     * <p>The array returned by this method contains each substring of the
     * input sequence that is terminated by another subsequence that matches
     * this pattern or is terminated by the end of the input sequence. The
     * substrings in the array are in the order in which they occur in the
     * input. If this pattern does not match any subsequence of the input
     * then the resulting array has just one element, namely the input
     * sequence in string form.</p>
     *
     * <p>The limit parameter controls the number of times the pattern is
     * applied and therefore affects the length of the resulting array. If
     * the limit n is greater than zero then the pattern will be applied
     * at most n - 1 times, the array's length will be no greater than n,
     * and the array's last entry will contain all input beyond the last
     * matched delimiter. If n is non-positive then the pattern will be
     * applied as many times as possible and the array can have any length.
     * If n is zero then the pattern will be applied as many times as
     * possible, the array can have any length, and trailing empty strings
     * will be discarded.</p>
     *
     * @param input The character sequence to be split
     * @param limit The result threshold, as described above
     * @return The array of strings computed by splitting the input around
     * matches of this pattern
     */
    public String[] split(CharSequence input, int limit) {
        return pattern.split(input, limit);
    }

    /**
     * Splits the given input sequence around matches of this pattern.
     *
     * @param input The character sequence to be split
     * @return The array of strings computed by splitting the input around
     * matches of this pattern
     */
    public String[] split(CharSequence input) {
        return pattern.split(input);
    }

    /**
     * Returns a string representation of this pattern
     *
     * @return the string
     */
    public String toString() {
        return namedPattern;
    }

    /**
     * Pre-computed, per-character syntax info for a string. Building it
     * takes O(n) time and every lookup is O(1), so parsing named groups
     * stays linear in the length of the input. (Before this, every lookup
     * rescanned the string, and compiling a pattern with many groups took
     * cubic time, which allowed denial of service.)
     */
    static private final class SyntaxInfo {
        /** true if the character at the index is escaped (by a backslash or \Q..\E) */
        final boolean[] escaped;
        /** number of capturing groups opened before the index */
        final int[] capturingParensBefore;
        /** index of the first '&gt;' at or after the index; -1 if none */
        final int[] nextGt;
        /** index of the first '}' at or after the index; -1 if none */
        final int[] nextCloseBrace;

        SyntaxInfo(String s) {
            final int len = s.length();
            escaped = new boolean[len];
            capturingParensBefore = new int[len + 1];
            nextGt = new int[len + 1];
            nextCloseBrace = new int[len + 1];

            boolean[] slashEscaped = new boolean[len];
            int slashRun = 0;          // number of consecutive backslashes right before i
            int lastOpenQuote = -1;    // start of last unescaped "\Q" that ends at or before i
            int lastCloseQuote = -1;   // start of last "\E" that ends at or before i
            int lastOpenBracket = -1;  // last unescaped '[' before i
            int lastCloseBracket = -1; // last unescaped ']' before i
            int parens = 0;

            for (int i = 0; i < len; i++) {
                // account for a "\Q" or "\E" that ends right before i
                if (i >= 2 && s.charAt(i - 2) == '\\') {
                    char c = s.charAt(i - 1);
                    if (c == 'Q' && !slashEscaped[i - 2]) {
                        lastOpenQuote = i - 2;
                    } else if (c == 'E') {
                        lastCloseQuote = i - 2;
                    }
                }

                // An odd number of preceding backslashes means the last one
                // escapes this character. Inside \Q..\E, everything is literal.
                slashEscaped[i] = slashRun % 2 != 0;
                boolean quoteEscaped = lastOpenQuote >= 0 && lastCloseQuote < lastOpenQuote;
                escaped[i] = slashEscaped[i] || quoteEscaped;
                boolean insideCharClass = lastOpenBracket >= 0 && lastCloseBracket < lastOpenBracket;

                char ch = s.charAt(i);
                if (!escaped[i]) {
                    if (ch == '[') {
                        lastOpenBracket = i;
                    } else if (ch == ']') {
                        lastCloseBracket = i;
                    }
                }

                capturingParensBefore[i] = parens;
                // ignore parentheses inside character classes (e.g., [0-9()a-f]),
                // which are literals; also ignore escaped and non-capturing parens
                if (ch == '(' && !insideCharClass && !escaped[i] && !isNoncapturingParen(s, i)) {
                    parens++;
                }

                slashRun = (ch == '\\') ? slashRun + 1 : 0;
            }
            capturingParensBefore[len] = parens;

            int gt = -1;
            int brace = -1;
            nextGt[len] = -1;
            nextCloseBrace[len] = -1;
            for (int i = len - 1; i >= 0; i--) {
                char ch = s.charAt(i);
                if (ch == '>') gt = i;
                if (ch == '}') brace = i;
                nextGt[i] = gt;
                nextCloseBrace[i] = brace;
            }
        }
    }

    /**
     * Determines if the parenthesis at the specified position
     * of a string is for a non-capturing group, which is one of
     * the flag specifiers (e.g., (?s) or (?m) or (?:pattern).
     * If the parenthesis is followed by "?", it must be a non-
     * capturing group unless it's a named group (which begins
     * with "?&lt;"). Make sure not to confuse it with the lookbehind
     * construct ("?&lt;=" or "?&lt;!").
     *
     * @param s string to evaluate
     * @param pos the position of the parenthesis to evaluate
     * @return true if the parenthesis is non-capturing; otherwise false
     */
    static private boolean isNoncapturingParen(String s, int pos) {
        final int len = s.length();
        if (pos + 1 >= len || s.charAt(pos + 1) != '?') {
            return false;
        }
        if (pos + 2 >= len || s.charAt(pos + 2) != '<') {
            return true;
        }
        // "(?<=" and "(?<!" are lookbehinds; anything else is a named group
        return pos + 3 < len && (s.charAt(pos + 3) == '=' || s.charAt(pos + 3) == '!');
    }

    /**
     * Finds all non-overlapping occurrences of a named construct in a
     * string, consisting of a prefix (e.g., "(?&lt;"), a name, and a closing
     * character (e.g., '&gt;'). The name is at least one character long, and
     * its first character is not '!' or '='. This is equivalent to finding
     * the regular expression {@code PREFIX([^!=].*?)CLOSER} (with DOTALL),
     * but runs in linear time.
     *
     * @param s string to evaluate
     * @param prefix the prefix of the construct
     * @param nextCloser lookup table of the next position of the closing character
     * @return list of {start of construct, start of name, position of closer}
     */
    static private List<int[]> findNamedConstructs(String s, String prefix, int[] nextCloser) {
        List<int[]> matches = new ArrayList<int[]>();
        final int len = s.length();
        int p = s.indexOf(prefix);
        while (p != -1) {
            int nameStart = p + prefix.length();
            int closer = -1;
            if (nameStart < len) {
                char c = s.charAt(nameStart);
                if (c != '!' && c != '=') {
                    closer = nextCloser[nameStart + 1];
                }
            }
            if (closer == -1) {
                p = s.indexOf(prefix, p + 1);
            } else {
                matches.add(new int[] { p, nameStart, closer });
                p = s.indexOf(prefix, closer + 1);
            }
        }
        return matches;
    }

    /**
     * Parses info on named capture groups from a pattern
     *
     * @param namedPattern regex the regular expression pattern to parse
     * @return list of group info for all named groups
     */
    static public Map<String,List<GroupInfo> > extractGroupInfo(String namedPattern) {
        Map<String,List<GroupInfo> > groupInfo = new LinkedHashMap<String,List<GroupInfo> >();
        SyntaxInfo info = new SyntaxInfo(namedPattern);
        for (int[] m : findNamedConstructs(namedPattern, "(?<", info.nextGt)) {
            int pos = m[0];

            // ignore escaped paren
            if (info.escaped[pos]) continue;

            String name = namedPattern.substring(m[1], m[2]);
            int groupIndex = info.capturingParensBefore[pos];

            List<GroupInfo> list = groupInfo.get(name);
            if (list == null) {
                list = new ArrayList<GroupInfo>();
                groupInfo.put(name, list);
            }
            list.add(new GroupInfo(groupIndex, pos));
        }
        return groupInfo;
    }

    /**
     * Replaces referenced group names with the reference to the corresponding group
     * index (e.g., <b><code>\k&lt;named&gt;</code></b>} to <b><code>\k2</code></b>};
     * <b><code>${named}</code></b> to <b><code>$2</code></b>}). Escaped references
     * are skipped. This assumes the group names have already been parsed from the
     * pattern.
     *
     * @param input the string to evaluate
     * @param namePrefix the prefix of the reference (e.g., "\\k&lt;" or "${")
     * @param closeChar the character that ends the reference (e.g., '&gt;' or '}')
     * @param prefix string to prefix to the replacement (e.g., "$" or "\\")
     * @return the modified string
     * @throws PatternSyntaxException group name was not found
     */
    private String replaceGroupNameWithIndex(String input, String namePrefix, char closeChar, String prefix) {
        SyntaxInfo info = new SyntaxInfo(input);
        int[] nextCloser = closeChar == '>' ? info.nextGt : info.nextCloseBrace;
        StringBuilder out = new StringBuilder(input.length());
        int last = 0;
        for (int[] m : findNamedConstructs(input, namePrefix, nextCloser)) {
            if (info.escaped[m[0]]) {
                continue;
            }

            int index = indexOf(input.substring(m[1], m[2]));
            if (index >= 0) {
                index++;
            } else {
                out.append(input, last, m[0]);
                throw new PatternSyntaxException("unknown group name",
                        out.toString() + input.substring(m[0]),
                        out.length() + (m[1] - m[0]));
            }

            out.append(input, last, m[0]).append(prefix).append(index);
            last = m[2] + 1;
        }
        out.append(input, last, input.length());
        return out.toString();
    }

    /**
     * Builds a {@code java.util.regex.Pattern} from a given regular expression
     * pattern (which may contain named groups) and flags
     *
     * @param namedPattern the expression to be compiled
     * @param flags Match flags, a bit mask that may include:
     * <ul>
     *   <li>{@link java.util.regex.Pattern#CASE_INSENSITIVE}</li>
     *   <li>{@link java.util.regex.Pattern#MULTILINE}</li>
     *   <li>{@link java.util.regex.Pattern#DOTALL}</li>
     *   <li>{@link java.util.regex.Pattern#UNICODE_CASE}</li>
     *   <li>{@link java.util.regex.Pattern#CANON_EQ}</li>
     *   <li>{@link java.util.regex.Pattern#UNIX_LINES}</li>
     *   <li>{@link java.util.regex.Pattern#LITERAL}</li>
     *   <li>{@link java.util.regex.Pattern#COMMENTS}</li>
     * </ul>
     * @return the standard {@code java.util.regex.Pattern}
     */
    private java.util.regex.Pattern buildStandardPattern(String namedPattern, Integer flags) {
        // replace the named-group construct with left-paren but
        // make sure we're actually looking at the construct (ignore escapes)
        SyntaxInfo info = new SyntaxInfo(namedPattern);
        StringBuilder s = new StringBuilder(namedPattern.length());
        int last = 0;
        for (int[] m : findNamedConstructs(namedPattern, "(?<", info.nextGt)) {
            if (info.escaped[m[0]]) {
                continue;
            }
            s.append(namedPattern, last, m[0]).append('(');
            last = m[2] + 1;
        }
        s.append(namedPattern, last, namedPattern.length());

        String standard = replaceGroupNameWithIndex(s.toString(), "\\k<", '>', "\\");
        return java.util.regex.Pattern.compile(standard, flags);
    }

    /**
     * Compares the keys and values of two group-info maps
     *
     * @param a the first map to compare
     * @param b the other map to compare
     * @return {@code true} if the first map contains all of the other map's keys and values; {@code false} otherwise
     */
    private boolean groupInfoMatches(Map<String, List<GroupInfo>> a, Map<String, List<GroupInfo>> b) {
        if (a == null && b == null) {
            return true;
        }

        boolean isMatch = false;
        if (a != null && b != null) {
            if (a.isEmpty() && b.isEmpty()) {
                isMatch = true;
            } else if (a.size() == b.size()) {
                for (Entry<String, List<GroupInfo>> entry : a.entrySet()) {
                    List<GroupInfo> otherList = b.get(entry.getKey());
                    isMatch = (otherList != null);
                    if (!isMatch) {
                        break;
                    }

                    List<GroupInfo> thisList = entry.getValue();
                    isMatch = otherList.containsAll(thisList) && thisList.containsAll(otherList);
                    if (!isMatch) {
                        break;
                    }
                }
            }
        }
        return isMatch;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Pattern)) {
            return false;
        }
        Pattern other = (Pattern)obj;

        boolean groupNamesMatch = (groupNames == null && other.groupNames == null) ||
                                  (groupNames != null && !Collections.disjoint(groupNames, other.groupNames));
        boolean groupInfoMatch = groupNamesMatch && groupInfoMatches(groupInfo, other.groupInfo);

        return groupNamesMatch
            && groupInfoMatch
            && namedPattern.equals(other.namedPattern)
            && pattern.flags() == other.pattern.flags()
            ;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = namedPattern.hashCode() ^ pattern.hashCode();
        if (groupInfo != null) {
            hash ^= groupInfo.hashCode();
        }
        if (groupNames != null) {
            hash ^= groupNames.hashCode();
        }
        return hash;
    }

}

