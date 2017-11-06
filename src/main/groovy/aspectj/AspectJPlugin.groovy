package aspectj

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

/**
 *
 * @author Luke Taylor
 * @author Mike Noordermeer
 */
class AspectJPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.plugins.apply(JavaPlugin)

        def aspectj = project.extensions.create('aspectj', AspectJExtension, project)

        if (project.configurations.findByName('ajtools') == null) {
            project.configurations.create('ajtools')
            project.afterEvaluate { p ->
                if (aspectj.version == null) {
                    throw new GradleException("No aspectj version supplied")
                }

                p.dependencies {
                    ajtools "org.aspectj:aspectjtools:${aspectj.version}"
                    compile "org.aspectj:aspectjrt:${aspectj.version}"
                }
            }
        }

        for (projectSourceSet in project.sourceSets) {
            def namingConventions = projectSourceSet.name.equals('main') ? new MainNamingConventions() : new DefaultNamingConventions();
            for (configuration in [namingConventions.getAspectPathConfigurationName(projectSourceSet), namingConventions.getAspectInpathConfigurationName(projectSourceSet)]) {
                if (project.configurations.findByName(configuration) == null) {
                    project.configurations.create(configuration)
                }
            }

            if (!projectSourceSet.allJava.isEmpty()) {
                def aspectTaskName = namingConventions.getAspectCompileTaskName(projectSourceSet)
                def javaTaskName = namingConventions.getJavaCompileTaskName(projectSourceSet)

                project.tasks.create(name: aspectTaskName, overwrite: true, description: "Compiles AspectJ Source for ${projectSourceSet.name} source set", type: Ajc) {
                    sourceSet = projectSourceSet
                    inputs.files(sourceSet.allJava)
                    outputs.dir(sourceSet.java.outputDir)
                    aspectpath = project.configurations.findByName(namingConventions.getAspectPathConfigurationName(projectSourceSet))
                    ajInpath = project.configurations.findByName(namingConventions.getAspectInpathConfigurationName(projectSourceSet))
                }

                project.tasks[aspectTaskName].setDependsOn(project.tasks[javaTaskName].dependsOn)
                project.tasks[aspectTaskName].dependsOn(project.tasks[aspectTaskName].aspectpath)
                project.tasks[aspectTaskName].dependsOn(project.tasks[aspectTaskName].ajInpath)
                project.tasks[javaTaskName].deleteAllActions()
                project.tasks[javaTaskName].dependsOn(project.tasks[aspectTaskName])
            }
        }
    }

    private static class MainNamingConventions implements NamingConventions {

        @Override
        String getJavaCompileTaskName(final SourceSet sourceSet) {
            return "compileJava"
        }

        @Override
        String getAspectCompileTaskName(final SourceSet sourceSet) {
            return "compileAspect"
        }

        @Override
        String getAspectPathConfigurationName(final SourceSet sourceSet) {
            return "aspectpath"
        }

        @Override
        String getAspectInpathConfigurationName(final SourceSet sourceSet) {
            return "ajInpath"
        }
    }

    private static class DefaultNamingConventions implements NamingConventions {

        @Override
        String getJavaCompileTaskName(final SourceSet sourceSet) {
            return "compile${sourceSet.name.capitalize()}Java"
        }

        @Override
        String getAspectCompileTaskName(final SourceSet sourceSet) {
            return "compile${sourceSet.name.capitalize()}Aspect"
        }

        @Override
        String getAspectPathConfigurationName(final SourceSet sourceSet) {
            return "${sourceSet.name}Aspectpath"
        }

        @Override
        String getAspectInpathConfigurationName(final SourceSet sourceSet) {
            return "${sourceSet.name}AjInpath"
        }
    }
}

class Ajc extends DefaultTask {

    SourceSet sourceSet

    FileCollection aspectpath
    FileCollection ajInpath

    // ignore or warning
    String xlint = 'ignore'

    String maxmem
    Map<String, String> additionalAjcArgs

    Ajc() {
        logging.captureStandardOutput(LogLevel.INFO)
    }

    @TaskAction
    def compile() {
        logger.info("=" * 30)
        logger.info("=" * 30)
        logger.info("Running ajc ...")
        logger.info("classpath: ${sourceSet.compileClasspath.asPath}")
        logger.info("srcDirs $sourceSet.java.srcDirs")

        def iajcArgs = [classpath           : sourceSet.compileClasspath.asPath,
                        destDir             : sourceSet.java.outputDir.absolutePath,
                        s                   : sourceSet.java.outputDir.absolutePath,
                        source              : project.convention.plugins.java.sourceCompatibility,
                        target              : project.convention.plugins.java.targetCompatibility,
                        inpath              : ajInpath.asPath,
                        xlint               : xlint,
                        fork                : 'true',
                        aspectPath          : aspectpath.asPath,
                        sourceRootCopyFilter: '**/*.java,**/*.aj',
                        showWeaveInfo       : 'true']

        if (null != maxmem) {
            iajcArgs['maxmem'] = maxmem
        }

        if (null != additionalAjcArgs) {
            for (pair in additionalAjcArgs) {
                iajcArgs[pair.key] = pair.value
            }
        }

        ant.taskdef(resource: "org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties", classpath: project.configurations.ajtools.asPath)
        ant.iajc(iajcArgs) {
            sourceRoots {
                sourceSet.java.srcDirs.each {
                    logger.info("   sourceRoot $it")
                    pathelement(location: it.absolutePath)
                }
            }
        }
    }
}

class AspectJExtension {

    String version

    AspectJExtension(Project project) {
        this.version = project.findProperty('aspectjVersion') ?: '1.8.12'
    }
}
