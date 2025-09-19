# Link-in-Code

The concept is simple:
if a link(`[]`) and a code(` `` `) comes next to each other,
format the link to be inside the code block, as the declaration of the class/method/property/etc. uses.

## Recommended custom styling

The following css is recommended to be included as a `styles.css` file for Dokka:
```css
code {
	padding: 0.3rem;
}
```
This is because Dokka requires a bit bigger space than normal text because of the underline.

## Known limitations

Because Dokka(or the Markdown parser) [cannot handle back-by-back links and inline codes properly](https://youtrack.jetbrains.com/issue/KTIJ-35552),
*always* qualify the link, such as `` [size][List.size]` - 1` ``.

Also, until https://youtrack.jetbrains.com/issue/KTIJ-29221 gets fixed,
leading spaces in inline codes might not be displayed.

# License

The source code is licensed with [MPL 2.0](LICENSE), *compatible* with secondary licenses.
