# remove-unused-imports-maven-plugin

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.xenoamess/remove-unused-imports-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.xenoamess/remove-unused-imports-maven-plugin)


## Goal:
maven plugin for remove unused imports according to pmd's result.
should run pmd's UnusedImports rule first.

## Usage:

include it in your `<build>`

```pom
<plugin>
    <groupId>com.xenoamess</groupId>
    <artifactId>remove-unused-imports-maven-plugin</artifactId>
    <version>0.0.5-SNAPSHOT</version>
    <executions>
        <execution>
            <goals>
                <goal>process</goal>
            </goals>
            <configuration>
                <ruleNames>
                    <ruleName>UnusedImports</ruleName>
                    <ruleName>DuplicateImports</ruleName>
                    <ruleName>UnnecessaryReturn</ruleName>
                    <ruleName>ImportFromSamePackage</ruleName>
                    <ruleName>DontImportJavaLang</ruleName>
                </ruleNames>
                <pmdXmlPath>${basedir}/target/pmd.xml</pmdXmlPath>
                <breakBuildIfHaveViolationRemains>false</breakBuildIfHaveViolationRemains>
            </configuration>
        </execution>
    </executions>
</plugin>
```
make sure when you run it, there be a pmd.xml already generated to use.
