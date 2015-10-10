## Installation

#### Requirements: 
 * Eclipse 4.5 (Mars) or later.
 * Java VM version 8 or later.

#### Instructions:
 1. Use your existing Eclipse, or download a new Eclipse package from http://www.eclipse.org/downloads/. 
  * For an Eclipse package without any other IDEs or extras (such a VCS tools), download the ["Platform Runtime Binary"](http://archive.eclipse.org/eclipse/downloads/drops4/R-4.5-201506032000/#PlatformRuntime). 
 1. Start Eclipse, go to `Help -> Install New Software...`
 1. Click the `Add...` button to add a new update site, enter the URL: **https://rustdt.github.io/releases/** in the Location field, click OK.
 1. Select the recently added update site in the `Work with:` dropdown. Type `RustDT` in the filter box. Now the RustDT feature should appear below.
 1. Select the `RustDT` feature, and complete the wizard. 
  * RustDT dependencies such as CDT will automatically be added during installation.
 1. Restart Eclipse. Then proceed to the Configuration section ahead, and also please read the [User Guide](UserGuide.md).

## Configuration

A [Rust installation](http://www.rust-lang.org/install.html) is required for most IDE functionality. The path to the installation should be configured in the `Rust` preference page, accessed from the menu `Window / Preferences`.

Additionally, for functionality such as code completion, you will need:
 * The [Racer](https://github.com/phildawes/racer) tool. The Racer preferences field can be configured with just the executable name, in which case, the executable will be searched in the PATH environment variable.
 * The Rust sources package. This package is not included in the Rust intallation, so it must be obtained separately (see [this Rust bug](https://github.com/rust-lang/rust/issues/19535)). Configure the `Rust 'src' Directory` to point to the 'src' directory of this package.
 
 
#### Updating:
If you already have RustDT installed, and want to update it to a newer release, click `Help / Check for Updates...`.

#### :cn: *Note for users in China*
Note: if you are behind the Great Firewall of China, you are very likely to encounter problems installing RustDT: blocked connections, timeouts, or slow downloads. This is because the update site is hosted in Github, which is blocked or has limited access. These alternative steps might help you perform the installation:

* Download the website from https://github.com/RustDT/rustdt.github.io/archive/master.zip, unpack the archive and use the `releases` directory as a Local repository instead of the Update Site URL. However, you will need to redownload the archive above whenever you want to update RustDT to a newer version.
* Download an Eclipse installation which already contains CDT (C Development Tools), so it doesn't have to be installed at the same time as RustDT.
