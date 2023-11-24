/*
 * Copyright (c) 2021-2023 Hugo Dupanloup (Yeregorix)
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

import com.google.gson.stream.JsonWriter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApplicationConfig {
	private final Property<String> application, name, title, version;
	private final ListProperty<String> dependencies;

	@Inject
	public ApplicationConfig(ObjectFactory factory) {
		this.application = factory.property(String.class);
		this.name = factory.property(String.class);
		this.title = factory.property(String.class);
		this.version = factory.property(String.class);

		this.dependencies = factory.listProperty(String.class);
		this.dependencies.convention((Iterable<String>) null);
	}

	@Input
	public Property<String> getApplication() {
		return this.application;
	}

	public void setApplication(String value) {
		this.application.set(value);
	}

	@Input
	@Optional
	public Property<String> getName() {
		return this.name;
	}

	public void setName(String value) {
		this.name.set(value);
	}

	@Input
	@Optional
	public Property<String> getTitle() {
		return this.title;
	}

	public void setTitle(String value) {
		this.title.set(value);
	}

	@Input
	@Optional
	public Property<String> getVersion() {
		return this.version;
	}

	public void setVersion(String value) {
		this.version.set(value);
	}

	@Input
	@Optional
	public ListProperty<String> getDependencies() {
		return this.dependencies;
	}

	public void setDependencies(Iterable<? extends String> values) {
		this.dependencies.set(values);
	}

	public void write(Path file) throws IOException {
		try (JsonWriter w = new JsonWriter(Files.newBufferedWriter(file))) {
			w.setIndent("  ");
			write(w);
		}
	}

	public void write(JsonWriter w) throws IOException {
		w.beginObject();

		w.name("application");
		w.value(this.application.get());

		if (this.name.isPresent()) {
			w.name("name");
			w.value(this.name.get());
		}

		if (this.title.isPresent()) {
			w.name("title");
			w.value(this.title.get());
		}

		if (this.version.isPresent()) {
			w.name("version");
			w.value(this.version.get());
		}

		if (this.dependencies.isPresent()) {
			w.name("dependencies");
			w.beginArray();
			for (String dep : this.dependencies.get())
				w.value(dep);
			w.endArray();
		}

		w.endObject();
	}
}
