## User Guide

*Note:* For an overview of RustDT features, see [Features](Features.md#ddt-features). This also serves to document 
the major functionalities available.

### Eclipse basics

If you are new to Eclipse, you can learn some of the basics of the Eclipse IDE with this short intro article: 
[An introduction to Eclipse for Visual Studio users
](http://www.ibm.com/developerworks/opensource/library/os-eclipse-visualstudio/)

Also, to improve Eclipse performance and startup time, it is recommended you tweak the JVM parameters. There is a tool called Eclipse Optimizer that can do that automatically, it is recommended you use it. Read more about it [here](http://www.infoq.com/news/2015/03/eclipse-optimizer). (Installing/enabling the JRebel optimization is not necessary as that only applies to Java developers)

### Project setup

##### Project Creation:
A new Rust project can be created in the Project Explorer view. Open `New / Project...` and then `Rust / Rust Cargo Project`. This wizard can also be used to import existing Cargo projects into Eclipse.

##### Project Building

The [Rust installation](http://www.rust-lang.org/install.html) is required for project building. The path to the installation should be configured in `Preferences/Rust`.  

### Editor and Navigation

##### Editor newline auto-indentation:
The editor will auto-indent new lines after an Enter is pressed. Pressing Backspace with the cursor after the indent characters in the start of the line will delete the indent and preceding newline, thus joining the rest of the line with the previous line. Pressing Delete before a newline will have an identical effect.
This is unlike most source editors - if instead you want to just remove one level of indent (or delete the preceding Tab), press Shift-Tab. 

##### Content Assist / Open Definition:
Content Assist (also know as Code Completion, Auto Complete) is invoked with `Ctrl-Space`. 
Content Assist is provided by means of the [Racer](https://github.com/phildawes/racer) tool. 
You must install this tool, and then configure its location in the `Rust` preference page.  

The Open Definition functionality is invoked by pressing F3 in the source editor. 
Open Definition is also available in the editor context menu and by means of editor *hyper-linking* 
(hold Ctrl and click on a reference with the mouse cursor). 
The [Racer](https://github.com/phildawes/racer) tool is also used to provide this functionality. 

> If there is a problem with these operations, and you need a diagnostics log, 
the output of Racer can be seen in the `RustDT Racer log` console page in the Eclipse Console view.


### Launching:
To run a Rust project that builds to an executable, you will need to create a launch configuration. Locate the main menu, open 'Run' / 'Run Configurations...'. Then double click 'Rust Application" to create a new Rust launch, and configure it accordingly. You can run these launches from the 'Run Configurations...', or for quicker access, from the Launch button in the Eclipse toolbar.

Alternatively, to automatically create and run a launch configuration (if a matching one doesn't exist already), you can select a Rust project in the workspace explorer, open the context menu, and do 'Run As...' / 'Rust Application'. (or 'Debug As...' for debugging instead). If a matching configuration exists already, that one will be run.

Whenever a launch is requested, a build will be performed beforehand. This behavior can be configured under general Eclipse settings, or in the launch configuration.

### Debugging
You can debug a Rust program by running a launch in debug mode. You will need a GDB debugger. To configure debug options (in particular, the path to the debugger to use), open the launch under 'Run' / 'Debug Configurations...', and then navigate to the 'Debugger' tab in the desired launch configuration:

<div align="center">
<a><img src="screenshots/UserGuide_DebuggerLaunchConfiguration.png" /><a/> 
</div>

GDB debugger integration is achieved by using the CDT plugins. To configure global debugger options, go the 'C/C++'/'Debug'/'GDB' preference page.

**Note that for debugging to work**, the program must be compiled with debug symbols information, and those debug symbols must be on a format that GDB understands. Otherwise you will get GDB error messages such "(no debugging symbols found)" or "file format not recognized".

**Windows note:** Using Cygwin GDB doesn't work very well, if at all. The recommended way to debug in Windows is to use the GDB of [mingw-w64](http://mingw-w64.org/), or the one of [TDM-GCC](http://tdm-gcc.tdragon.net/)

##### GDB Pretty printers
RustDT will try to automatically set up the rust-gdb pretty printing scripts when launching in debug mode. GDB will be configured to load them from the `${RUST_ROOT}/lib/rustlib/etc` location, where `${RUST_ROOT}` is the directory of the Rust installation, as configured in the RustDT preferences.
