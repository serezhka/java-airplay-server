package com.github.serezhka.jap2s.vlcj;

import com.github.serezhka.jap2s.receiver.handler.mirroring.MirrorDataConsumer;
import lombok.extern.slf4j.Slf4j;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.log.LogLevel;
import uk.co.caprica.vlcj.log.NativeLog;
import uk.co.caprica.vlcj.media.callback.nonseekable.NonSeekableInputStreamMedia;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@Slf4j
public class VLCJPlayer implements MirrorDataConsumer {

    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private JFrame f;

    private PipedOutputStream output;
    private PipedInputStream input;
    private NonSeekableInputStreamMedia nsism;

    private MediaPlayerFactory mediaPlayerFactory;
    private NativeLog nativeLog;

    public VLCJPlayer() {

        output = new PipedOutputStream();

        new Thread(() -> {

            mediaPlayerFactory = new MediaPlayerFactory("-vvv", "--demux=h264", "--h264-fps=30");

            nativeLog = mediaPlayerFactory.application().newLog();
            nativeLog.setLevel(LogLevel.DEBUG);
            nativeLog.addLogListener((level, module, file, line, name, header, id, message) ->
                    log.debug("[VLCJ] [{}] [{}] {} {}", level, module, name, message));

            mediaPlayerComponent = new EmbeddedMediaPlayerComponent(mediaPlayerFactory, null, null, null, null);

            f = new JFrame("Test Player");
            f.setSize(800, 600);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    mediaPlayerComponent.release();
                }
            });
            f.setContentPane(mediaPlayerComponent);
            f.setVisible(true);

            try {
                input = new PipedInputStream(output);
            } catch (IOException e) {
                throw new RuntimeException();
            }

            nsism = new NonSeekableInputStreamMedia(10240) {

                @Override
                protected InputStream onOpenStream() {
                    return input;
                }

                @Override
                protected void onCloseStream(InputStream inputStream) throws IOException {
                    inputStream.close();
                }

                @Override
                protected long onGetSize() {
                    return 0;
                }
            };

            mediaPlayerComponent.mediaPlayer().media().play(nsism);
            mediaPlayerComponent.mediaPlayer().controls().play();
        }).start();

        log.info("VLCJ Player started!");
    }

    @Override
    public void onData(byte[] data) {
        try {
            output.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
