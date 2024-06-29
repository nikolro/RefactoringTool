/*
the definition site variance of (X,Body) is contravariant because to compute it we need to use the use
site variance of bx which is contravariant because The method foo modify bx without accessesing bx itself.
It only write a value to bx.x.
so the final variance of bx is the join of its use site variance and the definition site variance of (X,Body).
which (- join - = -) so the type parameter of List changes from E to ? super E.
 */
class bechmark7 {

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
