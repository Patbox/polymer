# Getting Started

This is additional module/extension of Polymer, adding support for textured blocks.
It requires server resource pack to be active work correctly.

It requires Polymer Core and Polymer Resource Pack modules to work.

### Adding to dependencies
```groovy
repositories {
	maven { url 'https://maven.nucleoid.xyz' } // You should have it
}

dependencies {
	modImplementation include("eu.pb4:polymer-core:[TAG]")
	modImplementation include("eu.pb4:polymer-blocks:[TAG]")
	modImplementation include("eu.pb4:polymer-resource-pack:[TAG]")
}
```

For `[TAG]`/polymer-blocks version I recommend you checking [this maven](https://maven.nucleoid.xyz/eu/pb4/polymer-blocks/).

Latest version: ![version](https://img.shields.io/maven-metadata/v?color=%23579B67&label=&metadataUrl=https://maven.nucleoid.xyz/eu/pb4/polymer-blocks/maven-metadata.xml)