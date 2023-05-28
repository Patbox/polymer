# Getting Started

This is module of Polymer allowing you to create virtual (packet-only) entities
is a quick and simple way. It's mostly designed around using it with Display Entities,
but it can be extended to work with any other. You can attach them to regular (or Polymer)
entities and chunks.
### Adding to dependencies
```groovy
repositories {
    maven { url 'https://maven.nucleoid.xyz' } // You should have it
}

dependencies {
    modImplementation include("eu.pb4:polymer-virtual-entity:[TAG]")
}
```

For `[TAG]`/polymer-blocks version I recommend you checking [this maven](https://maven.nucleoid.xyz/eu/pb4/polymer-virtual-entity/).

Latest version: ![version](https://img.shields.io/maven-metadata/v?color=%23579B67&label=&metadataUrl=https://maven.nucleoid.xyz/eu/pb4/polymer-networking/maven-metadata.xml)