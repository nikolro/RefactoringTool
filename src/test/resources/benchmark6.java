/*
the definition site variance of (X,Body) is covariant because to compute it we need to use the use
site variance of bx which is covariant because The method foo accesses bx.x without modifying bx itself.
It only reads the value of bx.x.
so the final variance of bx is the join of its use site variance and the definition site variance of (X,Body).
which (+ join + = +) so the type parameter of List changes from E to ? extends E.
 */
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
