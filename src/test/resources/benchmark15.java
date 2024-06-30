//galaxy s21 hhhh stam
//// you asked for 10 benchmarks we added more 5 benchmarks as matana, sorry for my bad english :)
/*
We will have edge in the influence flow graph form source parameter
to iterator paramter and after we compute the variances according to the use site variance
and defintion site variance as we explained in the first benchmarks.
We get that source is COVARIANT so we change them both to extends.
and we get that also elems is CONTRAVARIANT so we change to super.
 */
class bechmark15 {

    class programBefore {
        import java.util .*;

        class WList<E> {
            private List<E> elems = new LinkedList<E>();

            void add(E elem) {
                addAll(Collections.singletonList(elem));
            }

            void addAll(List<E> source) {
                addAndLog(source.iterator(), this.elems);
            }

            private void addAndLog(Iterator<E> iterator, List<E> elems) {
                while (iterator.hasNext()) {
                    E elem = iterator.next();
                    elems.add(elem);
                    System.out.println("Added: " + elem);
                }
            }
        }

    }

    class programAfter {
        import java.util .*;

        class WList<E> {
            private List<E> elems = new LinkedList<E>();

            void add(E elem) {
                addAll(Collections.singletonList(elem));
            }

            void addAll(List<? extends E> source) {
                addAndLog(source.iterator(), this.elems);
            }

            private void addAndLog(Iterator<? extends E> iterator, List<? super E> elems) {
                while (iterator.hasNext()) {
                    E elem = iterator.next();
                    elems.add(elem);
                    System.out.println("Added: " + elem);
                }
            }
        }
    }

}