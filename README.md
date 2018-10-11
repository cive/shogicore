# shogicore [![Build Status](https://travis-ci.org/cive/shogicore.svg?branch=develop)](https://travis-ci.org/cive/shogicore) [![codecov](https://codecov.io/gh/cive/shogicore/branch/develop/graph/badge.svg)](https://codecov.io/gh/cive/shogicore) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) 
Shogi ( 将棋 ): Japanese Chess core library

### This library will use with maven repository. 

* TestTest

* Now, I can't auto deploy...
  + Please look the version from release tab.

* You can use maven repository!!! :)
  + usage:

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <repositories>
        <repository>
            <id>shogicore</id>
            <url>https://raw.github.com/cive/shogicore/mvn-repo/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>shogi-core</groupId>
            <artifactId>shogi-core</artifactId>
            <version>0.1</version>
        </dependency>
    </dependencies>
</project>
```
