class bechmark1 {
    class programBefore {
        class Body<X> {
            public X x = null;
            void foo(Body<X> bx) {
                X element = null;
                bx.x = element;
            }
        }
    }

    class programAfter {
        class Body<X> {
            public X x = null;
            void foo(Body<? super X> bx) {
                X element = null;
                bx.x = element;
            }
        }
    }
}
