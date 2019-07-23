package org.apidesign.demo.geom;

import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.util.Random;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

final class Geom {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: <number_of_objects> <random_seed_or_\"now\"> <#repeats> <object_types>*");
            System.err.println("       object types can be: circle square rectangle triangle");
            System.exit(1);
        }
        MBeanServer server = initializeMBeanServer();
        if (server != null) {
            turnProfilingOn(server);
        }

        int cnt = Integer.parseInt(args[0]);
        long seed;
        try {
            seed = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            seed = System.currentTimeMillis();
        }
        int repeat = Integer.parseInt(args[2]);
        Shape[] samples = generate(3, args, cnt, seed);

        double expected = computeArea(samples);
        long prev = System.currentTimeMillis();
        for (int i = 0; i < repeat * 1000; i++) {
            double sum = computeArea(samples);
            if (sum != expected) {
                throw new IllegalStateException("Wrong result " + sum + " was " + expected);
            }
            if (i % 1000 == 0) {
                prev = System.currentTimeMillis();
                System.err.println("sum: " + sum);
            }
        }
        System.err.println("last round " + (System.currentTimeMillis() - prev) + " ms.");
        if (server != null) {
            dumpProfilingData(server);
        }
    }

    static Shape[] generate(int offset, String[] types, int count, long seed) {
        Random r = new Random(seed);
        Shape[] arr = new Shape[count];
        for (int i = 0; i < arr.length; i++) {
            String t = types[offset + i % (types.length - offset)];
            Shape s;
            switch (t) {
                case "circle":
                    s = Shape.cicle(r.nextDouble());
                    break;
                case "rectangle":
                    s = Shape.rectangle(r.nextDouble(), r.nextDouble());
                    break;
                case "square":
                    s = Shape.square(r.nextDouble());
                    break;
                case "triangle":
                    s = Shape.triangle(r.nextDouble(), r.nextDouble());
                    break;
                default:
                    throw new IllegalStateException("" + t);
            }
            arr[i] = s;
        }
        return arr;
    }

    static double computeArea(Shape[] all) {
        double sum = 0;
        for (Shape shape : all) {
            sum += shape.area();
        }
        return sum;
    }

    private static MBeanServer initializeMBeanServer() {
        if ("Substrate VM".equals(System.getProperty("java.vm.name"))) {
            return null;
        }
        if (System.getProperty("graal.PGOInstrument") != null) {
            return null;
        }
        return ManagementFactory.getPlatformMBeanServer();
    }


    private static void turnProfilingOn(MBeanServer server) throws Exception {
        Exception status = null;
        for (int i = 0; i < 1000000; i++) {
            status = turnProfilingOn0(server);
            if (status == null) {
                System.err.println("MBeanServer initialized: " + server);
                break;
            }
        }
        if (status != null) {
            throw status;
        }
    }

    private static Exception turnProfilingOn0(MBeanServer server) {
        try {
            ObjectName graalName = new ObjectName("org.graalvm.compiler.hotspot:type=HotSpotGraalRuntime_VM");
            ObjectInstance graalBean = server.getObjectInstance(graalName);
            server.setAttribute(graalName, new Attribute("PGOInstrument", "\"true\""));
            return null;
        } catch (Exception ex) {
            return ex;
        }
    }

    private static void dumpProfilingData(MBeanServer server) throws Exception {
        ObjectName graalName = new ObjectName("org.graalvm.compiler.hotspot:type=HotSpotGraalRuntime_VM");
        String dump = (String) server.invoke(graalName, "profilingDump", null, null);
        File file = new File("default.iprof").getAbsoluteFile();
        try (FileWriter w = new FileWriter(file)) {
            w.write(dump);
        }
        System.err.println("profiling saved into: " + file);
    }
}
