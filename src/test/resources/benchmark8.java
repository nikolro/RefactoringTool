class bechmark1 {
    class programBefore {
        class Body<X> {
            public int x = 5;

            void foo(Body<X> bx) {

                int y = bx.x;
            }
        }
    }

    class programAfter {
        class Body<X> {
            public int x = 5;

            void foo(Body<?> bx) {

                int y = bx.x;
            }
        }
    }
}
