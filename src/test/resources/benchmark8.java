// Check Influence Flow Graph and the variances analysis
/*
in the tests that check the influence flow graph, we find all the changes, apply them,
then compare to the programAfter, so you cant see when a declaration influence other
declaration, if you want too see how out tool change the declaration that influence
other in one click you can run the plugin as we did in the user test and paste
the programBefore content.
*/
class bechmark8 {

class programBefore {
    interface C<X> {
        void foo(D<X> arg);
    }

    interface D<Y> {
        int getNumber();
    }

    class Client {
        void bar(C<String> cstr, D<String> dstr) {
            cstr.foo(dstr);
        }
    }
}

class programAfter {
    interface C<X> {
        void foo(D<?> arg);
    }

    interface D<Y> {
        int getNumber();
    }

    class Client {
        void bar(C<?> cstr, D<?> dstr) {
            cstr.foo(dstr);
        }
    }

}
