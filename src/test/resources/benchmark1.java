/*
the definition site variance of (E,BodyTestCovar) is invariant because to compute it we need to use the definition
site variance of (E,List) which is invariant.
the use site of list parameter is covariant because we only read from it in the firstElm method
by the method get.
so the final variance of list is the join of its use site variance and the definition site variance of (E,List).
which (+ join 0 = +) so the type parameter of List changes from E to ? extends E.
 */
class bechmark1 {

    class programBefore {
        import java.util.List;

        class BodyTestCovar<E> {
            void firstElem(List<E> list) {
                E firstElem = list.get(0);
            }
        }
    }

    class programAfter {
        import java.util.List;

        class BodyTestCovar<E> {
            void firstElem(List<? extends E> list) {
                E firstElem = list.get(0);
            }
        }
    }

}
