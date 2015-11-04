## release ChangeLog

### (NextVersion)
 * Added Racer preference to enable/disable showing the error dialog if failures occur during Content Assist. (#74)
 * Fixed: project Build Targets settings pages shows wrong default for Program Path field
 
### 0.4.0
 * Added support for Eclipse dark theme. Namely:  
   * Syntax/source coloring now have different settings for dark theme.
   * Fixed Tools Console colors, as well as content assist pop-up colors. 
 * Added number literals source highlighting. (#55)
 * Added macro invocation source highlighting.
 * Syntax highlighting now works on the source compare editor/viewer.
 * Added per-project Rust Installation preferences. (fixes #63)
 * Added Content Assist support of name-only proposal insertion by pressing `Ctrl+Enter`. (only applicable to proposals that insert arguments)
 * Changed: newly created launch configurations now have the debug option "Stop on startup at:" set to false by default. This way debugging won't stop on the C `main`, which is essentially useless outside of C/C++.
 * Added: `cargo update` action to Project explorer context menu. (#17)

--
 * Fixed: Properly implemented editor highlighting of Rust attributes with strings in them. (#24)
 * Fixed: AssertionFailureException pressing Enter after source with more closing braces than opening ones.
 * Fixed: Unindent (Shift-Tab) broken, does nothing after empty lines in selection.
 * Fixed "Unknown line format: " when invoking autocompletion with racer. (#68)
 

### 0.3.0
 * Added support for Build Targets, based on Cargo build targets.
   * Available Build Targets are displayed in the Project Explorer.  You can configure which targets are enabled or not for a workspace build. Or run/debug a specific target.
   * Top-level Build Targets are: `crate#no-tests` and `crate#tests`. You can run/debug specific Cargo binaries or tests from the context menu of the Build Targets.
   * Added Project Build Configuration property page to configure Build Target options.
 * Added specific Project Explorer icons for Rust crate elements: the `src`, `test`, `target` folders; the `Cargo.toml` file.
 * Added Project Explorer element with Cargo dependency references.
 * Added: launch shortcut for a Rust project and Rust integration tests. This is a quick way to create a launch from the Project Explorer context-menu, by selecting either a project, or a Rust test source file in `tests/`, and selecting "Run As" or "Debug As".
 * Added new [Configuration](documentation/UserGuide.md#configuration) section in the User Guide. (#46)
 * Added UserGuide note about using Homebrew GDB in OS X.

Fixes:

 * Fixed: if build tool reports many error messages, the Eclipse project build will take too long to finish. (#26)
 * Fixed: Properly parse Racer "END" line syntax, don't show error when this occurs. (#57)
 * Fixed: when invoking Rust tools like Cargo, add tool directory to beginning of PATH, not end. (#59)
 * Fixed: Don't show error dialog when Racer fails. Only the editor status line is updated now. (#41)
 * Fixed: Occasional AssertionFailure when creating new projects in nested locations (project would not show up in Explorer).
 * Fixed: Arguments field in launch configuration is not multi-line.

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
 
