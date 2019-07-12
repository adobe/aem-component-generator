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

## How To Use

To see the AEM Component Generator in action,
[watch this video](https://s3.amazonaws.com/HS2Presentations/AEMPublic/2019-Adobe-AEM-Component-Code-Generator-Demo-Bounteous.mp4).
Detailed steps for using the generator are found below. 

Step 1: Clone the project from github.

Step 2: Update placeholder references in the codebase.
- Replace the `<CODEOWNER>` placeholder in resource files (`aem-component-generator/src/main/resources`) with
the name of your company.
- Update the demo config file (`data-config.json`) to your company defaults, removing references to `NewCo`/`newco`
in the `project-settings` and `group` values.

Step 3: Build the project by running `mvn clean install` from the main project folder.

Step 4: Copy the generated `component-generator-N.N.jar` file (under the `target` folder) to a location
from which you wish to generate AEM component code.  Note that code will be generated at a relative path from which
the generator is executed, which can be different from where the jar file is located.

Step 5: Copy the `data-config.json` file from this project to the same location and update with relevant configs for
your component.

- `project-settings`: contains configuration options related to your AEM project
- `project-settings.bundle-path`: path to the java code of your main bundle
- `project-settings.apps-path`: path to the `/apps` root
- `project-settings.component-path`: path to the project's components directory, relative to the `/apps` folder
- `project-settings.model-interface-pkg`: Java package for the interface model objects
- `project-settings.model-impl-pkg`: Java package for the implementation model objects
- `name`: folder name for the component
- `title`: human readable component name, also used as the title for dialogs
- `group`: component group
- `type`: component folder type - content, form, structure
- `options.js`: whether to create an empty JS lib for the component (shared with CSS lib)
- `options.jstext`: whether to create the js text file within the clientlib. Set to `false` when this file is not needed within your clientlib.
- `options.css`: whether to create an empty CSS lib for the component (shared with JS lib)
- `options.csstext`: whether to create the css text file within the clientlib. Set to `false` when this file is not needed within your clientlib.
- `options.html`: whether to create a default HTML file for the component
- `options.slingmodel`: whether to create a sling model for the component
    - Class name is derived from converting "name" prop above to camel case (e.g. "google-maps" -> `GoogleMaps`/`GoogleMapsImpl`)
    - Fields are derived from dialog properties (see below)
- `options.content-exporter`: whether to configure sling model for content export
- `options.model-adaptables`: array of adaptables to include in the Sling Model ('request' and/or 'resource')
- `options.generic-javadoc`: whether to create generic javadoc for the getters in the model interface
- `options.properties`: properties to create in standard dialog for this component. If empty, no standard dialog will be created. This sample includes one of every possible sling:resourceType
    - `options.properties[].field`: the property "name" and java variable name.
    - `options.properties[].javadoc`: the javadoc associated with the property
    - `options.properties[].type`: the property field type
    - `options.properties[].label`: the `fieldLabel` associated with the property
    - `options.properties[].description`: the `fieldDescription` associated with the property
    - `options.properties[].items`: any child items needed for the specified property type
    - `options.properties[].attributes`: any additional attributes to be associated with property in `cq:dialog`
    - `options.properties[].model-name`: **(Multifield type Only)** the name of the sling model class generated for a multifield property
    - `options.properties[].use-existing-model`: **(Multifield type Only)**  whether or not to generate a new sling model for the multifield property
    - `options.properties[].json-expose`: by default, the content exporter will ignore all properties unless `json-expose` is set to `true`
    - `options.properties[].json-property`: the json key for the property to be used when content export is configured
- `options.properties-shared`: properties to create in shared dialog for this component. If empty, no shared dialog will be created
- `options.properties-global`: properties to create in global dialog for this component. If empty, no global dialog will be created

Step 6: To generate a component, navigate to the main folder of your AEM project and execute the following command.
Note that paths specified in `project-settings` configs (above) will be relative to this location.

```sh
$ java -jar <jarfile> <configfile>
```

- `jarfile`: path to `component-generator-N.N.jar` file (replacing `N.N` with the applicable numbers)
- `configfile`: path to `data-config.json` file

Example:
```sh
$ java -jar scripts/compgen/component-generator-1.0.jar scripts/compgen/data-config.json
```

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
 
Originally developed and contributed by Bounteous.

Contributions are welcomed! Read the [Contributing Guide](.github/CONTRIBUTING.md) for more information.
 
## Licensing
 
This project is licensed under the Apache V2 License. See [LICENSE](LICENSE) for more information.
