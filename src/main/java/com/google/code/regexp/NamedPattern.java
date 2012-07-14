package com.google.code.regexp;

import java.util.ArrayList;
import java.util.List;
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
	private int numUnnamedGroups;

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
    	groupNames = extractGroupNames(regex);
    	numUnnamedGroups = countUnnamedGroups();
	}

    /**
     * Gets the group index of a named capture group
     * 
     * @param groupName name of capture group
     * @return group index or -1 if not found
     */
    public int indexOf(String groupName) {
    	int idx = groupNames.indexOf(groupName);
    	return idx > -1 ? numUnnamedGroups + idx : -1;
    }
    
    /**
     * Counts the unnamed capture groups in the named pattern. That is,
     * all groups that do not begin with "(?<" and "(?:".
     * 
     * @return the number of unnamed capture groups
     */
    private int countUnnamedGroups() {
    	// include unnamed capture groups, exclude non-capturing groups
		String patt = namedPattern;
		int numGroups = 0;
		int sz = patt.length();
		int i = -1;
		
		// count all the left-parens without "?<" or "?:"
		while ((i = patt.indexOf("(", i+1)) >= 0) {
			if (i < sz - 3) {
				// ignore named groups and non-capturing groups
				String next2 = patt.substring(i+1, i+3); 
				if (next2.equals("?<") || next2.equals("?:")) {
					continue;
				}
			}
			if (i >= sz - 1) break;
			
			numGroups++;
		}
		return numGroups;
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
		return groupNames;
	}

	/**
	 * Gets the number of unnamed capture groups in this pattern
	 * 
	 * @return the count
	 */
	public int unnamedGroupCount() {
		return numUnnamedGroups;
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
	 * Parses the group names from a pattern
	 * 
	 * @param namedPattern regex the regular expression pattern to parse
	 * @return the group names
	 */
	static List<String> extractGroupNames(String namedPattern) {
		List<String> groupNames = new ArrayList<String>();
		Matcher matcher = NAMED_GROUP_PATTERN.matcher(namedPattern);
		while(matcher.find()) {
			groupNames.add(matcher.group(1));
		}
		return groupNames;
	}

	/**
	 * Constructs a named pattern with the given regular expression and flags
	 * 
	 * @param namedPattern the expression to be compiled
     * @param flags Match flags, a bit mask that may include CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, CANON_EQ, UNIX_LINES, LITERAL and COMMENTS 
	 * @return
	 */
	static Pattern buildStandardPattern(String namedPattern, int flags) {
		return Pattern.compile(NAMED_GROUP_PATTERN.matcher(namedPattern).replaceAll("("), flags);
	}

}
