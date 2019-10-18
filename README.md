# Groundwater

[![Build Status](https://travis-ci.org/JulietGroundwater/Groundwater.svg?branch=master)](https://travis-ci.org/JulietGroundwater/Groundwater)

The central repository for Team Juliet's project, the Groundwater App.

See a presentation about the app:

[![Group Juliet - Groundwater App Presentation](http://img.youtube.com/vi/lPbtp1cjT0g/0.jpg)](http://www.youtube.com/watch?v=lPbtp1cjT0g "Group Juliet - Groundwater app")


## Testing
------------------------

In this project we have multiple forms of testing:

1. For most of the code we have written JUnit tests to ensure correct functionality, these can be run by using the command `gradle test`
2. We have also adopted the `google-java-format` (AOSP) style and we are using [spotless](https://github.com/diffplug/spotless) to manage this - to run the linter you can use `gradle spotlessCheck`
3. For continuous integration we are using [Travis](https://travis-ci.org/)
