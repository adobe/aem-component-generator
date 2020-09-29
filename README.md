# AEM Component Generator

[![CircleCI](https://circleci.com/gh/adobe/aem-component-generator.svg?style=svg)](https://circleci.com/gh/adobe/aem-component-generator)

AEM Component Generator is a java project that enables developers to generate the base structure of an
AEM component using a JSON configuration file specifying component and dialog properties and other configuration
options.

**Starting with version 2.0:**

The AEM Component Generator now has graphical user interface!
- Features a React based SPA web app, with a backend api that can dynamically build json conigurations for the components

- The backend generates the component config json structures still and can maintain multiple components

- Executes the code / folder structure build-out on demand from the web ui

### How to use

-  Step 1: Clone the project from github.

-  Step 2: Build the project by running `mvn clean install` from the main project folder.

-  Step 3: Copy the generated `component-generator-N.N.jar` file (under the `target` folder) to a location
           from which you wish to generate AEM component code.  Note that code will be generated at a relative path from which
           the generator is executed, which can be different from where the jar file is located.
- Step 4: To generate launch the component generator user interface, navigate to the main folder of your AEM project and execute the following command.

          ```sh
          $ java -jar <jarfile>
          ```
- A new web browser tab will be launched containing the AEM component generator UI! Follow the instructions in the app to configure your new component. By default the app will bind to localhost:8080, you can override this by passing an argument `-p={port]`

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

## Contributing
 
Originally developed and contributed by [Bounteous](https://www.bounteous.com/insights/2019/07/31/aem-component-generator/).

Contributions are welcomed! Read the [Contributing Guide](.github/CONTRIBUTING.md) for more information.
 
## Licensing
 
This project is licensed under the Apache V2 License. See [LICENSE](LICENSE) for more information.
