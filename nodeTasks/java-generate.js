const shell = require('shelljs');
const glob = require("glob");
const fs = require('fs');
const childProcess = require('child_process');
const path = require('path');

const argv = require('optimist')
    .usage('Generates AEM components.\nUsage: $0')
    .default('workdir', '.')
    .describe('workdir', 'Relative path to script caller or absolute path of workdir')
    .demand('dataConfigRoot')
    .describe('dataConfigRoot', 'Directory of data-config.json files, relative to workdir')
    .default('generatorJarSource', '')
    .describe('generatorJarSource', 'Path of generator.jar file, relative to workdir.')
    .default('dataConfigGlob', '**/data-config*.json')
    .describe('dataConfigGlob', 'Glob search for data-config files below "dataConfigRoot"')
    .default('createBundleTarget', false)
    .describe('createBundleTarget', '[true|false] for creating missing target')
    .argv
;

console.log(`\n## Setup vars and dirs`);
const workdir = path.normalize(process.cwd() + "/" + argv.workdir);
const dataConfigRoot = path.relative(workdir + "/", path.normalize(workdir + "/" + argv.dataConfigRoot));
const generatorJarSource = path.relative(workdir + "/", path.normalize(workdir + "/" + argv.generatorJarSource));
const dataConfigGlob = argv.dataConfigGlob;
const isCreateBundleTarget = (argv.createBundleTarget === 'true' || argv.createBundleTarget === true);

console.log(`workdir: ${workdir}`);
console.log(`dataConfigRoot dir: ${dataConfigRoot}`);
console.log(`generatorJarSource dir: ${generatorJarSource}`);

try {
    process.chdir(workdir);
    console.log('CWD changed to workdir: ' + process.cwd());
} catch (err) {
    throw err;
}
if (!shell.which('java')) {
    shell.echo("Sorry, this script requires java. Please restart your IDE after a java installation.");
    shell.exit(1);
}

console.log(`\n## Check if the generator jar source exists at ${generatorJarSource}`);
if (fs.existsSync(generatorJarSource)) {
    console.log(`Using generator jar source available at ${generatorJarSource}`);
} else {
    console.log(`No generator jar source available at ${generatorJarSource}`);
    throw err;
}

console.log(`\n## Execute generator jar with found configs at ${dataConfigRoot}`);
glob(`${dataConfigGlob}`, {cwd: `${dataConfigRoot}`}, function (err, files) {
    if (err) {
        throw err;
    }
    // files is an array of filenames.
    for (const file of files) {
        console.log('------');
        if (isCreateBundleTarget) {
            try {
                let dataConfigJson = JSON.parse(fs.readFileSync(path.join(dataConfigRoot, file), "utf8"));
                let bundleTargetPath = dataConfigJson["project-settings"]["bundle-path"];
                let bundleTargetPathFromWorkdir = path.join(workdir, bundleTargetPath);
                console.log(`Check if target exist ${bundleTargetPathFromWorkdir}`);
                if (!fs.existsSync(bundleTargetPathFromWorkdir)) {
                    fs.mkdirSync(bundleTargetPathFromWorkdir, {recursive: true});
                    console.log(`bundle-path created at ${bundleTargetPathFromWorkdir}`);
                }
            } catch (err) {
                console.log(`Error: Missing target path from data-json ["project-settings"]["bundle-path"]`);
                throw err;
            }
        }

        try {
            console.log(`Run generation for ${generatorJarSource} ${path.join(dataConfigRoot, file)}`);
            let out = childProcess.execSync(`java -jar ${generatorJarSource} ${path.join(dataConfigRoot, file)}`);
            console.log(out.toString());
        } catch (err) {
            throw err;
        }
    }
});
