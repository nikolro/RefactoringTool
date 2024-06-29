//mix
/*
in the influence flow graph we will have edge from elements to iterator after doing the analysis
in section 4.3, so if we change the elements variance we have to change the iterator to the same
variance.
we compute the variances according to the use site variance and defintion site
variance as we explained in the first benchmarks. so we get that elemets variance is covariant
so we change also the iterator type parameter to extends.
 */
class bechmark13 {

    class programBefore {
        import java.util.Iterator;
        import java.util.List;

        class A<X> {
            public void printElements(List<X> elements) {
                Iterator<X> iterator = elements.iterator();
                while (iterator.hasNext()) {
                    System.out.println(iterator.next());
                }
            }
        }
    }

    class programAfter {
        import java.util.Iterator;
        import java.util.List;

        class A<X> {
            public void printElements(List<? extends X> elements) {
                Iterator<? extends X> iterator = elements.iterator();
                while (iterator.hasNext()) {
                    System.out.println(iterator.next());
                }
            }
        }
    }

}
