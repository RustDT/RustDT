## Installation

#### Installation Requirements: 
 * Java VM version 8 or later.
 * Eclipse 4.6 (Neon) or later.
 * CDT 9.0 or later (this will be installed or updated automatically as part of the steps below).

#### Instructions:
 1. Use your existing Eclipse, or download a new Eclipse package from http://www.eclipse.org/downloads/. 
    * For an Eclipse package without any other IDEs or extras (such a VCS tools), download the ["Platform Runtime Binary"](http://archive.eclipse.org/eclipse/downloads/drops4/R-4.6-201606061100/#PlatformRuntime). 
 1. Start Eclipse, go to `Help -> Install New Software...`
 1. Click the `Add...` button to add a new update site, enter the URL: **https://rustdt.github.io/releases/** in the Location field, click OK.
 1. Select the recently added update site in the `Work with:` dropdown. Type `RustDT` in the filter box. Now the RustDT feature should appear below.
 1. Select the `RustDT` feature, make sure "Contact all update sites during install to find required software" is enabled, and complete the wizard. 
    * RustDT dependencies such as CDT will automatically be added during installation.
 1. Restart Eclipse. 
 1. Follow the instructions from the User Guide's [Configuration section](UserGuide.md#configuration) to configure the required external tools. It is recommended you read the rest of the guide too.  
 
#### Updating:
If you already have RustDT installed, and want to update it to a newer release, click `Help / Check for Updates...`.

#### :cn: *Note for users in China*
Note: if you are behind the Great Firewall of China, you are very likely to encounter problems installing RustDT: blocked connections, timeouts, or slow downloads. This is because the update site is hosted in Github, which is blocked or has limited access. These alternative instructions should let you perform the installation:

 1. Download an Eclipse installation which already contains CDT (such as the "Eclipse IDE for C/C++ Developers" package), so CDT doesn't have to be downloaded during installation.
 1. Download the website from https://github.com/RustDT/rustdt.github.io/archive/master.zip, unpack the archive and use the `releases` directory as a Local repository instead of the Update Site URL. Uncheck the option "Contact all updates sites during installation to find required software" so only the local repository is used. 

  * Note however: you will likely need to re-download the archive above whenever you want to update RustDT to a newer version.
