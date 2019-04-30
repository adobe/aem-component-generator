# aem-component-generator

AEM component generator is a Java project helps developers to create base structure of AEM component using
`data-config.json` (JSON with user required config. For example required dialog properties, field names
and other config)

## Installation

Component generator can be either downloaded as a .jar file or check out as bit bucket project and build project
with below command which will get you a executable jar file under target folder.

```sh
$ mvn clean install
```

## How do you use

Step 1 :    Copy your `component-generator-1.0.jar` file to the your `hs2-aem-base` project folder.
            where you have `core`, `ui-apps` folders.

Step 2 :    Configure `data-config.json` with all required fields.

```json
{
  "name": "google-maps",
  "title": "Google Maps",
  "group": "Component-group",
  "type": "content",
  "options": {
    "js": true,
    "css": true,
    "html": true,
    "slingmodel": true,
    "properties": [
      {
        "field": "latitude",
        "type": "text",
        "label": "Latitude",
        "attributes": {
        	"attr1":"attrValue1",
        	"attr2":"attrValue2"
        }
      },
      {
        "field": "longitude",
        "type": "text",
        "label": "Longitude",
        "attributes": {}
      },
      {
        "field": "zoom",
        "type": "number",
        "label": "Zoom Level",
        "attributes": {
          "max": "{Double}20",
          "min": "{Double}0",
          "step": "1",
          "value": "{Long}20"
        }
      }
    ],
    "properties-shared": [],
    "properties-global": [
      {
        "field": "google maps api key",
        "type": "text",
        "label": "Google Maps API Key"
      }
    ]
  }
}
```

Step 3 :    Go to the folder containing component-generator.jar and execute below command by opening your terminal.

```sh
$ java -jar component-generator-1.0.jar arg1
```

arg1 -  arg1 will take data-config.json file location path and can be ignore if you data-config.json
        file available in  same folder.
