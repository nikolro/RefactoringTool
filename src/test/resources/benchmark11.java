/*
We compute the variances according to the use site variance and defintion site
variance as we explained in the previous benchmarks.
In this bechmark method foo in class a is overrided in class b so we add edge
between the parameters which means changin one of there variance means changing the other.
the list in class A variance we get is COVARIANT.
the list in class B variance we get is CONTRAVARIANT.
Pressing in one of them to change, changes the other to the variance to (+ meet - = 0).
So the variances doesnt change.
 */
class bechmark11 {

    class programBefore {
        import java.util .*;

        class A {
            void foo(List<String> list) {
                String s = list.get(0);
            }
        }

        class B extends A {
            @Override
            void foo(List<String> list1) {
                String s = "loka";
                list1.add(s);
            }
        }
    }

    class programAfter {
        import java.util .*;

        class A {
            void foo(List<String> list) {
                String s = list.get(0);
            }
        }

        class B extends A {
            @Override
            void foo(List<String> list1) {
                String s = "loka";
                list1.add(s);
            }
        }
    }

}