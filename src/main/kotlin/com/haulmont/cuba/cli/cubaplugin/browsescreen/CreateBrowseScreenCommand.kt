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

package com.haulmont.cuba.cli.cubaplugin.browsescreen

import com.beust.jcommander.Parameters
import com.haulmont.cuba.cli.ModuleStructure
import com.haulmont.cuba.cli.cubaplugin.CubaPlugin
import com.haulmont.cuba.cli.cubaplugin.ScreenCommandBase
import com.haulmont.cuba.cli.generation.PropertiesHelper
import com.haulmont.cuba.cli.generation.TemplateProcessor
import com.haulmont.cuba.cli.generation.getChildElements
import com.haulmont.cuba.cli.generation.parse
import com.haulmont.cuba.cli.prompting.Answers
import com.haulmont.cuba.cli.prompting.QuestionsList
import net.sf.practicalxml.DomUtil

@Parameters(commandDescription = "Creates new browse screen")
class CreateBrowseScreenCommand : ScreenCommandBase<BrowseScreenModel>() {
    override fun getModelName(): String = BrowseScreenModel.MODEL_NAME

    override fun preExecute() {
        checkProjectExistence()
    }

    override fun QuestionsList.prompting() {
        val persistenceXml = projectStructure.getModule(ModuleStructure.GLOBAL_MODULE).persistenceXml
        val entitiesList = parse(persistenceXml).documentElement
                .let { DomUtil.getChild(it, "persistence-unit") }
                .getChildElements()
                .filter { element -> element.tagName == "class" }
                .map { it.textContent.trim() }

        if (entitiesList.isEmpty())
            fail("Project does not have any entities.")

        options("entityName", "Choose entity", entitiesList)

        question("packageName", "Package name") {
            validate {
                checkIsPackage()
            }

            default { answers ->
                val entityName: String by answers

                val packageParts = entityName.split('.').filter { it != "entity" }
                packageParts.take(packageParts.lastIndex).joinToString(".") + ".web." + packageParts.last().toLowerCase()
            }
        }

        question("screenId", "Screen id") {
            default { answers ->
                val entityName: String by answers

                projectModel.namespace + "$" + entityName.split('.').last() + ".browse"
            }
        }

        question("descriptorName", "Descriptor name") {
            default { answers ->
                val entityName: String by answers

                entityName.split('.').last().toLowerCase() + "-browse"
            }

            validate {
                checkIsScreenDescriptor()
            }
        }

        question("controllerName", "Controller name") {
            default { answers -> (answers["entityName"] as String).split('.').last() + "Browse" }

            validate {
                checkIsClass()
            }
        }
    }

    override fun createModel(answers: Answers): BrowseScreenModel = BrowseScreenModel(answers)

    override fun beforeGeneration() {
        checkScreenId(model.screenId)
        checkExistence(model.packageName, descriptor = model.descriptorName, controller = model.controllerName)
    }

    override fun generate(bindings: Map<String, Any>) {
        TemplateProcessor(CubaPlugin.TEMPLATES_BASE_PATH + "browseScreen", bindings, projectModel.platformVersion) {
            transformWhole()
        }

        val webModule = projectStructure.getModule(ModuleStructure.WEB_MODULE)

        addToScreensXml(model.screenId, model.packageName, model.descriptorName)

        val messages = webModule.resolvePackagePath(model.packageName).resolve("messages.properties")

        PropertiesHelper(messages) {
            set("browseCaption", model.entityName + " browser")
        }

        addToMenu(webModule.rootPackageDirectory.resolve("web-menu.xml"), model.screenId, "${model.entityName}s")
    }

}