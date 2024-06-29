/*
the definition site variance of (E,BodyTestBivar) is invariant because to compute it we need to use the definition
site variance of (E,List) which is invariant.
the use site of list parameter is bivariant because we dont read or write to the parameter
in the printA method body.
so the final variance of list is the join of its use site variance and the definition site variance of (E,List).
which (* join 0=*) so the type parameter of List changes from E to ?.
 */
class bechmark3 {

    class programBefore {
        import java.util.List;

        class BodyTestBivar<E> {
            public void printA(List<E> list) {
                System.out.println("list");
            }
        }
    }

    class programAfter {
        import java.util.List;

        class BodyTestBivar<E> {
            public void printA(List<?> list) {
                System.out.println("list");
            }
        }
    }

}
