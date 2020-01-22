# java-airplay-server

[![Build Status](https://travis-ci.com/serezhka/java-airplay-server.svg?branch=master)](https://travis-ci.com/serezhka/java-airplay-server) [![Release](https://jitpack.io/v/serezhka/java-airplay-server.svg)](https://jitpack.io/#serezhka/java-airplay-server) [![HitCount](http://hits.dwyl.io/serezhka/java-airplay-server.svg)](http://hits.dwyl.io/serezhka/java-airplay-server) [![License](https://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

This is example of [java-airplay-lib](https://github.com/serezhka/java-airplay-lib) usage.

It's under development.

## How to use?

* Add java-airplay-server [dependency](https://jitpack.io/#serezhka/java-airplay-server) to your project

* Implement MirrorDataConsumer and start AirPlayServer, for example:
```java
  String dumpName = "dump.h264";
  var fileChannel = FileChannel.open(Paths.get(dumpName), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

  MirrorDataConsumer h264Dump = data -> {
      try {
          fileChannel.write(ByteBuffer.wrap(data));
      } catch (IOException e) {
          e.printStackTrace();
      }
  };

  String serverName = "AirPlayServer";
  int airPlayPort = 15614;
  int airTunesPort = 5001;
  new AirPlayServer(serverName, airPlayPort, airTunesPort, h264Dump).start();
```

## More examples

see repo [java-airplay-server-examples](https://github.com/serezhka/java-airplay-server-examples)

<img src="https://github.com/serezhka/java-airplay-server-examples/blob/media/gstreamer_playback.gif" width="600">
