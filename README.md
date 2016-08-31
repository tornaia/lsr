# <tt style="color:limegreen">LSR</tt> - Large Scale Refactor

<tt style="color:limegreen">lsr</tt> is a refactoring tool that can move Maven dependencies and dependent Java files from one module to an another - possible new - one.

### Build and debug

To build the plugin, you need to have IntelliJ libraries installed in your Maven repository.
If you're Linux/Mac user, you can use `install-intellij-libs.sh` script, if that doesn't work, you can
install those JARs manually (only dependencies from POM are required).

After that, you can build the plugin with `mvn package` command. The resulting ZIP file in `target/` directory can
be installed by going to `Settings > Plugins > Install plugin from disk...`.

To debug the plugin in IntelliJ IDEA, you need only to add a new `Plugin Sandbox Environment` under `Run/Debug Configurations`. Use the classpath of module lsr-plugin-idea and set `IntelliJ IDEA IU-145...` as JRE.

Requirement

IntelliJ Idea 2016.2.3 and above.

### Usage

Open your pom file and right click on any dependency `Refactor > Maven > Large Scale Refactor`.

### Credits

Many thanks to the developers of JBoss Forge IntelliJ IDEA Plugin. I used their project as an example.

### More to read

https://tornaiandras.com/tag/large-scale-refactor/
