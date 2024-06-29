//check override with method body (witch affect the use site vaniances)
//here the both parameters afeect each other so we need to choose the meet of variances
class bechmark10 {

    class programBefore {
        import java.util.List;

        class A {
            void foo(List<String> animals) {
            }
        }

        class B extends A {
            @Override
            void foo(List<String> animals) {
                String s = "loka";
                animals.add(s);
            }
        }
    }

    class programAfter {
        import java.util.List;

        class A {
            void foo(List<? super String> animals) {
            }
        }

        class B extends A {
            @Override
            void foo(List<? super String> animals) {
                String s = "loka";
                animals.add(s);
            }
        }
    }

}