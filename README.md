# fluxtion-mavenplugin
A maven plugin integrating the fluxtion toolset with maven build cycle.

### Releasing
To release using the jgitflow plugin requires configuration of user/password:

```xml
<plugin>
    <groupId>external.atlassian.jgitflow</groupId>
    <artifactId>jgitflow-maven-plugin</artifactId>
    <version>1.0-m5.1</version>
    <configuration>
        <username>${fluxtion.github.user}</username>
        <password>${fluxtion.github.password}</password>
    </configuration>
</plugin>  
```

#### Start a release ####
    mvn jgitflow:release-start
#### Finish a release ####
    mvn jgitflow:release-finish



