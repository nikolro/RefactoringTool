/*
the definition site variance of (E,BodyTestInvar) is INVARIANT because to compute it we need to use the definition
site variance of (E,List) which is INVARIANT,and the use site variance of list. We computes it as descriped in the paper with constrains.
The use site variance of list parameter is INVARIANT because we write and read from it in the readAndWrite method
body.
The final variance of list is the join of its use site variance and the definition site variance of (E,List).
which (0 join 0 = 0) so the type parameter doesnt change.
 */
class bechmark5 {

    class programBefore {
        import java.util.List;

        class BodyTestInvar<E> {
            public boolean readAndWrite(List<E> list) {
                E firstElem = list.get(0);
                return list.add(firstElem);
            }
        }
    }

    class programAfter {
        import java.util.List;

        class BodyTestInvar<E> {
            public boolean readAndWrite(List<E> list) {
                E firstElem = list.get(0);
                return list.add(firstElem);
            }
        }
    }

}
