//galaxy s21 hhhh stam
class bechmark12 {

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