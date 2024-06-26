class bechmark4 {
    class programBefore {
        import java.util.List;
        class BodyTestCovar<E> {
            void firstElem(List<E> list) {
                E firstElem = list.get(0);
            }
        }

        class D<Y> {
            void baz(BodyTestCovar<Y> cx) {
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

        class D<Y> {
            void baz(BodyTestCovar<? super Y> cx) {
            }
        }
    }
}
