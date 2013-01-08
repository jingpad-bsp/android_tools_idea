/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.jps.incremental.artifacts.instructions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.cmdline.ProjectDescriptor;

import java.io.IOException;

/**
 * @author nik
 */
public abstract class SourceFileFilter {
  public static final SourceFileFilter ALL = new SourceFileFilter() {
    @Override
    public boolean accept(@NotNull String fullFilePath, ProjectDescriptor projectDescriptor) {
      return true;
    }
  };

  public abstract boolean accept(@NotNull String fullFilePath, ProjectDescriptor projectDescriptor) throws IOException;
}
