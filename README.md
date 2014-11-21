Gradle AspectJ plugin
=====================

Usage
-----

Either build this project yourself, and include the `.jar` in your buildscript dependencies,
or use our Maven repo. Then set `ext.aspectjVersion` to your AspectJ version and `apply plugin: 'aspectj'`.
Something like this:

```groovy
buildscript {
    repositories {
        maven {
            url "https://maven.eveoh.nl/content/repositories/releases"
        }
    }

    dependencies {
        classpath "nl.eveoh:gradle-aspectj:1.5"
    }
}

repositories {
    mavenCentral()
}

project.ext {
    aspectjVersion = '1.8.4'
}

apply plugin: 'aspectj'
```

Use the `aspectpath`, `ajInpath`, `testAspectpath` and `testAjInpath` to specify external aspects or external code to weave:

```groovy
dependencies {
    aspectpath "org.springframework:spring-aspects:${springVersion}"
}
```

By default, `xlint: ignore` is used. Specify a different value for the `xlint` variable of the `compileAspect` or
`compileTestAspect` task to show AspectJ warnings:

```groovy
compileAspect {
    xlint = 'warning'
}
```

It is possible to specify a different value for the `maxmem` variable of the `compileAspect` or
`compileTestAspect` task to increase or decrease the max heap size:

```groovy
compileAspect {
    maxmem = '1024m'
}
```

To specify additional [ajc arguments](http://www.eclipse.org/aspectj/doc/released/devguide/antTasks-iajc.html#antTasks-iajc-options), you can use ```additionalAjcArgs```. If ```xlint``` or ```maxmem``` are also specified in ```additionalAjcArgs```, the values in ```additionalAjcArgs``` will take precedence. For example, to preserve debug symbols,

```groovy
compileAspect {
     additionalAjcArgs = ['debug' : '', 'X' : 'noInline', 'preserveAllLocals' : '']
}
```

Development
-----------

This project contains the feature set we need for our own projects, but other projects may have other requirements. Feel free to open a pull request to add a feature.

License
-------

The project is licensed under the Apache 2.0 license. Most/all of the code
originated from the Spring Security project and was created by Luke Taylor and
Rob Winch. See `LICENSE` for details.
