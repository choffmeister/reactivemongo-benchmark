# reactivemongo-benchmark

A test project using reactivemongo and spray to create a simple webservice. To test first make sure, that you have the following tools properly installed:

* [Scala][scala]
* [SBT][sbt]
* [MongoDB][mongodb]
* [wrk][wrk] (or another HTTP benchmarking tool like [Apache Benchmark][ab])

## How to benchmark

Clone this repository and execute:

~~~ bash
$ sbt pack
$ ./target/pack/bin/reactivemongo-benchmark
~~~

In another terminal execute:

~~~ bash
# test reactivemongo with artificial blocking
$ wrk -t12 -c400 -d30s http://localhost:8080/db/blocking

# test reactivemongo without blocking
$ wrk -t12 -c400 -d30s http://localhost:8080/db/nonblocking
~~~

## Results

On my Mac Book Pro 2013 (Core-i5 2.6 GHz, 8 GB RAM) I faced the following results:

~~~
choffmeister-mac:~ choffmeister$ wrk -t12 -c400 -d30s http://localhost:8080/db/blocking
Running 30s test @ http://localhost:8080/db/blocking
  12 threads and 400 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   117.53ms   86.31ms 626.27ms   80.61%
    Req/Sec   270.46    184.37   541.00     46.97%
  84211 requests in 30.01s, 17.19MB read
  Socket errors: connect 157, read 211, write 0, timeout 2318
Requests/sec:   2806.37
Transfer/sec:    586.49KB
~~~

~~~
choffmeister-mac:~ choffmeister$ wrk -t12 -c400 -d30s http://localhost:8080/db/nonblocking
Running 30s test @ http://localhost:8080/db/nonblocking
  12 threads and 400 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    28.37ms   17.76ms 423.58ms   88.35%
    Req/Sec   767.55    407.50     1.86k    63.62%
  275779 requests in 30.01s, 56.28MB read
  Socket errors: connect 157, read 135, write 0, timeout 2198
Requests/sec:   9191.03
Transfer/sec:      1.88MB
~~~

Note that this benchmark is not optimized with things like multiple request handling actors etc. This is by design to compare show the benefit of non blocking database access when having a webservice with few threads available.

[scala]: http://scala-lang.org/
[sbt]: http://scala-sbt.org/
[mongodb]: http://www.mongodb.org/
[wrk]: https://github.com/wg/wrk
[ab]: http://httpd.apache.org/docs/2.2/programs/ab.html
