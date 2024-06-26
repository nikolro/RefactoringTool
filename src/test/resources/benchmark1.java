class bechmark1 {
    class programBefore {
        class C<X> {
            X value;

            X foo(C<? super X> csx) {
                return value;
            }

            void bar(D<? extends X> dsx) {
            }
        }

        class D<Y> {
            void baz(C<Y> cx) {
            }
        }
    }

    class programAfter {
        class C<X> {
            X value;

            X foo(C<?> csx) {
                return value;
            }

            void bar(D<?> dsx) {
            }
        }

        class D<Y> {
            void baz(C<? extends Y> cx) {
            }
        }
    }
}
