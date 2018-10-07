# Running JAVA Bot in Azure Functions

## Prepare

Download Microsoft Bot Framework Java Libraries to the folder libraries (ver 4.0.0-SNAPSHOT)

## Deployment

In the "bot" folder, open a command line.

Compile & Run:

    mvn clean install
    mvn azure-functions:run

Deploy - using Azure CLI:

    mvn package
    mvn azure-functions:deploy

## Possible Extensions

### LUIS

### Translator Services

### QnAMaker