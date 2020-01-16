# java-airplay-server

[![Build Status](https://travis-ci.com/serezhka/java-airplay-server.svg?branch=master)](https://travis-ci.com/serezhka/java-airplay-server) [![License](https://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

This is example of [java-airplay-lib](https://github.com/serezhka/java-airplay-lib) usage.

It's under development. Tested with iPhone X (iOS 13.3). Many things are not supported yet such as sound, viewport change, ...

## receiver

Main module which handles RTSP requests, parse and decrypts fairplay encrypted mirror data.

## h264-dump

Saves mirror data stream to h264 file

[H264Dump.java](https://github.com/serezhka/java-airplay-server/blob/master/h264-dump/src/main/java/com/github/serezhka/jap2s/h264dump/H264Dump.java)

## vlcj-player

Playback screen mirroring in embedded vlc

[VLCJPlayer.java](https://github.com/serezhka/java-airplay-server/blob/master/vlcj-player/src/main/java/com/github/serezhka/jap2s/vlcj/VLCJPlayer.java)

<img src="https://github.com/serezhka/java-airplay-server/blob/media/vlcj_player_demo.gif" width="600">

## jmuxer-player

Playback screen mirroring with [jmuxer](https://github.com/samirkumardas/jmuxer)

[JMuxerWebSocketServer.java](https://github.com/serezhka/java-airplay-server/blob/master/jmuxer-player/src/main/java/com/github/serezhka/jap2s/jmuxer/JMuxerWebSocketServer.java)

[index-h264.html](https://github.com/serezhka/java-airplay-server/blob/master/index-h264.html)

<img src="https://github.com/serezhka/java-airplay-server/blob/media/jmuxer_player_demo.gif" width="600">

## TODO

* move RSTP handling, mirroring protocol impl to java-airplay-lib

* sound data handling

* viewport change

* ...

## Info

Inspired by many other open source projects analyzing AirPlay2 protocol. Special thanks to OmgHax.c's author 🤯

It took me several months of sleepless nights with debugger and wireshark to make this work.

If you appreciate my work, consider buying me a cup of coffee to keep me recharged

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/paypalme2/srzhka) [![Donate](https://github.com/serezhka/java-airplay-lib/blob/media/yandex_money.svg)](https://money.yandex.ru/to/4100111540466689)
