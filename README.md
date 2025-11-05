# Class Finder
## [Docs](https://darthcofferus.github.io/Class-Finder/)
## Description
With this library, you can find classes in your program without
knowing their names. You can set a predicate according to which
classes will be filtered, and also set an action that will be
performed with each class found in the program that will match
the predicate. The class search can be performed both for compiled
files and for a JAR file. You can limit the search area to a
specific package, package and its subpackages, or you can searchfor classes in the entire program at once.
## Examples
**Nested static classes are made for compactness of the example.**
```java
import com.github.darthcofferus.class_finder.*;

public class Main {

    public static void main(String[] args) {
        new ClassFinder()
                .setPredicate(aClass -> aClass != Base.class && Base.class.isAssignableFrom(aClass))
                .setActionWithClass(ActionWithClass.CREATING_INSTANCE)
                .find();
    }

    static class Base {
        Base() {
            System.out.println("Instance created: " + getClass().getSimpleName());
        }
    }

    static class Inheritor1 extends Base {}

    static class Inheritor2 extends Base {}

}
```
**Execution result:**
```
Instance created: Inheritor1
Instance created: Inheritor2
```
## Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <!-- https://github.com/darthcofferus/Class-Finder -->
        <groupId>com.github.darthcofferus</groupId>
        <artifactId>class-finder</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```