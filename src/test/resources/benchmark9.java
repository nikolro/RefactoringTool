/*
As explained in the paper we add an edge from a parameter to its corresponding
parameter in an overriding method. An edge in the reverse direction is also added.
If we change a parameter variance in function foo for example in class A
we had also change the variance in the foo corresponding parameter in class B.
so here we change the parameters variances for the three method in class A
according to the use site variance and defintion site varance as we explained in the
previos benchmarks.and in the same time the variances of the prarameters in class b changes to the smae variances.
or we can change a parameter variance form b and the corresponding parameter in class A variance will change.
 */
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
