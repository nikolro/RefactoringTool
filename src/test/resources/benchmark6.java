/*
The definition site variance of (X,Body) is INVARIANT because to compute it we need to use the use
site variance of bx which is COVARIANT , because The method foo accesses bx.x without modifying bx itself.
It only reads the value of bx.x. We computes it as descriped in the paper with constrains.
The final variance of bx is the join of its use site variance and the definition site variance of (X,Body).
which (+ join 0 = +) so the type parameter of List changes from E to ? extends E.
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
