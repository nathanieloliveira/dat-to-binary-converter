package test.console;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainClass {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage:\n t2bconvert input_folder output_folder");
            return;
        }

        File inputs = new File(args[0]);
        File outputs = new File(args[1]);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

        if (!inputs.exists() && !inputs.isDirectory()) {
            throw new IOException("in folder does not exist or isn't a folder");
        }

        if (!outputs.mkdirs()) {
            throw new IOException("could not create out directory");
        }

        File[] files = inputs.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".dat"));

        if (files == null) {
            System.out.println("did not find any file in folder \"in\"");
            return;
        }

        for (File f : files) {
            executor.submit(() -> {
                try {
                    System.out.println("converting file " + f.getName());
                    convertStringToBinary(f, new File(outputs, f.getName()));
                } catch (IOException e) {
                    System.err.println("could not convert file " + f.getName());
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    private static void convertStringToBinary(File text, File output) throws IOException {
        try (BufferedSource in = Okio.buffer(Okio.source(text));
             BufferedSink out = Okio.buffer(Okio.sink(output))) {

            while (!in.exhausted()) {
                String s = in.readUtf8LineStrict();
                float f = Float.parseFloat(s);
                int v = Float.floatToIntBits(f);
                out.writeInt(v);
            }
        }
    }
}