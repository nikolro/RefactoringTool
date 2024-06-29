// you asked for 10 benchmarks we added more 5 benchmarks as matana
class bechmark15 {

    class programBefore {
        import java.util.*;

        class A{
            void foo(List<String> list) {
                String s=list.get(0);
            }
        }
        class B extends A{
            @Override
            void foo(List<String> list1) {
                String s="loka";
                list1.add(s);
            }
        }
    }

    class programAfter {
        import java.util.*;

        class A{
            void foo(List<String> list) {
                String s=list.get(0);
            }
        }
        class B extends A{
            @Override
            void foo(List<String> list1) {
                String s="loka";
                list1.add(s);
            }
        }
    }

}