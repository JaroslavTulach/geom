Demonstrating the speedup thanks to generating execution profiles and re-running
the **SubstrateVM** compilation with the gathered data.

### Initial Benchmarking

Get [GraalVM](http://www.oracle.com/technetwork/oracle-labs/program-languages/),
version 0.33 or newer and then execute:

```bash
$ JAVA_HOME=/graalvm/ mvn clean install
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
have to generate special version of the binary:

```bash
$ JAVA_HOME=/graalvm/ mvn clean install -PProfilesCollect
$ ./target/geom 15000 now 30 square rectangle
...
sum: 4384.131082976915
last round 108 ms
$ ls target/*iprof
target/geom.iprof
```
Now the same algorithm runs slower, however that is because we are collecting
the profiling data. Once the program is finished, an `.iprof` file is generated.

### The Race

Now we can recompile once again and see the speedup. Do `install` again. This
time it finds the `geom.iprof` file and optimizes the binary:
```bash
$ JAVA_HOME=/graalvm/ mvn install -PProfilesUse
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
the help of the `ProfilesCollect` and `ProfilesUse` options, it can also give
you execution speed optimal for your workloads.
