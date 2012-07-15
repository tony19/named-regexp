named-regexp 0.1.6
==================
This library is a wrapper for `java.util.regex`, implementing named capture groups for Java 5/6. Note this isn't needed in Java 7, which already supports this feature.

This is a fork of the [named-regexp](http://code.google.com/p/named-regexp) project that fixes several bugs.


License
=======
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)


Download
========
[named-regexp-0.1.6.jar](https://oss.sonatype.org/content/repositories/releases/com/github/tony19/named-regexp/0.1.6/named-regexp-0.1.6.jar)


Usage
=====

 1. Replace `java.util.regex.Pattern` and `java.util.regex.Matcher` with `NamedPattern` and `NamedMatcher`, respectively.
 2. Use `(?<name>...)` to specify a named capture group as in the following code example:

<pre>
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
</pre>
