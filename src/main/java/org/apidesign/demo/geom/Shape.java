package org.apidesign.demo.geom;

public abstract class Shape {
    public abstract double area();


    public static Shape cicle(double radius) {
        return new Circle(radius);
    }

    public static Shape square(double side) {
        return new Square(side);
    }

    public static Shape rectangle(double a, double b) {
        return new Rectangle(a, b);
    }

    public static Shape triangle(double base, double height) {
        return new Triagle(base, height);
    }

    static class Circle extends Shape {

        private final double radius;

        Circle(double radius) {
            this.radius = radius;
        }

        @Override
        public double area() {
            return Math.PI * Math.pow(this.radius, 2);
        }
    }

    static class Square extends Shape {
        private final double side;

        Square(double side) {
            this.side = side;
        }

        @Override
        public double area() {
            return Math.pow(side, 2);
        }
    }

    static class Rectangle extends Shape {

        private final double a;
        private final double b;

        Rectangle(double a, double b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public double area() {
            return a * b;
        }
    }

    static class Triagle extends Shape {

        private final double base;
        private final double height;

        Triagle(double base, double height) {
            this.base = base;
            this.height = height;
        }

        @Override
        public double area() {
            return base * height / 2;
        }
    }


}
