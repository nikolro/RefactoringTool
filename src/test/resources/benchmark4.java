/*
The definition site variance of (E,BodyTestContravar) is COVARIANT because to compute it we need to use the definition
site variance of (E,List) which is INVARIANT.and the use site variance of list. We computes it as descriped in the paper with constrains.
the use site variance of list parameter is CONTRAVARIANT because we only write to it in the addElemTo method
body.
The final variance of list is the join of its use site variance and the definition site variance of (E,List).
which (- join 0 = -) so the type parameter of List changes from E to ? super E.
*/
class bechmark4 {

    class programBefore {
        import java.util.List;

        class BodyTestContravar<E> {
            public boolean addElemTo(List<E> list){
                E elem = null;
                return list.add(elem);
            }
        }
    }

    class programAfter {
       import java.util.List;

        class BodyTestContravar<E> {
            public boolean addElemTo(List<? super E> list){
                E elem = null;
                return list.add(elem);
            }
        }
    }

}
