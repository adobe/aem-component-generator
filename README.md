# AEM Component Generator

[![CircleCI](https://circleci.com/gh/adobe/aem-component-generator.svg?style=svg)](https://circleci.com/gh/adobe/aem-component-generator)

AEM Component Generator is a java project that enables developers to generate the base structure of an
AEM component using a JSON configuration file specifying component and dialog properties and other configuration
options.

Generated code includes:
- `cq:dialog` for component properties
    - `dialogshared`/`dialogglobal` for shared/global component properties
    - Supports all basic field types, multifields, and image upload fields
- Sling Model
    - Includes fully coded interface and implementation classes
    - Follows WCM Core component standards
    - Enables FE-only development for most authorable components
- HTL file for rendering the component
    - Includes an object reference to the Sling Model
    - Includes the default WCM Core placeholder template for when the component is not yet configured
- Stubbed clientlib (JS/CSS) following component client library patterns of WCM Core 

## AEM Component Generator in action,
[Watch this video](https://s3.amazonaws.com/HS2Presentations/AEMPublic/2019-Adobe-AEM-Component-Code-Generator-Demo-Bounteous.mp4).
Detailed steps for using the generator are found below. 

### How to install and run

- First of all clone the project from github.
- Running `mvn clean install` from the main project folder.
	- This generates `component-generator-N.N.jar`

> Experience AEM Component Generator with npm and node.js
- [Integration with node](doc/Integration-with-node.md)

### How to customize generation

[Explaining data-config.json](doc/Explaining-data-config.md)

### How to use in your AEM project

##### Step 1: Update the data-config file
- Copy data-config `src/main/resources/component-generator/data-config.json` to your project
- Adapt data-config to your company defaults, removing references to `NewCo`/`newco` in the `project-settings` and `group` values.

##### Step 2: Copy the component generator JAR 
- Copy `target/component-generator-N.N.jar` file (under the `target` folder) to a location
from which you wish to generate AEM component code.  Note that code will be generated at a relative path from which
the generator is executed, which can be different from where the jar file is located.

##### Step 3: To generate a component, navigate to the main folder of your AEM project and execute the following command.
Note that paths specified in `project-settings` configs (above) will be relative to this location.
    ```sh
    $ java -jar <jarfile> <configfile>
    ```
    - `jarfile`: pat```h to `component-generator-N.N.jar` file (replacing `N.N` with the applicable numbers)
    - `configfile`: path to `data-config.json` file
    Example:
    ```sh
    $ java -jar target/component-generator-1.0.jar src/main/resources/component-generator/data-config.json
    ```
- Generates
    - target/generated-sources/src/main/java
    - target/generated-sources/ui.apps

	Successful component generation should result in output similar to the following:
	```
	[17:57:50.427 [INFO ] CommonUtils @93] - Created: ui.apps/src/main/content/jcr_root/apps/newco/components/content/demo-comp/.content.xml
	[17:57:50.441 [INFO ] CommonUtils @93] - Created: ui.apps/src/main/content/jcr_root/apps/newco/components/content/demo-comp/_cq_dialog/.content.xml
	[17:57:50.443 [INFO ] CommonUtils @93] - Created: ui.apps/src/main/content/jcr_root/apps/newco/components/content/demo-comp/dialogglobal/.content.xml
	[17:57:50.446 [INFO ] CommonUtils @93] - Created: ui.apps/src/main/content/jcr_root/apps/newco/components/content/demo-comp/dialogshared/.content.xml
	[17:57:50.447 [INFO ] CommonUtils @93] - Created: ui.apps/src/main/content/jcr_root/apps/newco/components/content/demo-comp/clientlibs/.content.xml
	[17:57:50.453 [INFO ] CommonUtils @93] - Created: ui.apps/src/main/content/jcr_root/apps/newco/components/content/demo-comp/clientlibs/site/css/demo-comp.less
	[17:57:50.454 [INFO ] CommonUtils @93] - Created: ui.apps/src/main/content/jcr_root/apps/newco/components/content/demo-comp/clientlibs/site/js/demo-comp.js
	[17:57:50.456 [INFO ] CommonUtils @93] - Created: ui.apps/src/main/content/jcr_root/apps/newco/components/content/demo-comp/demo-comp.html
	[17:57:50.456 [INFO ] ComponentUtils @85] - --------------* Component 'demo-comp' successfully generated *--------------
	[17:57:50.476 [INFO ] CommonUtils @93] - Created: core/src/main/java/com/newco/aem/base/core/models/DemoComp.java
	[17:57:50.488 [INFO ] CommonUtils @93] - Created: core/src/main/java/com/newco/aem/base/core/models/impl/DemoCompImpl.java
	[17:57:50.488 [INFO ] JavaCodeModel @103] - --------------* Sling Model successfully generated *--------------
	```

## Contributing
 
Originally developed and contributed by [Bounteous](https://www.bounteous.com/insights/2019/07/31/aem-component-generator/).

Contributions are welcomed! Read the [Contributing Guide](.github/CONTRIBUTING.md) for more information.
 
## Licensing
 
This project is licensed under the Apache V2 License. See [LICENSE](LICENSE) for more information.
