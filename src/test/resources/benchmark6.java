//check field read
class bechmark6 {

    class programBefore {
        class Body<X> {
            public X x = null;

            void foo(Body<X> bx) {
                X x1 = bx.x;
            }
        }
    }

    class programAfter {
        class Body<X> {
            public X x = null;

            void foo(Body<? extends X> bx) {
                X x1 = bx.x;
            }
        }
    }

}
