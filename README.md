# aem-component-generator

AEM component generator is a Java project that enables developers to automatically create the base structure of an
AEM component using a JSON configuration file specifying component and dialog properties and other configuration
options.

## Installation

AEM component generator can be either downloaded as a .jar file or cloned from the bitbucket project and built locally
with following command which will create an executable jar file under the `target` folder.

```sh
$ mvn clean install
```

## How To Use

Step 1: Copy your `component-generator-N.N.jar` file to the your `hs2-aem-base` project under the `scripts/compgen`
folder.

Step 2: Create a `data-config.json` file with all required fields under the same folder.

```json
{
  "project-settings" : {
    "bundle-path": "core/src/main/java",
    "apps-path": "ui.apps/src/main/content/jcr_root/apps",
    "component-path": "hs2-aem-base/components",
    "model-interface-pkg": "com.hs2solutions.aem.base.core.models",
    "model-impl-pkg": "com.hs2solutions.aem.base.core.models.impl"
  },
  "name": "demo-comp",
  "title": "Demo Component",
  "group": "HS2 Base",
  "type": "content",
  "options": {
    "js": true,
    "css": true,
    "html": true,
    "slingmodel": true,
    "properties": [
      {
        "field": "textfieldTest",
        "description": "Adds a fieldDescription tooltip",
        "type": "textfield",
        "label": "Textfield Test",
        "attributes": {}
      },
      {
        "field": "checkTest",
        "type": "checkbox",
        "attributes": {
          "value": "{Boolean}true",
          "text": "Checkbox Test"
        }
      },
      {
        "field": "pathfieldTest",
        "type": "pathfield",
        "label": "Pathfield Test",
        "attributes": {
          "rootPath": "/content/sites"
        }
      },
      {
        "field": "textareaTest",
        "type": "textarea",
        "label": "Textarea Test",
        "attributes": {}
      },
      {
        "field": "dateTest",
        "description": "Context tooltip for authors.",
        "type": "datepicker",
        "label": "Select Date",
        "attributes": {
          "displayedFormat": "MM/DD/YYYY"
        }
      },
      {
        "field": "selectTest",
        "type": "select",
        "label": "Select Test",
        "attributes": {
          "defaultValue": "opt1",
          "value": "opt1"
        },
        "items": [
          {
            "field": "option1",
            "attributes": {
              "selected": "true",
              "text": "Option 1",
              "value": "opt1"
            }
          },
          {
            "field": "option2",
            "attributes": {
              "text": "Option 2",
              "value": "opt2"
            }
          }
        ]
      },
      {
        "field": "radioTest",
        "type": "radiogroup",
        "attributes": {
          "vertical": "{Boolean}false"
        },
        "items": [
          {
            "field": "radio1",
            "type": "radio",
            "attributes": {
              "checked": "{Boolean}true",
              "name": "radioTest",
              "text": "Radio 1",
              "value": "rad1"
            }
          },
          {
            "field": "radio2",
            "type": "radio",
            "attributes": {
              "checked": "{Boolean}true",
              "name": "radioTest",
              "text": "Radio 2",
              "value": "rad2"
            }
          }
        ]
      },
      {
        "field": "hiddenTest",
        "type": "hidden",
        "attributes": {
          "value": "hidden value"
        }
      },
      {
        "field": "numberfieldTest",
        "type": "numberfield",
        "label": "Numberfield Test",
        "attributes": {
          "max": "{Double}20",
          "min": "{Double}0",
          "step": "1",
          "value": "{Long}20"
        }
      },
      {
        "field": "imageTest",
        "type": "image",
        "label": "Image Test"
      }
    ],
    "properties-shared": [
      {
        "field": "sharedTextfieldTest",
        "type": "textfield",
        "label": "Shared Textfield Test"
      }
    ],
    "properties-global": [
      {
        "field": "globalTextfieldTest",
        "type": "textfield",
        "label": "Global Textfield Test"
      }
    ]
  }
}
```
- `project-settings`: contains configuration options related to the project code will be generated for
- `project-settings.bundle-path`: path to the code bundle's root of Java packages
- `project-settings.apps-path`: path to the apps root
- `project-settings.component-path`: path to the project's components directory
- `project-settings.model-interface-pkg`: Java package for the interface model objects
- `project-settings.model-impl-pkg`: Java package for the implementation model objects
- `name`: folder name for the component
- `title`: human readable component name, also used as the title for dialogs
- `group`: component group
- `type`: component folder type - content, form, structure
- `options.js`: whether to create an empty JS lib for the component (shared with CSS lib)
- `options.css`: whether to create an empty CSS lib for the component (shared with JS lib)
- `options.html`: whether to create a default HTML file for the component
- `options.slingmodel`: whether to create a sling model for the component
    - Class name is derived from converting "name" prop above to camel case (e.g. "google-maps" -> `GoogleMaps`/`GoogleMapsImpl`)
    - Fields are derived from dialog properties (see below)
- `options.properties`: properties to create in standard dialog for this component. If empty, no standard dialog will be created. This sample includes one of every possible sling:resourceType
- `options.properties-shared`: properties to create in shared dialog for this component. If empty, no shared dialog will be created
- `options.properties-global`: properties to create in global dialog for this component. If empty, no global dialog will be created

Step 3: In your terminal, navigate to the `hs2-aem-base` folder and execute the following command.

```sh
$ java -jar <jarfile> <arg1>
```

- `jarfile`: full path to `component-generator-N.N.jar` file (replacing `N.N` with the applicable numbers)
- `arg1`: full path to `data-config.json` file

Example:
```sh
$ java -jar scripts/compgen/component-generator-1.0.jar scripts/compgen/data-config.json
```