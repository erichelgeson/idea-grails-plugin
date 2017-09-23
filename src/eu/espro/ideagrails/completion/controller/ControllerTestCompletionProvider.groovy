/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.espro.ideagrails.completion.controller

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import eu.espro.ideagrails.GrailsControllerApi
import eu.espro.ideagrails.GrailsPsiUtil
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrClassDefinition
import org.jetbrains.plugins.groovy.lang.psi.impl.GrAnnotationUtil

import static eu.espro.ideagrails.GrailsCompletionUtil.fieldLookupElement

@CompileStatic
class ControllerTestCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement parent = parameters.getPosition().getParent()
        if (parent instanceof GrReferenceExpression) {
            def testClass = PsiTreeUtil.getParentOfType(parent, GrClassDefinition)
            def cd = GrailsPsiUtil.resolveReference(parent as GrReferenceExpression)
            def anno = testClass?.modifierList?.findAnnotation('grails.test.mixin.TestFor')
            if (anno && testClass == cd) {
                def controllerPsiClass = GrAnnotationUtil.inferClassAttribute(anno, 'value')
                if (controllerPsiClass && controllerPsiClass.name.endsWith('Controller')) {
                    GrailsControllerApi.controllerTestMembers(testClass, controllerPsiClass).each { name, field ->
                        result.addElement(fieldLookupElement(field))
                    }
                }
            }
        }
    }
}
