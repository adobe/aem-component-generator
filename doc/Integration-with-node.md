# Integration with npm and node.js

## How to install and run

#### [IntelliJ] Using node.js with 
1. Right click on package.json -> select "Show npm Scripts"
2. Double click on "1.Setup npm - npm:install"
3. Double click on "2.Build generator jar"
4. Double click on "3.Generate Components - java:generate"

#### [Eclipse] Using node.js with 
1. Right click on package.json > select "Run as..." > "npm install"
2. See "Run" in the Toolbar > Double click on "Build generator.jar - mvn clean install"
3. See "Run" in the Toolbar > Double click on "Generate Components - java generate"

## How to use in your AEM project

Inspect and copy source files below in your project using node.js

#### Source: npm and demo config files -> Gives hints to adapt to your project
- See npm scripts `src/main/resources/component-generator/package-example.json`
	- Adapt 'java:generate:aem:components' spaths to data-config.json and component-generator-N.jar in <your-project>
- Copy component-generator-N.jar
	- Copy to <your-project>/src/main/resources/component-generator
- Copy nodeTasks
	- nodeTasks/mvn-build.js: Builds generator jar -> Copy to <your-project>/nodeTasks
	- nodeTasks/java-generate.js: Generates Components -> Copy to <your-project>/nodeTasks
- Copy data config `src/main/resources/component-generator/data-config.json`
	- Copy to <your-project>/src/main/resources/component-generator
- See example npm script `src/main/resources/component-generator/package-example.json`
	- Copy to <your-project> 
	
#### Run generation in <your-project> with node.js
##### With IntelliJ
- Right click on package.json -> select "Show npm Scripts"
- Double click on "java:generate:aem:components"

##### With Eclipse
- See and adapt nodeTasks/eclipse/*.launch: Supports running node.js scripts on eclipse
