## RustDT release ChangeLog

### (NextVersion)
 * Added: Source menu with shift rigth/left operations.
 * Fixed: missing UserGuide screenshot of debug launch configuration.
 * Doc - Installation guide: Added note for users in China.
 * Added: Toggle Comment action (Ctrl+/) .
 * Added editor Go To Matching Bracket action (shortcut: Ctrl+Shift+P)
 * Fixed minor bug with block comments syntax highlighting.
 * Fixed #2: Lifetimes are incorrectly hilighted as char
 * Fixed: "Attribute" syntax highlighting option missing from Source Coloring preference page.
 * Added: Can now do syntax highlighting for lifetime token.

### RustDT 0.1.0 - Initial release 
 * Added: Rust source code editor, with:
   * Syntax highlighting. Configurable in `Preferences/Rust/Editor/Source Coloring`. 
   * Automatic indent/de-indent and brace completion on certain keypresses (Enter, Backspace). Configurable in `Preferences/Rust/Editor/Typing`.
 * Added: Rust/Cargo Project wizard.
 * Added: Rust/Cargo project builder.
   * With in-editor build errors reporting.
 * Added: Debugging support using [CDT](https://eclipse.org/cdt/)'s GDB integration. 
 
