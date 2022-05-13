# Aerial
Aerial view of software features, driven by tests.

## Build Aerial
    ./gradlew clean jar
    alias aerial='java -jar ./build/libs/aerial-0.1.jar'

## Read features
    aerial read /path/to/my/app

## Build report
    aerial report --app='My App'

## View report
    npx parcel report/**/report.html
