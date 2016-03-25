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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Fork of org.sonar.api.utils.ZipUtils
 */
public final class ZipUtils7 {

  private ZipUtils7() {
    // only static methods
  }

  public static void zipDir(final Path srcDir, Path zip) throws IOException {

    try (final OutputStream out = FileUtils.openOutputStream(zip.toFile());
      final ZipOutputStream zout = new ZipOutputStream(out)) {
      Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try (InputStream in = new BufferedInputStream(new FileInputStream(file.toFile()))) {
            String entryName = srcDir.relativize(file).toString();
            ZipEntry entry = new ZipEntry(entryName);
            zout.putNextEntry(entry);
            IOUtils.copy(in, zout);
            zout.closeEntry();
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          if (dir.equals(srcDir)) {
            return FileVisitResult.CONTINUE;
          }

          String entryName = srcDir.relativize(dir).toString();
          ZipEntry entry = new ZipEntry(entryName);
          zout.putNextEntry(entry);
          zout.closeEntry();
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  public static void smartReportZip(final Path srcDir, Path zip) throws IOException {
    try (final OutputStream out = FileUtils.openOutputStream(zip.toFile());
      final ZipOutputStream zout = new ZipOutputStream(out)) {
      Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try (InputStream in = new BufferedInputStream(new FileInputStream(file.toFile()))) {
            String entryName = srcDir.relativize(file).toString();
            int level = file.toString().endsWith(".pb") ? ZipOutputStream.STORED : Deflater.DEFAULT_COMPRESSION;
            zout.setLevel(level);
            ZipEntry entry = new ZipEntry(entryName);
            zout.putNextEntry(entry);
            IOUtils.copy(in, zout);
            zout.closeEntry();
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          if (dir.equals(srcDir)) {
            return FileVisitResult.CONTINUE;
          }

          String entryName = srcDir.relativize(dir).toString();
          ZipEntry entry = new ZipEntry(entryName);
          zout.putNextEntry(entry);
          zout.closeEntry();
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  public static void fastZip(final Path srcDir, Path zip) throws IOException {
    try (final OutputStream out = FileUtils.openOutputStream(zip.toFile());
      final ZipOutputStream zout = new ZipOutputStream(out)) {
      zout.setMethod(ZipOutputStream.STORED);
      Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try (InputStream in = new BufferedInputStream(new FileInputStream(file.toFile()))) {
            String entryName = srcDir.relativize(file).toString();
            ZipEntry entry = new ZipEntry(entryName);
            zout.putNextEntry(entry);
            IOUtils.copy(in, zout);
            zout.closeEntry();
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          if (dir.equals(srcDir)) {
            return FileVisitResult.CONTINUE;
          }

          String entryName = srcDir.relativize(dir).toString();
          ZipEntry entry = new ZipEntry(entryName);
          zout.putNextEntry(entry);
          zout.closeEntry();
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

}
