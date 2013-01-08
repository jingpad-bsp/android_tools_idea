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
package org.jetbrains.idea.devkit.inspections.quickfix;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionEP;
import com.intellij.ide.TypePresentationService;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.DevKitBundle;
import org.jetbrains.idea.devkit.dom.Extension;
import org.jetbrains.idea.devkit.dom.Extensions;
import org.jetbrains.idea.devkit.dom.IdeaPlugin;

import javax.swing.*;
import java.util.List;

/**
* @author Dmitry Avdeev
*         Date: 1/20/12
*/
class RegisterInspectionFix implements IntentionAction {

  private final PsiClass myPsiClass;
  private final ExtensionPointName<? extends InspectionEP> myEp;

  RegisterInspectionFix(PsiClass psiClass, ExtensionPointName<? extends InspectionEP> ep) {
    myPsiClass = psiClass;
    myEp = ep;
  }

  @NotNull
  @Override
  public String getText() {
    return "Register inspection";
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return DevKitBundle.message("inspections.component.not.registered.quickfix.family");
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return true;
  }

  @Override
  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    Module module = ModuleUtil.findModuleForPsiElement(file);
    assert module != null;
    List<DomFileElement<IdeaPlugin>> elements =
      DomService.getInstance().getFileElements(IdeaPlugin.class, project, module.getModuleContentWithDependenciesScope());

    elements = ContainerUtil.filter(elements, new Condition<DomFileElement<IdeaPlugin>>() {
      @Override
      public boolean value(DomFileElement<IdeaPlugin> element) {
        VirtualFile virtualFile = element.getFile().getVirtualFile();
        return virtualFile != null && ProjectRootManager.getInstance(project).getFileIndex().isInContent(virtualFile);
      }
    });

    if (elements.isEmpty()) {
      HintManager.getInstance().showErrorHint(editor, "Cannot find plugin descriptor");
      return;
    }

    if (elements.size() == 1) {
      doFix(elements.get(0), project, file);
      return;
    }

    final BaseListPopupStep<DomFileElement<IdeaPlugin>> popupStep =
      new BaseListPopupStep<DomFileElement<IdeaPlugin>>("Choose Plugin Descriptor", elements) {
        @Override
        public Icon getIconFor(DomFileElement<IdeaPlugin> aValue) {
          return TypePresentationService.getService().getIcon(aValue);
        }

        @NotNull
        @Override
        public String getTextFor(DomFileElement<IdeaPlugin> value) {
          final String name = value.getFile().getName();
          if (!Comparing.equal(PluginManager.PLUGIN_XML, name)) {
            return name;
          }
          final Module module = value.getModule();
          return module != null ? name + " (" + module.getName() + ")" : name;
        }

        @Override
        public PopupStep onChosen(DomFileElement<IdeaPlugin> selectedValue, boolean finalChoice) {
          doFix(selectedValue, project, file);
          return FINAL_CHOICE;
        }
      };
    JBPopupFactory.getInstance().createListPopup(popupStep)
      .showInBestPositionFor(editor);
  }

  private void doFix(DomFileElement<IdeaPlugin> selectedValue, final Project project, final PsiFile file) {
    final IdeaPlugin plugin = selectedValue.getRootElement();
    final List<Extensions> extensionsList = plugin.getExtensions();
    Extension extension = new WriteCommandAction<Extension>(project, file) {

      @Override
      protected void run(Result<Extension> result) throws Throwable {
        final Extensions extensions = getExtension(plugin, extensionsList);
        Extension extension = extensions.addExtension(myEp.getName());
        XmlTag tag = extension.getXmlTag();
        tag.setAttribute("implementationClass", myPsiClass.getQualifiedName());
        result.setResult(extension);
      }
    }.execute().throwException().getResultObject();
    PsiNavigateUtil.navigate(extension.getXmlTag());
  }

  private Extensions getExtension(IdeaPlugin plugin, List<Extensions> extensionsList) {
    Extensions extensions = null;
    for (Extensions e : extensionsList) {
      String s = e.getDefaultExtensionNs().getStringValue();
      if (s != null && myEp.getName().startsWith(s)) {
        extensions = e;
        break;
      }
    }
    if (extensions == null) {
      extensions = plugin.addExtensions();
      extensions.getDefaultExtensionNs().setStringValue("com.intellij");
    }
    return extensions;
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
