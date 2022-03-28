plugins {
   id("sap.commerce.build") version("3.6.0")
   id("sap.commerce.build.ccv2") version("3.6.0")
}

repositories {
  //Please refer to the official Gradle documentation and the plugin documentation for additional
  // information about dependency resolution.

  // Option 1: Use a (custom) Maven repository to provide SAP Commerce artifacts for development
  maven {
     url = uri("https://custom.repo.com/maven")
  }
  // Option 2: Download all required files manually and put them in `dependencies` folder
  // There are ways to automate the downloads from launchpad.support.sap.com, please check the FAQ.
  // Make sure to rename the files accordingly (<artifactId>-<version>.zip)
  flatDir { dirs("dependencies") }

  mavenCentral()
}
