/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

package com.intellij.lang.parameterInfo;

import java.awt.*;
import java.util.EnumSet;

/**
 * Richer interface for decribing a popup hint contents.
 * User: dcheryasov
 * Date: Sep 6, 2008
 */
public interface ParameterInfoUIContextEx extends ParameterInfoUIContext {

  /**
   * Set the contents and formatting of a one-line, multi-formatted popup hint.
   * @param texts pieces ot text to be put together, each individually formattable.
   * @param flags a set of Flags; flags[i] describes formatting of texts[i].
   * @param background background color of the hint.
   */
  void setupUIComponentPresentation(String[] texts, EnumSet<Flag>[] flags, Color background);

  enum Flag {
    HIGHLIGHT, DISABLE, STRIKEOUT // more to come
  }
}
