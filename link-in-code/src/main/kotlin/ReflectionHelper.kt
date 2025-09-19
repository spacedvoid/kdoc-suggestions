/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.spacedvoid.linkincode

import org.jetbrains.dokka.model.doc.DocTag
import kotlin.reflect.full.declaredMemberFunctions

/**
 * A bit of dirty hack:
 * we can't list all the types of [DocTag] in a `when`,
 * so we just use reflection to get the copy method and invoke it.
 */
fun DocTag.copy(children: List<DocTag> = this.children, params: Map<String, String> = this.params): DocTag {
	val copyMethod = this::class.declaredMemberFunctions.firstOrNull { it.name == "copy" }
		?: throw IllegalArgumentException("Unknown DocTag during LinkInCode transformation: $this")
	return copyMethod.callBy(mapOf(
		copyMethod.parameters[0] to this,
		copyMethod.parameters.first { it.name == "children" } to children,
		copyMethod.parameters.first { it.name == "params" } to params
	)) as DocTag
}
