class bechmark5 {
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
