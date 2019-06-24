It is well known that [GraalVM](https://www.graalvm.org/) `native-image` can give you fast startup. 
However, the peak execution speed may lack behind JVM with JIT compiler 
(see [great explanation](https://github.com/oracle/graal/issues/979#issuecomment-480786612) of such behavior). 
In short, the missing execution profiles are the problem. However,
`native-image` enterprise is capable to collect and use such profiles for its ahead-of-time compilation. 
This demo shows the speedup obtained by collecting execution profiles and re-running
the `native-image` compilation with the gathered profiling data.

### Installation

Get [GraalVM](https://www.graalvm.org/) Enterprise Edition version 19.0.2 by
downloading it from the
[Oracle Technology Network](https://www.oracle.com/technetwork/graalvm/downloads/index.html).
Unpack it.

Download *Oracle GraalVM Enterprise Edition Native Image* JAR file from
the same [OTN page](https://www.oracle.com/technetwork/graalvm/downloads/index.html).
Install it using the `gu` tool:

```bash
$ /graalvm-ee-19.0.2/bin/gu install --file native-image-installable-svm-svmee-*-19.0.2.jar
```

Verify that PGO support is available:
```bash
$ /graalvm-ee-19.0.2/bin/native-image --help | grep pgo
    --pgo                 a comma-separated list of files from which to read the data
    --pgo-instrument      instrument AOT compiled code to collect data for profile-guided
```

### Initial Benchmarking

With the GraalVM EE 19.0.2 properly installed we can proceed with initial measuring:

```bash
$ JAVA_HOME=/graalvm-ee-19.0.2/ mvn clean install
$ ./target/geom 15000 now 30 square rectangle
sum: 4394.341496946556
last round 55 ms
$ ./target/geom 15000 now 30 triangle circle
sum: 8776.729175399201
last round 48 ms
```

That is the initial benchmark we want to beat. The default code is ready for
four different objects `square`, `rectangle`, `triangle` and `circle` and treats
all of them with the same probability.

### The Training

However we can train the code to optimize for certain objects. To do that we
have to generate a special version of the binary:

```bash
$ JAVA_HOME=/graalvm-ee-19.0.2/ mvn clean install -PProfilesCollect
$ ./target/geom 15000 now 30 square rectangle
...
sum: 4384.131082976915
last round 108 ms
    $ ls *iprof
default.iprof
```
Now the same algorithm runs slower, however that is because we are collecting
the profiling data. Once the program is finished, an `.iprof` file is generated.

### The Race

Now we can recompile once again and see the speedup. Do `install` again. This
time it finds the `geom.iprof` file and optimizes the binary:
```bash
$ JAVA_HOME=/graalvm-ee-19.0.2/ mvn install -PProfilesUse
$ ./target/geom 15000 now 30 square rectangle
...
sum: 4401.419731937973
last round 36 ms
```
That means **35%** speedup!

However, the speed is only improved when the trained for geometric shapes are
being processed. Should the other ones appear too, the execution slows down. At
the end, it can be even less performant than the non-optimized version:
```bash
$ ./target/geom 15000 now 30 triangle circle
sum: 8888.679818384859
last round 74 ms
```
If something like this happens, it is time to re-profile and re-deploy new version.

### The Winner

It is well known that `native-image` can give you fast startup. However with
the help of the `--pgo-instrument` and `--pgo` options, it can also give
you execution speed optimal for your workloads.
