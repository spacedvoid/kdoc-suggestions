/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.spacedvoid.linkincode

import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.model.DAnnotation
import org.jetbrains.dokka.model.DClass
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.model.DEnumEntry
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DInterface
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DObject
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.DTypeAlias
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.SourceSetDependent
import org.jetbrains.dokka.model.doc.CodeInline
import org.jetbrains.dokka.model.doc.Constructor
import org.jetbrains.dokka.model.doc.CustomTagWrapper
import org.jetbrains.dokka.model.doc.Description
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.DocumentationNode
import org.jetbrains.dokka.model.doc.Param
import org.jetbrains.dokka.model.doc.Property
import org.jetbrains.dokka.model.doc.Receiver
import org.jetbrains.dokka.model.doc.Return
import org.jetbrains.dokka.model.doc.TagWrapper
import org.jetbrains.dokka.model.doc.Throws
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.DokkaPluginApiPreview
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement
import org.jetbrains.dokka.transformers.documentation.DocumentableTransformer

class LinkInCodePlugin: DokkaPlugin() {
	@OptIn(DokkaPluginApiPreview::class)
	override fun pluginApiPreviewAcknowledgement(): PluginApiPreviewAcknowledgement = PluginApiPreviewAcknowledgement

	@Suppress("unused")
	val linkInCodeTransformer by extending { CoreExtensions.documentableTransformer with LinkInCodeTransformer() }
}

class LinkInCodeTransformer(): DocumentableTransformer {
	private lateinit var context: DokkaContext

	override fun invoke(original: DModule, context: DokkaContext): DModule {
		this.context = context
		return original.transform()
	}

	@Suppress("UNCHECKED_CAST")
	private fun <T: Documentable> T.transform(): T = when(this) {
		is DModule -> copy(
			packages = this.packages.map { it.transform() }
		)
		is DPackage -> copy(
			functions = this.functions.map { it.transform() },
			properties = this.properties.map { it.transform() },
			classlikes = this.classlikes.map { it.transform() },
			typealiases = this.typealiases.map { it.transform() }
		)
		is DClass -> copy(
			documentation = transformDocumentations(),
			classlikes = this.classlikes.map { it.transform() },
			functions = this.functions.map { it.transform() },
			properties = this.properties.map { it.transform() }
		)
		is DInterface -> copy(
			documentation = transformDocumentations(),
			classlikes = this.classlikes.map { it.transform() },
			functions = this.functions.map { it.transform() },
			properties = this.properties.map { it.transform() }
		)
		is DEnum -> copy(
			documentation = transformDocumentations(),
			classlikes = this.classlikes.map { it.transform() },
			functions = this.functions.map { it.transform() },
			properties = this.properties.map { it.transform() }
		)
		is DObject -> copy(
			documentation = transformDocumentations(),
			classlikes = this.classlikes.map { it.transform() },
			functions = this.functions.map { it.transform() },
			properties = this.properties.map { it.transform() }
		)
		is DAnnotation -> copy(
			documentation = transformDocumentations(),
			classlikes = this.classlikes.map { it.transform() },
			functions = this.functions.map { it.transform() },
			properties = this.properties.map { it.transform() }
		)
		is DTypeAlias -> copy(
			documentation = transformDocumentations()
		)
		is DProperty -> copy(
			documentation = transformDocumentations()
		)
		is DFunction -> copy(
			documentation = transformDocumentations()
		)
		is DEnumEntry -> copy(
			documentation = transformDocumentations()
		)
		is DParameter -> copy(
			documentation = transformDocumentations()
		)
		else -> run {
			this@LinkInCodeTransformer.context.logger.warn("Unknown documentable during LinkInCode transformation: $this")
			return@run this
		}
	} as T

	private fun Documentable.transformDocumentations(): SourceSetDependent<DocumentationNode> {
		val result = mutableMapOf<DokkaConfiguration.DokkaSourceSet, DocumentationNode>()
		this.documentation.forEach { (sourceSet, documentation) ->
			result[sourceSet] = documentation.copy(
				children = documentation.children.map { it.transformTagWrapper() }
			)
		}
		return result
	}

	private fun TagWrapper.transformTagWrapper(): TagWrapper = when(this) {
		is Constructor -> copy(root = this.root.copy(children = this.root.children.linkInCode()))
		is Description -> copy(root = this.root.copy(children = this.root.children.linkInCode()))
		is CustomTagWrapper -> copy(root = this.root.copy(children = this.root.children.linkInCode()))
		is Param -> copy(root = this.root.copy(children = this.root.children.linkInCode()))
		is Property -> copy(root = this.root.copy(children = this.root.children.linkInCode()))
		is Throws -> copy(root = this.root.copy(children = this.root.children.linkInCode()))
		is Receiver -> copy(root = this.root.copy(children = this.root.children.linkInCode()))
		is Return -> copy(root = this.root.copy(children = this.root.children.linkInCode()))
		else -> run {
			this@LinkInCodeTransformer.context.logger.debug("Skipping transformation of tag wrapper during LinkInCode transformation: $this")
			return@run this
		}
	}

	private fun List<DocTag>.linkInCode(): List<DocTag> {
		// These two cannot be non-null simultaneously
		/**
		 * The link that is waiting for a (possible) inline code block to merge.
		 *
		 * Not added to the `result` yet.
		 */
		var awaitingLink: DocumentationLink? = null
		/**
		 * The inline code block that can merge upcoming links.
		 *
		 * Not added to the `result` yet.
		 */
		var currentCode: CodeInline? = null
		val result = mutableListOf<DocTag>()
		for(tag in this) when(tag) {
			is CodeInline -> {
				if(awaitingLink != null) {
					currentCode = tag.copy(children = listOf(awaitingLink) + tag.children)
					awaitingLink = null
				}
				else if(currentCode != null) currentCode = currentCode.copy(children = currentCode.children + tag.children)
				else currentCode = tag
			}
			is DocumentationLink -> {
				if(currentCode != null) currentCode = currentCode.copy(children = currentCode.children + tag)
				else if(awaitingLink != null) {
					// We do not assume that consecutive links are present. These are most likely documentation errors.
					result += awaitingLink
					awaitingLink = tag
				}
				else awaitingLink = tag
			}
			else -> {
				if(awaitingLink != null) {
					result += awaitingLink
					awaitingLink = null
				}
				else if(currentCode != null) {
					result += currentCode
					currentCode = null
				}
				result += tag.copy(children = tag.children.linkInCode())
			}
		}
		if(awaitingLink != null) result += awaitingLink
		else if(currentCode != null) result += currentCode
		return result
	}
}
