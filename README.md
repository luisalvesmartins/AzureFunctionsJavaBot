# Running JAVA Bot in Azure Functions

## Prepare

Download Microsoft Bot Framework Java Libraries to the folder libraries (ver 4.0.0-SNAPSHOT).

Download Azure CLI.

## Deployment

In the "bot" folder, open a command prompt.

Compile & Run:

    mvn clean install
    mvn azure-functions:run

or just execute 

    run.cmd

Deploy, using Azure CLI:

    mvn package
    mvn azure-functions:deploy
