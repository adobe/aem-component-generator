const shell = require('shelljs');
const maven = require("maven");
const path = require('path');

const argv = require('optimist')
    .usage('Generates AEM components.\nUsage: $0')
    .default('workdir', '.')
    .describe('workdir', 'Relative path to script caller or absolute path of workdir')
    .demand('mvn-command')
    .describe('mvn-command', 'mvn commands like \'clean install\'')
    .argv
;

console.log(`\n## Setup vars and dirs`);
const workdir = path.normalize(process.cwd() + "/" + argv.workdir);
const mvnCommand = argv['mvn-command'].split(" ");

console.log(`workdir: ${workdir}`);

if (!shell.which('mvn')) {
    shell.echo("Sorry, this script requires maven. Please restart your IDE after a maven installation.");
    shell.exit(1);
}

console.log(`\n## Run mvn command ${mvnCommand}`);
const mvn = maven.create(options = {cwd: `${workdir}`});
try {
    mvn.execute(mvnCommand, {}).catch((err) => {
        console.log(err);
    });
} catch (err) {
    throw err;
}
