
# Troubleshooting

## Development

**My IDE complains about diamond operator/lambda syntax not being supported!**

This is due to support for the required language level (8) not being present or not being correctly set. Check the project setup dialog in your IDE.

**My IDE complains about package names! It expects that the package names be prefixed with `main.java.` or something similar.**

This is likely due to the source folders not being recognized as such.

Open the project setup dialog in your IDE and register the following directories as source directories:

- `src/main/java`
- `src/main/resources`
- `src/test/java` (add this as a test directory if your IDE supports it)

**After updating my branch, my IDE complains about libraries not being on the classpath!**

You may need to refresh the IDE's Gradle configuration.

If this fails, re-import the project.

**Eclipse refuses to import HubTurbo as a Gradle project!**

HubTurbo uses a nested source set to selectively run stable/unstable tests. Eclipse's BuildShip plugin doesn't support this.

One solution is to create the Eclipse project manually, adding the libraries downloaded by Gradle to the classpath. Another is to use IntelliJ IDEA instead, as its Gradle plugin does support nested source sets.

## CI and Testing

**How can I see how the CI build failed?**

Refer [here](workflow.md), under the section for running all tests and static analysis.

**I've managed to reproduce the problem locally but can't figure out why it happens!**

See if the problem is [documented](testing.md), otherwise contact a core team member or open an issue for help.

## Miscellaneous

**My problem isn't listed here!**

Feel free to ask for help on the [mailing list](https://groups.google.com/forum/#!forum/hubturbo-contributors).
