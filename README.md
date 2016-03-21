# Static Gson [![Circle CI](https://circleci.com/gh/gfx/StaticGson.svg?style=svg)](https://circleci.com/gh/gfx/StaticGson) [ ![Download](https://api.bintray.com/packages/gfx/maven/static-gson/images/download.svg) ](https://bintray.com/gfx/maven/static-gson/)

This library makes [Gson](https://github.com/google/gson) faster by generating `TypeAapterFactory` with annotation processing. In other words, this is an AOT compiler for Gson.

## Getting Started

For Android apps:

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}

apply plugin: 'com.neenbedankt.android-apt'

repositories {
    jcenter()
}

// ...

dependencies {
    apt 'com.github.gfx.static_gson:static-gson-processor:0.9.6'
    compile 'com.github.gfx.static_gson:static-gson:0.9.6'
}
```

For Java apps with Gradle 2.12+:

```gradle
dependencies {
    compileOnly 'com.github.gfx.static_gson:static-gson-processor:0.9.6'
    compile 'com.github.gfx.static_gson:static-gson:0.9.6'
}
```

## Usage

Add `@JsonSerializable` to JSON serializable models:

```java
@JsonSerializable(fieldNamingPolicy = FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
public class User {
    public String firstName; // serialized to "first_name"
    public Stirng lastName; // serialized to "last_name"
}
```

Then, give `StaticGsonTypeAdapterFactory.newInstance()` to `GsonBuilder`:

```java
Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(StaticGsonTypeAdapterFactory.newInstance())
        .create();
```

That's all. `Gson#toJson()` and `Gson#fromGson()` becomes faster
for `@JsonSerializable` classes.

## Benchmark

See `example/MainActivity.java` for details.

On Xperia Z4 / Android 5.0.2:

```
$ adb logcat -v tag | ag D/XXX

D/XXX     : start benchmarking Dynamic Gson
D/XXX     : Dynamic Gson in serialization: 449ms
D/XXX     : Dynamic Gson in deserialization: 387ms
D/XXX     : start benchmarking Static Gson
D/XXX     : Static Gson in serialization: 198ms
D/XXX     : Static Gson in deserialization: 233ms
D/XXX     : start benchmarking Moshi
D/XXX     : Moshi in serialization: 270ms
D/XXX     : Moshi in deserialization: 656ms
D/XXX     : start benchmarking LoganSquare
D/XXX     : LoganSquare in serialization: 111ms
D/XXX     : LoganSquare in deserialization: 268ms
```

## Support

* Use [GitHub issues](https://github.com/gfx/StaticGson/issues) for the issue tracker
* Feel free to ask for questions to the author [@\_\_gfx\_\_](https://twitter.com/__gfx__)

## Release Engineering for Maintainers

```shell
./gradlew bumpMajor # or bumpMinor / bumpPatch
git add -va
make publish # run tests, build artifacts, publish to jcenter, and make a tag
```

## Author

FUJI Goro ([gfx](https://github.com/gfx)).

## License

Copyright (c) 2015 FUJI Goro (gfx).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
