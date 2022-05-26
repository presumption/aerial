# Aerial
Aerial view of software features, driven by tests.

## Build Aerial
    ./gradlew clean jar
    alias aerial='java -jar ./build/libs/aerial-{version}.jar'

## Read features
    aerial scan /path/to/my/app

## Build report
    aerial report --app='My App'

## View report
    npx parcel report/**/report.html --port 12345
