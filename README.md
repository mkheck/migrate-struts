# Iterative Proof of Concept repo for migration from Apache Struts to Spring Boo

NOTE: This is very much a work in progress, and this is quite literally "step one".

## Some background

Tools and inputs I used for this process:

- Claude Code
- https://github.com/apache/struts-examples
- https://github.com/moderneinc/moderne-docs
- https://github.com/openrewrite/rewrite-docs

## Exact prompt used

Using https://github.com/moderneinc/moderne-docs and https://github.com/openrewrite/rewrite-docs as both reference documentation and a source for existing recipes (found under lower tree structures), create a plan for modernizing existing Apache Struts applications, both in this directory and others that exist, and migrating them to the latest supporting production version of Spring Boot. Create a checklist of your actions and check each item off as it is complete; this will be used as a guide for future efforts as well. List all existing OpenRewrite (OR) and Moderne OR recipes that go into this update/migration process and create new OpenRewrite recipes to complete the process.

## For more information

Please refer to the other Markdown, yaml, and Java files produced by Claude Code. These aren't intended to be the "final solution" -- I feel it important to repeat that -- but may serve as inspiration and/or a cautionary tale as we move forward. And by we, I mean *me*, along with anyone reading this who may be interested or intrigued. Go crazy and remember "sharing is caring". :)

What follows is content from the original Struts examples README:

## Struts Examples

[![Build Status @ ASF](https://ci-builds.apache.org/buildStatus/icon?job=Struts%2FStruts-examples-master)](https://ci-builds.apache.org/job/Struts/job/Struts-examples-master/)
[![Build Status @ GH Actions](https://github.com/apache/struts-examples/actions/workflows/maven.yml/badge.svg)](https://github.com/apache/struts-examples/actions/workflows/maven.yml)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

This Maven multi-module project contains all the Struts 2 example applications that are part of the Getting Started Struts 2 tutorials at http://struts.apache.org.

To build all the example applications run the Maven command:

```
mvn -e clean package
```

In the project's root folder, Maven will build each module and create a `.war` file in the target sub-folder of each module.

You can then copy the `.war` files to your Servlet container (e.g. Tomcat, Jetty, GlassFish, etc).

There is a README file in each module with instructions and the URL to view that application.

## Older versions

The examples are using the latest Struts version, if you are looking for older versions please take a look on the [Releases](releases) page.
