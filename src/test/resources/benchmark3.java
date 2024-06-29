//check method body without read or write affect on use site variance
class bechmark3 {

    class programBefore {
        import java.util.List;

        class BodyTestBivar<E> {
            public void printSize(List<E> list) {
                System.out.println("list.size(): " + list);
            }
        }
    }

    class programAfter {
        import java.util.List;

        class BodyTestBivar<E> {
            public void printSize(List<?> list) {
                System.out.println("list.size(): " + list);
            }
        }
    }

}
