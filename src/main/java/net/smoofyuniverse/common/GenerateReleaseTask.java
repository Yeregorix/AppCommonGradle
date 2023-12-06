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
import net.smoofyuniverse.exporter.Util;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public abstract class GenerateReleaseTask extends DefaultTask {

	public GenerateReleaseTask() {
		getIncludeDate().convention(true);
		getIncludeSize().convention(true);
		getIncludeSha1().convention(true);
		getIncludeSha256().convention(true);
	}

	@Input
	public abstract Property<Boolean> getIncludeDate();

	@Input
	public abstract Property<Boolean> getIncludeSize();

	@Input
	public abstract Property<Boolean> getIncludeSha1();

	@Input
	public abstract Property<Boolean> getIncludeSha256();

	@TaskAction
	public void generate() throws Exception {
		Path file = getFile().get().getAsFile().toPath();

		String fn = file.getFileName().toString();
		int i = fn.lastIndexOf('.');
		if (i != -1)
			fn = fn.substring(0, i);

		Path jsonFile = file.resolveSibling(fn + ".json");

		try (JsonWriter w = new JsonWriter(Files.newBufferedWriter(jsonFile))) {
			w.setIndent("  ");
			w.beginObject();

			if (getIncludeDate().get()) {
				w.name("date");
				w.value(Instant.now().toString());
			}

			if (getIncludeSize().get()) {
				w.name("size");
				w.value(Files.size(file));
			}

			if (getIncludeSha1().get()) {
				w.name("sha1");
				w.value(Util.toHexString(Util.digest(file, "SHA-1")));
			}

			if (getIncludeSha256().get()) {
				w.name("sha256");
				w.value(Util.toHexString(Util.digest(file, "SHA-256")));
			}

			w.endObject();
		}
	}

	@InputFile
	public abstract RegularFileProperty getFile();
}
