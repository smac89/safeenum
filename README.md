# SafeEnum

## Usage


**`build.gradle`**
``` gradle
buildscript {
	dependencies {
		classpath "com.github.smac89:safeenum-aspect-task:0.1.0"
	}

	repositories {
		maven { url 'https://jitpack.io' }
	}
}

repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.smac89:safeenum:v0.1.0'
}

task compileAspects(description: "Compiles aspects found on classpath", type: com.github.smac89.AspectJTask, group: 'build') {
    sourceSet = sourceSets.main
    aspectjOpts = [ // additional options which override the default ones
            log    : 'iajc.log',
            verbose: 'true'
    ]
}
```
