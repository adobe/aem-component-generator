# aem-component-generator

Aem component generator is a Java project helps developers to create base structure of aem component from
from data-config.json (JSON with user required config. For example required dialog properties, field names and other config)

## Installation

Component generator can be either downloaded as a .jar file or check out as bit bucket project and
build project with below command which will get you a executable jar file under target folder.

```sh
$ mvn clean install
```

## How do you use

Step 1 :    Copy your "*component-generator-1.0-jar-with-dependencies.jar*" file to the your hs2-aem-base project folder. where you have core, ui-apps.

Step 2 :    Configure data-config.json with all required fields.

Step 3 :    You may now execute below commands from your terminal from the component-generator jar copied folder.

```sh
$ java -jar component-generator-filename.jar arg1 arg2
```

arg1 -  arg1 will take data-config.json file location path and can be ignore if you data-config.json file available in  same folder.

arg2 -  arg2 will take the location of your hs2-base-aem project main folder. This arg is required only if you trying to execute component-generator jar file other than bs2-base-aem project folder.