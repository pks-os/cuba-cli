/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.cli.cubaplugin.screen.entityscreen

import com.beust.jcommander.Parameters
import com.haulmont.cuba.cli.commands.NonInteractiveInfo
import com.haulmont.cuba.cli.cubaplugin.model.ModuleStructure
import com.haulmont.cuba.cli.cubaplugin.model.PlatformVersion
import com.haulmont.cuba.cli.generation.Properties
import com.haulmont.cuba.cli.generation.TemplateProcessor
import com.haulmont.cuba.cli.prompting.Answers

@Parameters(commandDescription = "Creates new edit screen")
class CreateEditScreenCommand(private val forceVersion: PlatformVersion? = null) : EntityScreenCommandBase<EntityScreenModel>(), NonInteractiveInfo {
    private val version: PlatformVersion
        get() = forceVersion ?: projectModel.platformVersion

    override fun getModelName(): String = EntityScreenModel.MODEL_NAME

    override fun createModel(answers: Answers): EntityScreenModel = EntityScreenModel(answers, entitySearch)

    override fun getDefaultScreenId(entityName: String) = projectModel.namespace + "$" + entityName.split('.').last() + ".edit"

    override fun getDefaultControllerName(entityName: String) = entityName.split('.').last() + "Edit"

    override fun getDefaultDescriptorName(entityName: String) = entityName.split('.').last().toLowerCase() + "-edit"

    override fun generate(bindings: Map<String, Any>) {
        TemplateProcessor(resources.getTemplate("editScreen"), bindings, version) {
            transformWhole()
        }

        val webModule = projectStructure.getModule(ModuleStructure.WEB_MODULE)

        if (version < PlatformVersion.v7) {
            addToScreensXml(model.screenId, model.packageName, model.descriptorName)
        }

        val screenMessages = webModule.resolvePackagePath(model.packageName).resolve("messages.properties")
        Properties.modify(screenMessages) {
            set("editorCaption", model.entity.className + " editor")
        }
    }
}