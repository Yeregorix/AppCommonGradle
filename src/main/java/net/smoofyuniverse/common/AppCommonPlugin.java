/*
 * Copyright (c) 2021 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.common;

import net.smoofyuniverse.exporter.DependencyExporterPlugin;
import net.smoofyuniverse.exporter.ExportConfig;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

import java.io.File;

public class AppCommonPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		// Dependency exporter
		project.getPlugins().apply(DependencyExporterPlugin.class);
		NamedDomainObjectContainer<ExportConfig> exportConfigs = (NamedDomainObjectContainer<ExportConfig>) project.getExtensions().getByName("dependencyExport");

		// Application config
		ApplicationConfig appConfig = project.getExtensions().create("app", ApplicationConfig.class);
		appConfig.getName().convention(project.provider(project::getName));
		appConfig.getVersion().convention(project.provider(() -> project.getVersion().toString()));

		// Dependencies management
		ConfigurationContainer configs = project.getConfigurations();
		NamedDomainObjectProvider<Configuration> appcommon = configs.register("appcommon"), export = configs.register("export");

		// Configure default dependency export
		exportConfigs.register("application", config -> {
			config.getPath().convention("dep/application.json");
			config.getConfig().convention(export);
		});

		// Generate config task
		TaskProvider<GenerateConfigTask> generateConfig = project.getTasks().register("generateAppConfig", GenerateConfigTask.class, task -> {
			task.getConfiguration().set(appConfig);
			task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("generated/appcommon/config"));
		});

		project.getPlugins().withType(JavaLibraryPlugin.class, plugin -> {
			// Dependencies management
			configs.named(JavaPlugin.API_CONFIGURATION_NAME, config -> config.extendsFrom(appcommon.get()));
			configs.named(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, config -> config.extendsFrom(export.get()));
		});

		project.getPlugins().withType(JavaPlugin.class, plugin -> {
			// Include generated config
			project.getExtensions().getByType(SourceSetContainer.class).named(SourceSet.MAIN_SOURCE_SET_NAME, sourceSet -> {
				sourceSet.getResources().srcDir(generateConfig.map(DefaultTask::getOutputs));
			});

			// Shade AppCommon and configure manifest
			project.afterEvaluate(p -> {
				Configuration compileClasspath = p.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);

				p.getTasks().named(JavaPlugin.JAR_TASK_NAME, task -> {
					Jar jar = (Jar) task;

					for (File dep : appcommon.get()) {
						if (compileClasspath.contains(dep)) {
							jar.from(p.zipTree(dep)).exclude("META-INF", "META-INF/**");
						}
					}

					Attributes attrs = jar.getManifest().getAttributes();
					attrs.put("Main-Class", "net.smoofyuniverse.common.Main");
					attrs.put("Launcher-Agent-Class", "net.smoofyuniverse.common.Main");
				});
			});
		});

		// Dummy Java agent
		project.getTasks().register("dummyAgentJar", Jar.class, jar -> {
			jar.getArchiveBaseName().set("DummyAgent");
			jar.getArchiveVersion().set("");
			jar.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("generated/appcommon"));
			jar.getManifest().getAttributes().put("Premain-Class", "net.smoofyuniverse.common.Main");
		});
	}
}
