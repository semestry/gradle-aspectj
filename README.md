Gradle AspectJ plugin
=====================

Usage
-----

Either build this project yourself, and include the `.jar` in your buildscript dependencies,
or use our Maven repo. The plugin is applied using `apply plugin: 'aspectj'`. 
The version of AspectJ to use can be defined using either `ext.aspectjVersion`, 
or the `aspectj` extension's `version` attribute. 
If the AspectJ version is not set, version `1.8.12` is used as the default.

Something like this:

```groovy
buildscript {
    repositories {
        maven {
            url "https://maven.eveoh.nl/content/repositories/releases"
        }
    }

    dependencies {
        classpath "nl.eveoh:gradle-aspectj:2.0"
    }
}

repositories {
    mavenCentral()
}

apply plugin: 'aspectj'

// Optionally
project.ext {
    aspectjVersion = '1.8.12'
}
// Or
aspectj {
    version = '1.8.12'
}
```

Note that version 2.0+ is only compatible with Gradle 4+. Use version 1.6 for earlier Gradle versions.

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

See https://github.com/eveoh/aspectj-example for an example project, contributed by Jason Zwolak.

Development
-----------

We do not use this code any longer and this repository has been archived. Feel free to fork to make your own changes.

License
-------

The project is licensed under the Apache 2.0 license. Most/all of the code
originated from the Spring Security project and was created by Luke Taylor and
Rob Winch. See `LICENSE` for details.
