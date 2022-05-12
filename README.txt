btclib

Apache Maven 3.8.5

mvn versions:display-plugin-updates
mvn versions:display-dependency-updates
mvn clean package

Java version compatibility

The code has been tested primarily using JDK 18.0.1.1 (https://jdk.java.net/18/).
The tests have runtime dependencies on at least Java 11 since there are references to the Unicode code point "BITCOIN SIGN" U+20bf.
Java 11 was the first release to support the version of Unicode (10.0) that introduced the "BITCOIN SIGN" code point.
Local variable type inference, a Java 10 feature addition, is used commonly.
Maintaining compatibility with old Java releases has not been a goal / priority of this project.
As such, the compiler settings have been set to target Java 18, and recent features may be used when convenient.
Please do your own research and due diligence if you attempt to use this code with an older runtime.
