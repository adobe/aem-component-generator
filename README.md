# AEM Component Generator

[![CircleCI](https://circleci.com/gh/adobe/aem-component-generator.svg?style=svg)](https://circleci.com/gh/adobe/aem-component-generator)

AEM Component Generator is a java project that enables developers to generate the base structure of an
AEM component using a JSON configuration file specifying component and dialog properties and other configuration
options.

#Starting with version 2.0:

[See video on usage example](https://youtu.be/QQmm9sxK56o)

The AEM Component Generator now has graphical user interface!
- Features a React based SPA web app, with a backend API that can dynamically build out the code and configurations for AEM components

- The backend generates a component configuration JSON structure that the backend then utilizes to build out the code structure.

- Executes the code / folder structure build-out on demand from the web ui.

## Dependencies
The AEM Component Generator itself bundles all the dependencies it needs to execute.  However, the
**generated code has dependencies on ACS AEM Commons version 4.2.0+** for the following sling model injector annotations.
- `@ChildResourceFromRequest` for injecting child resources as model classes (e.g. image fields, composite multifields)
- `@SharedValueMapValue` for injecting shared/global component property field values

### How to use

-  Step 1: Clone the project from github.

-  Step 2: Build the project by running `mvn clean install` from the main project folder.

-  Step 3: Copy the generated `component-generator-N.N.jar` file (under the `target` folder) to a location
           from which you wish to generate AEM component code.  Note that code will be generated at a relative path from which
           the generator is executed, which can be different from where the jar file is located. If you are using this tool via command line only, you will need to copy a sample configuration JSON file from `/src/main/resources` and configure it as needed.
- Step 4: To generate launch the component generator web user interface, navigate to the main folder of your AEM project and execute the following command.

```sh
$ java -jar <jarfile>
```
- A new web browser tab will be launched containing the AEM component generator UI. Follow the instructions in the app to configure your new component. By default the app will bind to localhost:8080, you can override this by passing an argument `-p={port]`
- If you would like to bypass the web user interface you can generate a component via command line only by passing the path to the config:
```sh
$ java -jar <jarfile> <configfile>
```
Ex:
```sh
$ java -jar scripts/compgen/component-generator-2.0.jar scripts/compgen/data-config.json
```


### Development environment: getting started

- pre-req install yarn and Node.js (version > 12), Java 8 SDK

- in the *component-generator-ui* folder run `yarn install` and then `yarn bootstrap` to produce a production build of the react app ready for the static web server that runs from the java backend.

- In your  Java IDE of choice setup and run the main java class found in **AemCompGenerator.java**, this will launch a web browser by default with localhost:8080/

- to run the react app separately for front-end development, navigate to the *component-generator-ui* folder and run `yarn start`; This will launch a local dev version of the react based front end on port localhost:3000/
 
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

## JSON config file properties

- `project-settings`: contains configuration options related to your AEM project
- `project-settings.code-owner`: the name of the company/user this code belongs to - will replace `${CODEOWNER}` in the template files with this configured value
- `project-settings.year`: the copyright year this code belongs to - will replace `${YEAR}` in the template files with this configured value. If one is not specified, will default to the current year.
- `project-settings.bundle-path`: path to the java code of your main bundle
- `project-settings.test-path`: path to the java code of your test cases
- `project-settings.apps-path`: path to the `/apps` root
- `project-settings.component-path`: path to the project's components directory, relative to the `/apps` folder
- `project-settings.model-interface-pkg`: Java package for the interface model objects
- `project-settings.model-impl-pkg`: Java package for the implementation model objects
- `name`: folder name for the component
- `title`: human readable component name, also used as the title for dialogs
- `group`: component group
- `type`: component folder type - content, form, structure
- `options.js`: whether to create an empty JS lib for the component (shared with CSS lib)
- `options.jstxt`: whether to create the js.txt mapping file within the clientlib. Set to `false` when this file is not needed within your clientlib
- `options.css`: whether to create an empty CSS lib for the component (shared with JS lib)
- `options.csstxt`: whether to create the css.txt mapping file within the clientlib. Set to `false` when this file is not needed within your clientlib
- `options.html`: whether to create a default HTML file for the component
- `options.html-content`: generate dialog fields in the html file
- `options.slingmodel`: whether to create a sling model for the component
    - Class name is derived from converting "name" prop above to camel case (e.g. "google-maps" -> `GoogleMaps`/`GoogleMapsImpl`)
    - Fields are derived from dialog properties (see below)
- `options.testclass`: whether to create a test class for the component's sling model
    - Test methods will fail with reason as yet to be implemented.
- `options.junit-major-version`: provide major version identifier of junit to generate test classes accordingly. Currently, 4 or 5 is supported.
- `options.content-exporter`: whether to configure sling model for content export
- `options.model-adaptables`: array of adaptables to include in the Sling Model ('request' and/or 'resource')
- `options.generic-javadoc`: whether to create generic javadoc for the getters in the model interface
- `options.properties-tabs`: properties to create tabs structure in standard dialog for this component. If empty, properties will be created without tab structure
    - `options.properties-tabs[].id`: the tab "name"
    - `options.properties-tabs[].label`: the "title" of the tab
    - `options.properties-tabs[].fields`: all the properties to be added in the tab.
- `options.properties-shared-tabs`: shared properties to create tabs structure for this component in shared dialog. If empty, properties will be created without tab structure in shared dialog for this component.
    - `options.properties-shared-tabs[].id`: the tab "name"
    - `options.properties-shared-tabs[].label`: the "title" of the tab
    - `options.properties-shared-tabs[].fields`: all the properties to be added in the tab.
- `options.properties-global-tabs`: global properties to create in tabs for this component in global dialog. If empty, properties will be created without tab structure in global dialog for this component.
    - `options.properties-global-tabs[].id`: the tab "name"
    - `options.properties-global-tabs[].label`: the "title" of the tab
    - `options.properties-global-tabs[].fields`: all the properties to be added in the tab.
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
 

## Troubleshooting
### JSON Export Failing
If you attempt to fetch your component's model json via a `.model.json` selector/extension and get the following error:
```
Invalid recursion selector value 'model'
Cannot serve request to /content/<site>/<path-to-page>/jcr:content/<path-to-component>.model.json in org.apache.sling.servlets.get.DefaultGetServlet
```
The most likely cause is that your sling model is not actually deployed to AEM. To validate, first check the bundles
console in AEM at `/system/console/bundles` to validate that your java bundle containing the generated sling model is
indeed on the server and in `Active` state. Assuming it is, check the Sling Models console at `/system/console/status-slingmodels`
to validate that your sling model is deployed and running. The default `demo-comp` component produced by the `data-config.json`
provided with the project should generate a fully deployable and functioning sling model. If you are experiencing this
error with the demo component, the most likely cause is that either your build failed to deploy the java bundle to the
AEM server, or the bundle deployed to the server but is not running.

### Shared Property Injection Failing
If your logs have an error that looks like this:
```
Caused by: java.lang.IllegalArgumentException: No Sling Models Injector registered for source 'shared-component-properties-valuemap'. 
```
This is because you do not have [Shared Component Properties](https://adobe-consulting-services.github.io/acs-aem-commons/features/shared-component-properties/)
configured on your AEM instance.  This often happens to people experimenting with the demo `data-config.json` provided
with the project, which creates a component with both a shared and a global property for testing.

To resolve this issue, you can configure Shared Component Properties on your AEM instance if you plan to use that feature
in your components. If you dont plan to use Shared Component Properties, however, empty out (or delete) the following
values inside of the `data-config.json` file used to generate your component: `properties-shared-tabs`,
`properties-shared`, `properties-global-tabs`, `properties-global`.  Once this is complete, regenerate your component,
which will remove any fields in your sling model attempting to be injected by `@SharedValueMapValue`.

## Contributing
 
Originally developed and contributed by [Bounteous](https://www.bounteous.com/insights/2019/07/31/aem-component-generator/).

Contributions are welcomed! Read the [Contributing Guide](.github/CONTRIBUTING.md) for more information.
 
## Licensing
 
This project is licensed under the Apache V2 License. See [LICENSE](LICENSE) for more information.
