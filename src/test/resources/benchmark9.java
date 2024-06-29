//check changing a method parameter for a overrided method, must change also the
//corresponded parameter in the overrided method (Influence Flow Graph)
class bechmark9 {

    class programBefore {
        import java.util.List;

        class A<X> {
            void foo(List<X> l) {
                X firstElem = l.get(0);
            }

            void foo1(List<X> l) {
            }

            void foo2(List<X> l) {
                X elem = null;
                l.add(elem);
            }
        }

        class B<X> extends A<X> {
            @Override
            void foo(List<X> l) {
                X firstElem = l.get(0);
            }

            @Override
            void foo1(List<X> l) {
            }

            @Override
            void foo2(List<X> l) {
                X elem = null;
                l.add(elem);
            }
        }
    }

    class programAfter {
        import java.util.List;

        class A<X> {
            void foo(List<? extends X> l) {
                X firstElem = l.get(0);
            }

            void foo1(List<?> l) {
            }

            void foo2(List<? super X> l) {
                X elem = null;
                l.add(elem);
            }
        }

        class B<X> extends A<X> {
            @Override
            void foo(List<? extends X> l) {
                X firstElem = l.get(0);
            }

            @Override
            void foo1(List<?> l) {
            }

            @Override
            void foo2(List<? super X> l) {
                X elem = null;
                l.add(elem);
            }
        }
    }

}
