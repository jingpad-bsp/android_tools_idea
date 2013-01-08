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
package com.intellij.spellchecker.tokenizer;

import com.intellij.codeInsight.daemon.impl.actions.AbstractSuppressByNoInspectionCommentFix;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiPlainText;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.inspections.TextSplitter;
import com.intellij.spellchecker.quickfixes.AcceptWordAsCorrect;
import com.intellij.spellchecker.quickfixes.ChangeTo;
import com.intellij.spellchecker.quickfixes.RenameTo;
import com.intellij.spellchecker.quickfixes.SpellCheckerQuickFix;
import org.jetbrains.annotations.NotNull;

public class SpellcheckingStrategy {
  protected final Tokenizer<PsiComment> myCommentTokenizer = new CommentTokenizer();
  protected final Tokenizer<XmlAttributeValue> myXmlAttributeTokenizer = TokenizerBase.create(TextSplitter.getInstance());
  protected final Tokenizer<XmlText> myXmlTextTokenizer = new XmlTextTokenizer();

  public static final ExtensionPointName<SpellcheckingStrategy> EP_NAME = ExtensionPointName.create("com.intellij.spellchecker.support");
  public static final Tokenizer EMPTY_TOKENIZER = new Tokenizer() {
    @Override
    public void tokenize(@NotNull PsiElement element, TokenConsumer consumer) {
    }
  };

  public static final Tokenizer<PsiElement> TEXT_TOKENIZER = new TokenizerBase<PsiElement>(PlainTextSplitter.getInstance());

  private static final SpellCheckerQuickFix[] BATCH_FIXES = new SpellCheckerQuickFix[]{new AcceptWordAsCorrect()};

  @NotNull
  public Tokenizer getTokenizer(PsiElement element) {
    if (element instanceof PsiNameIdentifierOwner) return new PsiIdentifierOwnerTokenizer();
    if (element instanceof PsiComment) {
      if (AbstractSuppressByNoInspectionCommentFix.isSuppressionComment(element)) {
        return EMPTY_TOKENIZER;
      }
      return myCommentTokenizer;
    }
    if (element instanceof XmlAttributeValue) return myXmlAttributeTokenizer;
    if (element instanceof XmlText) return myXmlTextTokenizer;
    if (element instanceof PsiPlainText) return TEXT_TOKENIZER;
    return EMPTY_TOKENIZER;
  }

  public SpellCheckerQuickFix[] getRegularFixes(PsiElement element,
                                                int offset,
                                                @NotNull TextRange textRange,
                                                boolean useRename,
                                                String wordWithTypo) {
    return getDefaultRegularFixes(useRename, wordWithTypo);
  }

  public static SpellCheckerQuickFix[] getDefaultRegularFixes(boolean useRename, String wordWithTypo) {
    return new SpellCheckerQuickFix[]{
      (useRename ? new RenameTo(wordWithTypo) : new ChangeTo(wordWithTypo)),
      new AcceptWordAsCorrect(wordWithTypo)
    };
  }

  public SpellCheckerQuickFix[] getBatchFixes(PsiElement element, int offset, @NotNull TextRange textRange) {
    return getDefaultBatchFixes();
  }

  public static SpellCheckerQuickFix[] getDefaultBatchFixes() {
    return BATCH_FIXES;
  }
}
