package org.graalvm.test.geom;

import java.util.Random;

final class Geom {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: <number_of_objects> <random_seed_or_\"now\"> <no_repeats> <object_types>*");
            System.err.println("       object types can be: circle square rectangle triangle");
            System.exit(1);
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

        double expected = area(samples);
        long prev = System.currentTimeMillis();
        for (int i = 0; i < repeat * 1000; i++) {
            double sum = area(samples);
            if (sum != expected) {
                throw new IllegalStateException("Wrong result " + sum + " was " + expected);
            }
            if (i % 1000 == 0) {
                prev = System.currentTimeMillis();
                System.err.println("sum: " + sum);
            }
        }
        System.err.println("last round " + (System.currentTimeMillis() - prev) + " ms");
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

    static double area(Shape[] all) {
        double sum = 0;
        for (Shape shape : all) {
            sum += shape.area();
        }
        return sum;
    }
}
