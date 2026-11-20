# Markdown support

Established by the Phase 4 spike (commit `da01a5a`) against
multiplatform-markdown-renderer 0.46.0 (GFM flavour) with the
multiplatform-markdown-renderer-code module (dev.snipme:highlights 1.2.0)
and the coil3 image transformer (Coil 3.6.3). Rendering behaviour is
verified on device by the project owner; this document records what the
integration provides, what the app customises, and the known limits.

## Supported (rendered natively)

- Headings H1-H6, paragraphs, bold, italic, bold-italic, strikethrough
- Inline code and fenced/indented code blocks with syntax highlighting
  for common languages (Kotlin, Java, JavaScript, Python, JSON, XML,
  Bash and others provided by Highlights)
- Links (inline, reference-style and autolinks) and images (inline and
  reference-style), including remote http(s) images and SVG (badges)
- Blockquotes, horizontal rules, hard line breaks
- Ordered, unordered and nested lists; GFM task lists
- GFM tables with column alignment; wide tables scroll horizontally
- GFM alerts (`> [!NOTE]`, `> [!TIP]`, `> [!WARNING]`, ...)
- Highlight spans (`==text==`) and emoji shortcodes are not part of GFM
  and are not rendered as graphics

## Customised to match the design

- Headings H1 and H2 get the GitHub-style divider underneath
- Task-list checkboxes are drawn as rounded squares (green when checked);
  the library default renders literal `[x]` text
- Table header row is filled with the design's yellow accent inside the
  rounded beige container
- Code blocks use the deep-black container with cream monospace text
- Text follows the app palette and the Outfit/Cairo type styles;
  paragraphs use content-based bidirectional layout, so Arabic paragraphs
  in an English document (and vice versa) align correctly

## Images

- Images are rendered by the app itself (Coil 3 SubcomposeAsyncImage
  against the app-wide image loader), not by the renderer library's
  default elements: the defaults paint through a painter whose loading
  failure is an invisible empty box. The app's elements show a loading
  placeholder with the alt text, the image at its natural aspect on
  success, and a visible "image unavailable" placeholder on error —
  an image never fails silently
- http(s) images load over the network (INTERNET permission; the app
  requests no storage permission), including SVG badges
- Relative image paths are resolved against the opened document's folder
  (including `../` segments) when the folder is reachable through the
  document's provider, and loaded from the resulting document URI
- Images written as raw HTML (`<img>` tags) are not rendered — raw HTML
  is not parsed (see the limits section)

## Links

- http(s) and mailto links open in the browser / mail app
- A relative link to another `.md` file opens inside the app when it can
  be resolved the same way as relative images
- `#anchor` links are accepted but do not scroll in this version
  (the renderer lays content out in one column without per-heading
  scroll targets); this is the documented fallback

## Not supported

- Raw HTML blocks and inline HTML tags are not rendered as UI
- Footnotes, mathematics and Mermaid diagrams (outside GFM)
- Table cells are single-line in the underlying renderer: very long cell
  text is ellipsised, and cell content cannot wrap

## Performance notes

- Parsing happens off the main composition path; the viewer shows a
  loading state while a document is read and parsed
- Very large documents (multiple MB) render as one long column; the
  practical comfort limit is verified on device with the samples in
  `docs/samples/`
