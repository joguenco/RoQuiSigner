# RoQuiSigner
Is a signer application of the XML files using XAdES-BES algorithm

## Thanks
Thanks for XAdES4j project https://github.com/luisgoncalves/xades4j

## Description
The application is used for sign invoice, retention receipts, debit notes,
credit notes, remission guides and purchase liquidation for Ecuador country

## Software
* Java 21
* Gradle 8.14.3

## Warning
Before of continuing, please read the documentation, read the all documents in doc folder

## Gradle
### Upgrade Gradle
```
gradle wrapper --gradle-version 8.14.3
```

### Displays the tasks runnable from root project
```
gradle tasks
```
### Run
```
gradle run
```
### Test
```
gradle test
```
### Clean, Build and Publish in local maven repository
```
gradle clean build publishToMavenLocal
```
or you can run separately
### Build
```
gradle build
```
## Publish in local maven repository
### GNU/Linux or MacOS
```
mvn install:install-file -Dfile=./app/build/libs/RoQuiSigner-1.0.0.jar -DgroupId=dev.joguenco.signer -DartifactId=RoQuiSigner -Dversion=1.0.0 -Dpackaging=jar
```
### Windows
In CMD terminal, not in PowerShell
```
mvn install:install-file -Dfile=.\app\build\libs\RoQuiSigner-1.0.0.jar -DgroupId=dev.joguenco.signer -DartifactId=RoQuiSigner -Dversion=1.0.0 -Dpackaging=jar
```
