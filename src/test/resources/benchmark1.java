//check methodCall with read
class bechmark1 {

    class programBefore {
        import java.util.List;
        class BodyTestCovar<E> {
            void firstElem(List<E> list) {
                E firstElem = list.get(0);
            }
        }
    }

    class programAfter {
        import java.util.List;
        class BodyTestCovar<E> {
            void firstElem(List<? extends E> list) {
                E firstElem = list.get(0);
            }
        }
    }

}
