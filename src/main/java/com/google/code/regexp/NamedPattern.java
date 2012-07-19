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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A compiled representation of a regular expression. This is a wrapper 
 * for the java.util.regex.Pattern with support for named capturing 
 * groups. The named groups are specified with "(?&lt;name>exp)", which
 * is identical to Java 7 named groups.
 */
public class NamedPattern {

	private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<(\\w+)>");

	private Pattern pattern;
	private String namedPattern;
	private List<String> groupNames;
	private Map<String,List<GroupInfo>> groupInfo;

	/**
	 * Compiles the given regular expression into a pattern
	 * 
	 * @param regex the expression to be compiled
	 * @return the pattern
	 */
    public static NamedPattern compile(String regex) {
        return new NamedPattern(regex, 0);
    }

    /**
     * Compiles the given regular expression into a pattern with the given flags
     * 
     * @param regex the expression to be compiled
     * @param flags Match flags, a bit mask that may include CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, CANON_EQ, UNIX_LINES, LITERAL and COMMENTS
     * @return the pattern
     */
    public static NamedPattern compile(String regex, int flags) {
        return new NamedPattern(regex, flags);
    }

    /**
     * Constructs a named pattern with the given regular expression and flags
     *  
     * @param regex the expression to be compiled
     * @param flags Match flags, a bit mask that may include CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, CANON_EQ, UNIX_LINES, LITERAL and COMMENTS
     */
    private NamedPattern(String regex, int flags) {
    	namedPattern = regex;
    	pattern = buildStandardPattern(regex, flags);
    	groupInfo = extractGroupInfo(regex);
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
     * @param index the index of the named capture group within 
     * the pattern (if more than one instance)
     * @return group index or -1 if not found
     */
    public int indexOf(String groupName, int index) {
    	int idx = -1;
    	if (groupInfo.containsKey(groupName)) {
    		List<GroupInfo> list = groupInfo.get(groupName);
    		if (index < list.size()) {
    			idx = list.get(index).groupIndex();
    		}
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
	public NamedMatcher matcher(CharSequence input) {
		return new NamedMatcher(this, input);
	}

	/**
	 * Returns the wrapped {@link java.util.regex.Pattern}
	 * @return the pattern
	 */
	public Pattern pattern() {
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
		return groupNames;
	}
	
	/**
	 * Gets the names and group info (group index and string position 
	 * within the named pattern) of all named capture groups
	 * 
	 * @return a map of group names and their info
	 */
	public Map<String, List<GroupInfo>> groupInfo() {
		return groupInfo;
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
	 * @return The array of strings computed by splitting the input around matches of this pattern
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
	 * Determines if the parenthesis at the specified position
	 * of a string is escaped with a backslash
	 * 
	 * @param s string to evaluate
	 * @param pos the position of the parenthesis to evaluate
	 * @return true if the parenthesis is escaped; otherwise false
	 */
	static private boolean isEscapedParen(String s, int pos) {
		
		// Count the backslashes preceding this position. If it's
		// even, there is no escape and the slashes are just literals.
		// If it's odd, one of the slashes (the last one) is escaping
		// the parenthesis at the given position.
		int numSlashes = 0;
		while (pos > 0 && (s.charAt(pos - 1) == '\\')) {
			pos--;
			numSlashes++;
		}
		return numSlashes % 2 != 0;
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
	static private boolean isNoncapturingParen(String s, int pos) {
		int len = s.length();
		boolean isLookbehind = false;
		if (pos >= 0 && pos + 4 < len) {
			String pre = s.substring(pos, pos+4);
			isLookbehind = pre.equals("(?<=") || pre.equals("(?<!");
		}
		return (pos >= 0 && pos + 2 < len) &&
				s.charAt(pos + 1) == '?' &&
				(isLookbehind || s.charAt(pos + 2) != '<');
	}
	
	/**
	 * Counts the open-parentheses to the left of a string position,
	 * excluding escaped parentheses
	 * 
	 * @param s string to evaluate
	 * @param pos ending position of string
	 * @return number of open parentheses 
	 */
	static private int countOpenParens(String s, int pos) {
		Pattern p = Pattern.compile("\\(");
		Matcher m = p.matcher(s.subSequence(0, pos));
		
		int numParens = 0;
		
		while (m.find()) {
			String match = m.group(0);
			
			// ignore escaped parens
			if (isEscapedParen(s, m.start())) continue;
			
			if (match.equals("(") && !isNoncapturingParen(s, m.start())) {
				numParens++;
			}
		}
		return numParens;
	}
	
	/**
	 * Parses info on named capture groups from a pattern
	 * 
	 * @param namedPattern regex the regular expression pattern to parse
	 * @return list of group info for all named groups
	 */
	static public Map<String,List<GroupInfo>> extractGroupInfo(String namedPattern) {
		Map<String,List<GroupInfo>> groupInfo = new LinkedHashMap<String,List<GroupInfo>>();
		Matcher matcher = NAMED_GROUP_PATTERN.matcher(namedPattern);
		while(matcher.find()) {
			
			int pos = matcher.start();
			
			// ignore escaped paren
			if (isEscapedParen(namedPattern, pos)) continue;
			
			String name = matcher.group(1);
			int groupIndex = countOpenParens(namedPattern, pos);
			
			List<GroupInfo> list;
			if (groupInfo.containsKey(name)) {
				list = groupInfo.get(name);
			} else {
				list = new ArrayList<GroupInfo>();
			}
			list.add(new GroupInfo(groupIndex, pos));
			groupInfo.put(name, list);
		}
		return groupInfo;
	}

	/**
	 * Constructs a named pattern with the given regular expression and flags
	 * 
	 * @param namedPattern the expression to be compiled
     * @param flags Match flags, a bit mask that may include CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, CANON_EQ, UNIX_LINES, LITERAL and COMMENTS 
	 * @return
	 */
	static private Pattern buildStandardPattern(String namedPattern, Integer flags) {
		
		// replace the named-group construct with left-paren but
		// make sure we're actually looking at the construct (ignore escapes)
		StringBuilder s = new StringBuilder(namedPattern);
		Matcher m = NAMED_GROUP_PATTERN.matcher(s);
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			
			if (isEscapedParen(s.toString(), start)) {
				continue;
			}
			
			// since we're replacing the original string being matched,
			// we have to reset the matcher so that it searches the new
			// string
			s.replace(start, end, "(");
			m.reset();
		}
		
		return Pattern.compile(s.toString(), flags);
	}

}

