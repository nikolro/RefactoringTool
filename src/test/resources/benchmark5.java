//check method call with both read or write
class bechmark5 {

    class programBefore {
        class BodyTestInvar<E> {
            public boolean readAndWrite(List<E> list) {
                E firstElem = list.get(0);
                return list.add(firstElem);
            }
        }
    }

    class programAfter {
        class BodyTestInvar<E> {
            public boolean readAndWrite(List<E> list) {
                E firstElem = list.get(0);
                return list.add(firstElem);
            }
        }
    }

}
