/*
the definition site variance of (E,BodyTestContravar) is invariant because to compute it we need to use the definition
site variance of (E,List) which is invariant.
the use site of list parameter is contravariant because we only write to it in the firstElm method
body.
so the final variance of list is the join of its use site variance and the definition site variance of (E,List).
which (- join 0 = -) so the type parameter of List changes from E to ? super E.
*/
class bechmark4 {

    class programBefore {
        import java.util.List;

        class BodyTestContravar<E> {
            private E elem = null;

            public boolean addElemTo(List<E> list) {
                return list.add(this.elem);
            }
        }
    }

    class programAfter {
        import java.util.List;

        class BodyTestContravar<E> {
            private E elem = null;

            public boolean addElemTo(List<? super E> list) {
                return list.add(this.elem);
            }
        }
    }

}
