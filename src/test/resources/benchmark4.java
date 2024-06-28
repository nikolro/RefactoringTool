//check method call with write
class bechmark4 {

    class programBefore {
        import java.util.List;
        class BodyTestContravar<E> {
            private E elem = null;
            public boolean addElemTo(List<E> list) {
                return list.add(this.elem);
            }
        }
    }

    class programAfter {
        import java.util.List;
        class BodyTestContravar<E> {
            private E elem = null;
            public boolean addElemTo(List<? super E> list) {
                return list.add(this.elem);
            }
        }
    }

}
