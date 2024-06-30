/*
in the tests that check the influence flow graph, we find all the changes, apply them,
then compare to the programAfter, so you cant see when a declaration influence other
declaration, if you want too see how out tool change the declaration that influence
other in one click you can run the plugin as we did in the user test and paste
the programBefore content.
*/
/*
As explained in the analysis in section 4.2 in the paper we have to add edeg from
qualifiers to the formal method arguments in the influence flow graph so here we add edge
from dstr to arg, and using the analysis in section 4.2 in the paper we add another
edge from cstr to arg. this mean that if we change the variance of cstr we have
to change the variance of arg ,same for cstr.
The final variance for dstr and cstr are BIVARIANT so after applying the all the changes:
C<x>-->?
C<String>-->C<?>
D<String>-->D<?>
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
