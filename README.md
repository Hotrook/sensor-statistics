# sensor-statistics [![Build Status](https://travis-ci.com/Hotrook/sensor-statistics.svg?branch=master)](https://travis-ci.com/Hotrook/sensor-statistics) [![codecov](https://codecov.io/gh/Hotrook/sensor-statistics/branch/master/graph/badge.svg)](https://codecov.io/gh/Hotrook/sensor-statistics) [![Maintainability](https://api.codeclimate.com/v1/badges/d271d121a131822dcbfa/maintainability)](https://codeclimate.com/github/Hotrook/sensor-statistics/maintainability) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/0b43ef39bcee4d78a358d794c551d5f1)](https://app.codacy.com/app/Hotrook/sensor-statistics?utm_source=github.com&utm_medium=referral&utm_content=Hotrook/sensor-statistics&utm_campaign=Badge_Grade_Dashboard) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) 

## What is this app? 

This is an simple tool which can scan given directory for `*.csv` files with temperature measurements from different sensors.
This is expected format of those files: 
```csv
sensor,temperature
s1,30
s2,30
s4,45
s3,NaN
s1,10
```
Some measurements may be not successful and in those cases instead of number in temperature field there is `NaN`
There could be a lot of such files and measurements from one sensor can be in different files. 
This akka-actor based app reads all found files and make some statistics based on this data, which results in short summary.

This summary includes:
* Number of found files
* Number of measurements
* Number of failed measurements
* For any sensor there is an average, minimal and maximal temperature. All those measurements are sorted by average.

## How to run it? 

Before running a quick configuration is needed. There is an `application.conf` file in `src/main/resources`directory.
It looks like this:
```
app {
  directoryPath = "src/test/resources/testDirectories/3-files-dir-example"
}

akka {
  loglevel = "INFO"
  stdout-loglevel = "OFF"
  loglevel = "OFF"
}
```
There you could specify in `directoryPath` field what directory should be scanned for `*.csv` files.

#### Optional configuration 
Optionally you can also set up logger configuration in this files. By default logging is turned off. 
If you want to turn on logging, you need to remove those two lines:
```
stdout-loglevel = "OFF"
loglevel = "OFF"
```
so the effect would be: 
```
app {
  directoryPath = "src/test/resources/testDirectories/3-files-dir-example"
}

akka {
  loglevel = "INFO"
}
```

#### Actual running sensor-statistics app

Go to main directory of this project and type:
```
sbt run
```
That's all! It while take a while to compile the project and you will have your sensor statistics in Standard Output.

## Improvement ideas
* adding some library for streaming data with `back-pressure` feature
* rethink communication to adjust it to `let it crash` approach
* use types bigger than `Int` to store temperature
