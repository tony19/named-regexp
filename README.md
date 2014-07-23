named-regexp 0.2.3 [![Build Status](https://tony19.ci.cloudbees.com/job/named-regexp/job/named-regexp-SNAPSHOT/badge/icon)](https://tony19.ci.cloudbees.com/job/named-regexp/job/named-regexp-SNAPSHOT/)
==================
This lightweight library adds support for [named capture groups][6] in Java 5/6 (and on Android).

This is a fork of the [named-regexp][1] project from Google Code (currently inactive).


Usage
-----
You can use the same constructs for named capture groups from Java 7 (i.e., `(?<name>patt)`, etc.), as in the following example:

```java
import com.google.code.regexp.Pattern;
import com.google.code.regexp.Matcher;

public class NamedRegexpTest {
    public static void main(String[] args) {
        // pattern contains capture group, named "foo"
        Matcher m = Pattern.compile("(?<foo>\\w+) world").matcher("hello world!");
        m.find();
        System.out.println(m.group("foo")); // prints "hello"
    }
}
```

See more [examples][3]


Download
--------
Grab the latest release ([`named-regexp-0.2.3.jar`][4]) and include it in your classpath...

*OR* Maven users can simply add this dependency:

```xml
<dependency>
  <groupId>com.github.tony19</groupId>
  <artifactId>named-regexp</artifactId>
  <version>0.2.3</version>
</dependency>
```

_Any snapshots would be hosted in the [Sonatype snapshot repository][5]._


Build
-----

To build `named-regexp` from source, use Maven 2 or higher:

```bash
$ git clone git://github.com/tony19/named-regexp.git
$ cd named-regexp
$ mvn package
```


Support
-------
Feel free to ask any questions at named-regexp@googlegroups.com.
Please report any issues in [JIRA][2].


License
-------

    Copyright 2013 Anthony Trinh.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[1]: http://code.google.com/p/named-regexp
[2]: https://tony19.atlassian.net/issues/?jql=project%20%3D%20REGEX
[3]: http://tony19.github.com/named-regexp/index.html
[4]: https://oss.sonatype.org/content/repositories/releases/com/github/tony19/named-regexp/0.2.3/named-regexp-0.2.3.jar
[5]: https://oss.sonatype.org/content/repositories/snapshots/
[6]: http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#groupname
[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/6153b1e63711b00863135b84138816f9 "githalytics.com")](http://githalytics.com/tony19/named-regexp)
