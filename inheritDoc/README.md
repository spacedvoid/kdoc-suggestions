# `@inheritDoc`

Currently, Dokka does not support the tag `@inheritDoc`.
To implement this, we can just copy-paste the whole documentation from parents,
but let's give it some more abilities.

# 0. Definitions

<dl>
<dt>Element</dt>
<dd>The method or property that has the `@inheritDoc` tag in its documentation</dd>
<dt>Container</dt>
<dd>The class or interface that contains the element</dd>
<dt>Parent</dt>
<dd>A method or property that has the same(or compatible) signature that the element can override</dd>
<dt>(Parent) Source</dt>
<dd>The class or interface that contains a parent</dd>
</dl>

# 1. Basic behavior

The tag `@inheritDoc` is applicable to only methods and properties.
Showing up at any other locations will cause an error.

When present, the whole tag is replaced by the documentation of the parent.
Any documentations before or after the tag will be preserved.

## 1.1. Resolving parents

If an element has the `@inheritDoc` tag, we first get the supertypes of the element's container.
Starting with the first supertype by declaration order, we find a parent of the element.
The search is depth first: we find a parent in the first supertype, then the next, and so on.
However, if the parent in a supertype has no documentation, or does not explicitly override it,
we search for its parents with a documentation as the same way above.
But if no parents have documentations, a warning is raised, and the tag is just removed.
If no parents can be found at all, an error is raised, meaning that the element is not a proper override.

# 2. Parameters

`@inheritDoc` accepts an optional parameter, called *requested parent source*.
The parameter must be a type name, which is either a class, interface, or a typealias.
If the specified type is not a supertype of the container, an error is raised.

If this parameter is present, the tag targets the parent in the specified source,
prioritized over any other supertypes.
It forces the tag to copy the documentation from that specific parent source.

If the specified source does not have a parent that has documentation or is explicitly overridden,
we find the parent as the same way from [1.1](#11-resolving-parents).

If the specified source does not have a parent at all, an error is raised,
meaning that the element does not override any methods or properties from the given source.

# 3. Other tags

If the parent contains block tags in its documentation, they are handled differently.

- `@param`, `@return`, `@receiver`

These tags are copied as-is.

- `@throws`, `@see`, `@author`, `@since`, and any other tags

These tags are not copied.

# 3.1. Overriding tags

If the same tag is present in the override that is also present in the parent,
they are *overridden*, replaced by the overriding tag.

This is applied to each individual tag:
for example, `@param a` and `@param b` are two separate tags, and can be overridden or inherited individually.

# 4. Known limitations

We do not have access to Kotlin Analysis API, so we cannot determine the parents ourselves.
