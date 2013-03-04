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
package com.intellij.designer.clipboard;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.IOException;

/**
 * @author yole
 */
public class SimpleTransferable implements Transferable {
  private static final Logger LOG = Logger.getInstance("#com.intellij.designer.clipboard.SimpleTransferable");

  private final Object myData;
  private final DataFlavor myFlavor;

  public SimpleTransferable(Object data, DataFlavor flavor) {
    myData = data;
    myFlavor = flavor;
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    try {
      return new DataFlavor[]{myFlavor};
    }
    catch (Exception ex) {
      LOG.error(ex);
      return new DataFlavor[0];
    }
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    try {
      return myFlavor.equals(flavor);
    }
    catch (Exception ex) {
      LOG.error(ex);
      return false;
    }
  }

  @Override
  @Nullable
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    try {
      if (!myFlavor.equals(flavor)) {
        return null;
      }
      return myData;
    }
    catch (Exception e) {
      LOG.error(e);
      return null;
    }
  }

  @Nullable
  public static <T> T getData(Transferable transferable, Class<T> dataClass) {
    try {
      for (DataFlavor dataFlavor : transferable.getTransferDataFlavors()) {
        if (transferable.isDataFlavorSupported(dataFlavor)) {
          try {
            Object transferData = transferable.getTransferData(dataFlavor);
            if (transferData != null && dataClass.isInstance(transferData)) {
              return (T)transferData;
            }
          } catch (InvalidDnDOperationException e) {
            // Ignore; even though transferable reported that it supports this flavor,
            // some transferables will throw a "no protocol: unsupported type" exception
            // here, for example if you try to drag an OSX text file into the layout
            // editor
          }
        }
      }
    }
    catch (IOException e) {
    }
    catch (Exception e) {
      LOG.error(e);
    }
    return null;
  }
}