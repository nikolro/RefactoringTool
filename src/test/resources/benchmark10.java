/*
We compute the variances according to the use site variance and defintion site
variance as we explained in the first benchmarks.
In this bechmark method foo in class A is overrided in class B so we add edge
between the parameters which means changin one of their variance means changing the other.
The list in class A variance we get is BIVARIANT.
The list in class B variance we get is CONTRAVARIANT.
Pressing on one of them to change, changes the other to the variance (* meet - = -).
 */
class bechmark10 {

    class programBefore {
        import java.util.List;

        class A {
            void foo(List<String> list) {
            }
        }

        class B extends A {
            @Override
            void foo(List<String> list) {
                String s = "loka";
                list.add(s);
            }
        }
    }

    class programAfter {
        import java.util.List;

        class A {
            void foo(List<? super String> list) {
            }
        }

        class B extends A {
            @Override
            void foo(List<? super String> list) {
                String s = "loka";
                list.add(s);
            }
        }
    }

}