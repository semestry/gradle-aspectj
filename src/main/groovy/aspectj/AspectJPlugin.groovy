package aspectj

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.TaskAction
import org.gradle.api.logging.LogLevel
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException

import org.gradle.api.plugins.JavaPlugin
import org.gradle.plugins.ide.eclipse.GenerateEclipseProject
import org.gradle.plugins.ide.eclipse.GenerateEclipseClasspath
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.eclipse.model.BuildCommand
import org.gradle.plugins.ide.eclipse.model.ProjectDependency

/**
 *
 * @author Luke Taylor
 * @author Mike Noordermeer
 */
class AspectJPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.plugins.apply(JavaPlugin)

        if (!project.hasProperty('aspectjVersion')) {
            throw new GradleException("You must set the property 'aspectjVersion' before applying the aspectj plugin")
        }

        if (project.configurations.findByName('ajtools') == null) {
            project.configurations.create('ajtools')
            project.dependencies {
                ajtools "org.aspectj:aspectjtools:${project.aspectjVersion}"
                compile "org.aspectj:aspectjrt:${project.aspectjVersion}"
            }
        }

        if (project.configurations.findByName('aspectpath') == null) {
            project.configurations.create('aspectpath')
        }

        if (project.configurations.findByName('ajInpath') == null) {
            project.configurations.create('ajInpath')
        }

        project.tasks.create(name: 'compileAspect', overwrite: true, description: 'Compiles AspectJ Source', type: Ajc) {
            dependsOn project.configurations*.getTaskDependencyFromProjectDependency(true, "build")

            dependsOn project.processResources
            sourceSet = project.sourceSets.main
            inputs.files(sourceSet.allSource)
            outputs.dir(sourceSet.output.classesDir)
            aspectPath = project.configurations.aspectpath
            ajInpath = project.configurations.ajInpath
        }
        project.tasks.compileJava.deleteAllActions()
        project.tasks.compileJava.dependsOn project.tasks.compileAspect


        project.tasks.create(name: 'compileTestAspect', overwrite: true, description: 'Compiles AspectJ Test Source', type: Ajc) {
            dependsOn project.processTestResources, project.compileJava
            sourceSet = project.sourceSets.test
            inputs.files(sourceSet.allSource)
            outputs.dir(sourceSet.output.classesDir)
            aspectPath = project.configurations.aspectpath
            ajInpath = project.configurations.ajInpath
        }
        project.tasks.compileTestJava.deleteAllActions()
        project.tasks.compileTestJava.dependsOn project.tasks.compileTestAspect
    }
}

class Ajc extends DefaultTask {
    SourceSet sourceSet
    FileCollection aspectPath
    FileCollection ajInpath
    String xlint = 'ignore'

    Ajc() {
        logging.captureStandardOutput(LogLevel.INFO)
    }

    @TaskAction
    def compile() {
        logger.info("="*30)
        logger.info("="*30)
        logger.info("Running ajc ...")
        logger.info("classpath: ${sourceSet.compileClasspath.asPath}")
        logger.info("srcDirs $sourceSet.java.srcDirs")
        ant.taskdef(resource: "org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties", classpath: project.configurations.ajtools.asPath)
        ant.iajc(classpath: sourceSet.compileClasspath.asPath, fork: 'true', destDir: sourceSet.output.classesDir.absolutePath,
                source: project.convention.plugins.java.sourceCompatibility,
                target: project.convention.plugins.java.targetCompatibility,
                inpath: ajInpath.asPath, xlint: xlint,
                aspectPath: aspectPath.asPath, sourceRootCopyFilter: '**/*.java,**/*.aj', showWeaveInfo: 'true') {
            sourceroots {
                sourceSet.java.srcDirs.each {
                    logger.info("   sourceRoot $it")
                    pathelement(location: it.absolutePath)
                }
            }
        }
    }
}