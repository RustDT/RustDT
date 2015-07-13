## RustDT release ChangeLog

### (NextVersion)

### 0.2.2
  ▶ Recommended/tested CDT version is now 8.7
 * Fixed #44: Debugging not working on Eclipse 4.5 Mars (CDT 8.7), for certain platform/GDB combinations.
 * Fixed #43: PATH of external tools bungled if original PATH not set (Windows)
 * Doc: added note about Cygwin GDB not being recommended for debugging. 

### 0.2.1
 * Added Content Assist preference page, with auto-activation options (#23).
 * Improved transparency/aliasing of content assist icons - this improves them for dark themes.
 * Upgraded minimum Java version to Java 8
  * Added: Show error message dialog if starting Eclipse with a Java version below the minimum.
 * Added: Content Assist proposal list now shows a popup on the right with a small description of the selected proposal.
  * However, this is based on information provided by Racer, which at the moment is fairly limited.

Fixes:
 * Fixed #31: Added workaround to fix OS X issue "Could not execute process `rustc -vV`". 
 * Fixed #27: the preference pages are now searchable in the Preferences dialog search field, by means of relevant keywords.
 * Fixed #39: Unable to parse multiline rustc error messages
 * Fixed #37: No editor problem underlining for macro expansion errors
 * Fixed #36: Autocompletion error `Unknown line format:` on std::process::Command.
 * Fixed: snippets Content Assist preview information hover not showing up.
 * Fixed: `Tab policy: "Spaces Only"` preference ignored when pressing TAB to indent.


### 0.2.0
 * Added: Open Definition functionality using Racer.
 * Added: Content Assist functionality using Racer.
  * Added: Content Assist auto-insert of function arguments for function proposals.
  * Added: Content Assist code snippet proposals. Configurable in `Preferences/Rust/Editor/Code Snippets`. (needs more defaults)
  * [Doc] Added [Content Assist / Open Definition](documentation/UserGuide.md#content-assist--open-definition) section.
 * Added: Automatically set up Rust GDB pretty-printers for debug launches. (#6)
  * [Doc] Added [GDB pretty-printers](documentation/UserGuide.md#gdb-pretty-printers) section.
  * Note: I couldn't get this to work on any Windows GDB (Cygwin, TDM-GCC, MinGW-w64).
 * Upgraded minimum CDT version to 8.6.0.
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
 
