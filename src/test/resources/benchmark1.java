/*
The definition site variance of (E,BodyTestCovar) is CONTRAVARIANT because to compute it we need to use the definition
site variance of (E,List) which is INVARIANT and the use site variance of list. We computes it as descriped in the paper with constrains.
The use site variance of list parameter is COVARIANT because we only read from it in the firstElm method
by the method get.
The final variance of list is the join of its use site variance and the definition site variance of (E,List).
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
