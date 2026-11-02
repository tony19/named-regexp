named-regexp [![GitHub release](https://img.shields.io/github/release/tony19/named-regexp.svg?maxAge=2592000)](https://github.com/tony19/named-regexp/releases/) [![Build](https://github.com/tony19/named-regexp/actions/workflows/ci.yml/badge.svg)](https://github.com/tony19/named-regexp/actions/workflows/ci.yml)
---

This library backports [named capture groups](http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#groupname) from Java 7 to Java 5/6.

Usage
-----
You can use the same constructs for named capture groups from Java 7
(i.e., `(?<name>patt)`, etc.), as in the following example:

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

Install
--------

### Gradle
```gradle
implementation 'com.github.tony19:named-regexp:1.0.0'
```

### Maven
```xml
<dependency>
  <groupId>com.github.tony19</groupId>
  <artifactId>named-regexp</artifactId>
  <version>1.0.0</version>
</dependency>
```

[Maven Central](https://search.maven.org/artifact/com.github.tony19/named-regexp)

Build
-----

To build `named-regexp` from source, use Maven 2 or higher and JDK 8 or higher:

```bash
$ git clone git://github.com/tony19/named-regexp.git
$ cd named-regexp
$ mvn package
```

To create Java 5/6 compatible bytecode, use JDK 11 or older to build this library.
The build automatically targets Java 8 (the minimum supported version) if using a
newer JDK to build.

Release
-------

To cut a new release, run the [Release](https://github.com/tony19/named-regexp/actions/workflows/release.yml)
workflow from the Actions tab (or `gh workflow run release.yml [-f version=X.Y.Z]`).
By default, it bumps the patch version of the latest release; a custom version can
be given as input. The workflow sets the version in `pom.xml` and this README, tags
the release commit (`vX.Y.Z`), creates a GitHub release with notes generated from
[Conventional Commits](https://www.conventionalcommits.org/), and then bumps
`pom.xml` to the next `-SNAPSHOT` version.

The workflow does not deploy to Maven Central. To deploy a release cut by the
workflow, check out its tag and deploy it with signed artifacts:

```bash
$ git checkout vX.Y.Z
$ mvn clean deploy -DperformRelease=true
```

(`./release.sh` is the legacy `maven-release-plugin` flow, which tags and bumps
versions itself, so don't combine it with the workflow.)

*Releases are deployed to https://repo1.maven.org/maven2/com/github/tony19/named-regexp/*

To release a `SNAPSHOT` (i.e., the version in `pom.xml` ends with `-SNAPSHOT`),
run the following command:

```bash
$ mvn clean deploy
```

*Snapshots are deployed to https://oss.sonatype.org/content/repositories/snapshots/com/github/tony19/named-regexp/*

License
-------

    Copyright 2024 Anthony Trinh.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
