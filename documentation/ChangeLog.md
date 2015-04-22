## RustDT release ChangeLog

### next
 * Added: Open Definition functionality using Racer.
 * Added: Code Completion functionality using Racer.
  * Added: auto-insert of function arguments for Content Assist function proposals.
  * [Doc] Added [Content Assist / Open Definition](documentation/UserGuide.md#content-assistopen-definition) section.
 * Added: Code snippets proposals for Content Assist. Configurable in `Preferences/Rust/Editor/Code Snippets`. TODO more defaults
 * [Doc] Added [Editor auto-indentation](documentation/UserGuide.md#editor-newline-auto-indentation) section.

### 0.1.1
 * Added: Source menu with shift right/left operations.
 * Added: Toggle Comment action (shortcut: Ctrl+/) to Source Menu.
 * Added: Editor Go To Matching Bracket action (shortcut: Ctrl+Shift+P).
 * Fixed #2: Lifetimes are incorrectly highlighted as char
 * Fixed: "Attribute" syntax highlighting option missing from Source Coloring preference page.
 * Added: Syntax highlighting for lifetime token.
 * Added: Syntax highlighting for strings inside attribute region.
 * Fixed minor bug with block comments syntax highlighting.
 * [Doc] Fixed: missing UserGuide screenshot of debug launch configuration.
 * [Doc] Added: Installation guide note for users in China.

### RustDT 0.1.0 - Initial release 
 * Added: Rust source code editor, with:
   * Syntax highlighting. Configurable in `Preferences/Rust/Editor/Source Coloring`. 
   * Automatic indent/de-indent and brace completion on certain keypresses (Enter, Backspace). Configurable in `Preferences/Rust/Editor/Typing`.
 * Added: Rust/Cargo Project wizard.
 * Added: Rust/Cargo project builder.
   * With in-editor build errors reporting.
 * Added: Debugging support using [CDT](https://eclipse.org/cdt/)'s GDB integration. 
 
