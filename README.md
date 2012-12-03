named-regexp 0.1.9 [![Build Status](https://tony19.ci.cloudbees.com/job/named-regexp/badge/icon)](https://tony19.ci.cloudbees.com/job/named-regexp/)
==================
This library is a thin wrapper for `java.util.regex`, implementing named capture groups for Java 5/6. Note this isn't needed in Java 7, which already supports this feature.

This is a fork of the [named-regexp][1] project, including several bug fixes.

### How is this different from the original project?
 1. The files are fully commented.
 2. The bugs reported in the original project (and unreported ones) are [fixed][2].
 3. This library is in Maven Central.

Please report any issues in [JIRA][2].

Usage
=====

_Maven dependency_

<pre>
&lt;dependency>
  &lt;groupId>com.github.tony19&lt;/groupId>
  &lt;artifactId>named-regexp&lt;/artifactId>
  &lt;version>0.1.9&lt;/version>
&lt;/dependency>
</pre>

_Steps_

 1. Add `named-regexp` to your project classpath.
 2. Replace `java.util.regex.Pattern` and `java.util.regex.Matcher` with `NamedPattern` and `NamedMatcher`, respectively.
 3. Use `(?<name>...)` to specify a named capture group as in the following code example:

<pre>
import com.google.code.regexp;

public class NamedRegexpTest {

	public static void main(String[] args) {
		String input = "hello world!";
		String regex = "(?&lt;foo>world)"; // contains capture group named "foo"
		boolean found;

		NamedPattern pattern = NamedPattern.compile(regex);
		NamedMatcher matcher = pattern.matcher(input);

		found = matcher.find();
		System.out.println("Input: " + input);
		System.out.println("Regex: " + regex);
		System.out.println("Found: " + found);

		if (found) {
			System.out.println("Captured: " + matcher.group("foo"));
		}
	}

}
</pre>


Download
========
[named-regexp-0.1.9.jar](https://oss.sonatype.org/content/repositories/releases/com/github/tony19/named-regexp/0.1.9/named-regexp-0.1.9.jar)


Build
=====

To build, use Maven 2+:

    $ mvn package


License
=======
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)


Changelog
=========

__0.1.9__ (23 November 2012)
 * Fixed issues in `NamedMatcher.namedGroups()`

__0.1.8__ (18 July 2012)
 * Fixed bug where escaped parentheses were processed anyway

__0.1.7__ (16 July 2012)
 * Fixed invalid group value when named group was preceded by lookarounds

__0.1.6__ (15 July 2012)
 * Identical to 0.1.5 (cosmetic)

__0.1.5__ (15 July 2012)
 * Fixed query result of named groups

__0.1.4__ (15 July 2012)
 * Fixed bug where escaped parentheses and some special constructs were still counted as capture groups

__0.1.3__ (15 July 2012)
 * Fixed bug where some noncapturing constructs were still counted as capture groups

__0.1.2__ (14 July 2012)
 * Fixed bug that hid regex flags from underlying `Pattern` class
 * Fixed invalid group value when named group preceded by unnamed group
 * Fixed `IndexOutOfBoundsException` when querying named groups

__0.1.1__ (14 July 2012)
 * Initial commit (forked from [Google code][1])

[1]: http://code.google.com/p/named-regexp
[2]: https://tony19.atlassian.net/issues/?jql=project%20%3D%20REGEX
