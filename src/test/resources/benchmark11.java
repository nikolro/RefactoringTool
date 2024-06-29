//check override with method body (witch affect the use site vaniances)
//here the both parameters afeect each other so we need to choose the meet of variances
//the first one is + the second - so we get 0
//so nothing change
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