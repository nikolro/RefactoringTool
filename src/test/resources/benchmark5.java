//check method body with both read or write affect on use site variance
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
