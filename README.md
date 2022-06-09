# java-airplay-server

[![build](https://github.com/serezhka/java-airplay-server/actions/workflows/build.yaml/badge.svg)](https://github.com/serezhka/java-airplay-server/actions/workflows/build.yaml)
[![Release](https://jitpack.io/v/serezhka/java-airplay-server.svg)](https://jitpack.io/#serezhka/java-airplay-server)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

This is example of [java-airplay-lib](https://github.com/serezhka/java-airplay-lib) usage.

It's under development.

## How to use?

* Add java-airplay-server [dependency](https://jitpack.io/#serezhka/java-airplay-server) to your project

* Implement AirplayDataConsumer and start AirPlayServer, for example:
```java
FileChannel videoFileChannel = FileChannel.open(Paths.get("video.h264"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
FileChannel audioFileChannel = FileChannel.open(Paths.get("audio.pcm"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

AirplayDataConsumer dumper = new AirplayDataConsumer() {
    
    @Override
    public void onVideo(byte[] video) {
        videoFileChannel.write(ByteBuffer.wrap(video));
    }

    
    @Override
    public void onAudio(byte[] audio) {
        if (FdkAacLib.isInitialized()) {
            byte[] audioDecoded = new byte[480 * 4];
            FdkAacLib.decodeFrame(audio, audioDecoded);
            audioFileChannel.write(ByteBuffer.wrap(audioDecoded));
        }
    }
};

String serverName = "AirPlayServer";
int airPlayPort = 15614;
int airTunesPort = 5001;
new AirPlayServer(serverName, airPlayPort, airTunesPort, dumper).start();
```

## More examples

see repo [java-airplay-server-examples](https://github.com/serezhka/java-airplay-server-examples)

<img src="https://github.com/serezhka/java-airplay-server-examples/blob/media/gstreamer_playback.gif" width="600">
