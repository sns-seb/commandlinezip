/*
 * commandlinezip
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.commandlinezip;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class App {

  private static final String TARGET_LABEL = "target";
  private static final String SOURCE_LABEL = "source";

  public static void main(String[] args) {
    if (args.length < 3) {
      System.err.println("Argument(s) missing\n" +
        "usage: $0 OPERATION SOURCE TARGET [ numberOfRuns ]\n" +
        "OPERATION = " + Command.values() + "\n" +
        "SOURCE = absolute path to directory to compress or to zip file to decompress\n" +
        "TARGET = absolute path to directory to decompress to or to zip file to compress to\n" +
        "numberOfRuns (optional) = number of time doing the operation (defaults to 1)");
      System.exit(1);
    }
    int runs = extractNumberOfRuns(args);
    String commandString = args[0];
    Command command = requireNonNull(Command.parse(commandString), "Unrecognized command " + commandString);

    for (int i = 0; i < runs; i++) {
      Path output = null;
      try {
        Instant start = Instant.now();
        output = command.run(args, start);
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (i < runs - 1) {
        deleteQuietly(output);
      }
    }
  }

  private static int extractNumberOfRuns(String[] args) {
    if (args.length < 4) {
      return 1;
    }
    return Integer.parseInt(args[3]);
  }

  private static void deleteQuietly(Path path) {
    System.out.println(format("Deleting '%s'", path));
    try {
      if (Files.isRegularFile(path)) {
        Files.delete(path);
      } else {
        Files.walkFileTree(path, DeleteRecursivelyFileVisitor.INSTANCE);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static final class DeleteRecursivelyFileVisitor extends SimpleFileVisitor<Path> {
    public static final DeleteRecursivelyFileVisitor INSTANCE = new DeleteRecursivelyFileVisitor();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      Files.delete(file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      Files.delete(dir);
      return FileVisitResult.CONTINUE;
    }
  }

  private static void logDone(Instant now) {
    Duration duration = Duration.between(now, Instant.now());
    System.out.println(format("done in %s", duration));
  }

  private static Path dirPathOf(String dirPath, String label) {
    Path res = Paths.get(dirPath);
    if (Files.isDirectory(res)) {
      return res;
    }
    throw new IllegalArgumentException(format("%s directory '%s' does not exist or is not a directory", label, dirPath));
  }

  private static Path sourceFilePathOf(String filePath) {
    Path res = Paths.get(filePath);
    if (Files.isRegularFile(res)) {
      return res;
    }
    throw new IllegalArgumentException(format("target file '%s' does not exist or is not a file", filePath));
  }

  private static Path targetFilePathOf(String filePath) {
    Path res = Paths.get(filePath);
    if (Files.exists(res.getParent()) && !Files.exists(res)) {
      return res;
    }
    throw new IllegalArgumentException(format("target file '%s' already exists or parent directory does not exist", filePath));
  }

  private enum Command {
    ZIP {
      @Override
      public Path run(String[] args, Instant start) throws IOException {
        Path zipFile = targetFilePathOf(args[2]);
        ZipUtils6.zipDir(dirPathOf(args[1], SOURCE_LABEL).toFile(), zipFile.toFile());
        logDone(start);
        return zipFile;
      }
    }, UNZIP {
      @Override
      public Path run(String[] args, Instant start) throws IOException {
        Path targetDir = dirPathOf(args[2], TARGET_LABEL);
        ZipUtils6.unzip(sourceFilePathOf(args[1]).toFile(), targetDir.toFile());
        logDone(start);
        return targetDir;
      }
    }, ZIP7 {
      @Override
      public Path run(String[] args, Instant start) throws IOException {
        Path zipFile = targetFilePathOf(args[2]);
        ZipUtils7.zipDir(dirPathOf(args[1], SOURCE_LABEL), zipFile);
        logDone(start);
        return zipFile;
      }
    }, SMARTZIP {
      @Override
      public Path run(String[] args, Instant start) throws IOException {
        Path zipFile = targetFilePathOf(args[2]);
        ZipUtils7.smartReportZip(dirPathOf(args[1], SOURCE_LABEL), zipFile);
        logDone(start);
        return zipFile;
      }
    }, FASTZIP {
      @Override
      public Path run(String[] args, Instant start) throws IOException {
        Path zipFile = targetFilePathOf(args[2]);
        ZipUtils7.fastZip(dirPathOf(args[1], SOURCE_LABEL), zipFile);
        logDone(start);
        return zipFile;
      }
    };

    public abstract Path run(String[] args, Instant start) throws IOException;

    public static Command parse(String s) {
      for (Command command : values()) {
        if (command.name().equalsIgnoreCase(s)) {
          return command;
        }
      }
      return null;
    }
  }
}
