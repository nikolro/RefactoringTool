//mix
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
